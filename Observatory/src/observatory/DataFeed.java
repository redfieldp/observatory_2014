package observatory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import processing.core.PApplet;
import processing.data.Table;
import processing.data.TableRow;

public class DataFeed
{
	PApplet processingInstance;
	float dataTimeInterval= 10.0f;
	int timeExpiration = 30; // Time until a big point expires
	String lastDataReceived;
	int lastPointCount = 0;
	String feedTestUrl = "";
	String feedGraphUrl = "";
	boolean detailedDebugging=false;
	
	DataPoint lastBigPoint, lastMediumPoint;
	
	Table storedData;
	int expectedPointsPerQuery = (int)dataTimeInterval * 10;
	int tableTraversalCounter = 0;
	
	File saveDataFile;
	FileWriter saveWriter;

	public DataFeed(PApplet parentApp, int bigThreshold, int medThreshold) {
		processingInstance = parentApp;
		lastBigPoint = new DataPoint(bigThreshold);
		lastMediumPoint = new DataPoint(medThreshold);
	}

	public ArrayList<DataPoint> getFreshData(int bigThreshold, int medThreshold, int magnitudeFactor) {
	    
	    if (saveDataFile == null) {
	        setupSaveDataFile();
	    }
	    
		ArrayList<DataPoint> newData = new ArrayList<DataPoint>();

		// Set up time for URL query
		Calendar currentTime = Calendar.getInstance();

		int minutes = currentTime.get(Calendar.MINUTE);
		int hours = currentTime.get(Calendar.HOUR_OF_DAY);
		int seconds = currentTime.get(Calendar.SECOND);

		// Account for minutes being on a different scale, and hours being in GMT
		hours = hours + 4;
		if (minutes == 0) {
			minutes = 60;
		}
		else {
			minutes = minutes - 1;
		}

		if (seconds == 60) {
			seconds = 0;
		}

		// Build URL from time variables
		// Each value that can potentially be one digit goes through a "fixer" to give it a leading zero
		
		String feedBaseUrl="http://service.iris.edu/irisws/timeseries/1/query?net=CC&sta=SEP&cha=EHZ&start="+
				currentTime.get(Calendar.YEAR) + "-" +
				fixDigits(currentTime.get(Calendar.MONTH) + 1) +"-" +
				fixDigits(currentTime.get(Calendar.DATE)) + "T" +
				fixDigits(hours) + ":" +
				fixDigits(minutes) + ":" + 
				fixDigits(seconds) +
				"&duration="+dataTimeInterval+"&demean=true&bp=0.1-10.0&scale=AUTO&deci=10&envelope=true&loc=--";
		
		feedTestUrl = feedBaseUrl+ "&output=ascii"; // used for main animation
		feedGraphUrl = feedBaseUrl+ "&output=plot"; // used for image

		
		PApplet.println("Datafeed: getting data from '"+ feedTestUrl+"'");
		String[] feedData = {};
		feedData = processingInstance.loadStrings(feedTestUrl); // gets params from datafeed URL
		
		if (feedData == null || feedData.length == 0) {
			// If retrieval failed just return the empty array list
			lastDataReceived = "ERROR: Could not retrieve data.";
			lastPointCount = 0;
			return newData;
		}
		
		Date tempDate=new Date();
		lastDataReceived = ""+ tempDate.getHours()+":"+tempDate.getMinutes()+":"+tempDate.getSeconds();
		lastPointCount = feedData.length - 1;
		
		// Skip the first line since it is a description and then generate data objects for all others
		for (int i=1; i < feedData.length; i++) {
			// Split the string
			String[] dataInfo = PApplet.split(feedData[i], " ");
			// There are actually two spaces, so skip [1] of the array
			//PApplet.println("time "+dataInfo[0]);
			//PApplet.println("mag "+dataInfo[2]);
			// Create a new DataPoint object and add to the array
			double originalMagnitude = Double.parseDouble(dataInfo[2]);
			double scaledMagnitude = originalMagnitude * magnitudeFactor;
			DataPoint currentReading = new DataPoint(originalMagnitude, scaledMagnitude, lastBigPoint, lastMediumPoint);
			if (detailedDebugging) PApplet.println("Comparing " + scaledMagnitude + " to " + lastBigPoint.magnitude);
			
			if (scaledMagnitude > bigThreshold || (currentReading.time - lastBigPoint.time > timeExpiration)) {
				if (detailedDebugging) PApplet.println("New big point detected!");
				lastBigPoint = currentReading;
			}
			else if (scaledMagnitude > lastMediumPoint.magnitude || (currentReading.time - lastMediumPoint.time > timeExpiration)) {
				if (detailedDebugging) PApplet.println("New medium point detected!");
				lastMediumPoint = currentReading;
			}
			newData.add(currentReading);
			
			try {
	            saveWriter.write(originalMagnitude + "," + scaledMagnitude + "\n");
	        }
	        catch (IOException e) {
	            PApplet.println("Could not write to data save file!");
	        }
		}

		try
        {
            saveWriter.flush();
            PApplet.println("Completed data write.");
        }
        catch (IOException e)
        {
            PApplet.println("Could not resolve writing of data save file!");
        }
		
		PApplet.println("DataFeed: " + (feedData.length - 1) + " new data points received.");

		return newData;
	}

	/**
	 * Fix single digit ints so that they have a leading zero
	 */
	private String fixDigits(int value) {
		if (value < 10) {
			return "0" + value;
		}
		else {
			return "" + value;
		}
	}
	
	public void setupSaveDataFile() {
	    Date now = new Date();
	    saveDataFile = new File("saveData_" + now + ".csv");
	    try {
	        saveWriter = new FileWriter(saveDataFile);
	        saveWriter.write("originalMagnitude,scaledMagnitude\n");
	    }
	    catch (IOException e) {
	        PApplet.println("Could not create data save file!");
	    }
	}

    public void setStoredData (Table dataTable) {
        storedData = dataTable;
    }
	
	public ArrayList<DataPoint> loadStoredData(int bigThreshold, int medThreshold, int magnitudeFactor) {
	    ArrayList<DataPoint> newData = new ArrayList<DataPoint>();
	    
	    if (storedData != null && tableTraversalCounter < storedData.getRowCount()) {
	        int expectedDataCounter = 0;
	        while (expectedDataCounter < expectedPointsPerQuery && tableTraversalCounter < storedData.getRowCount()) {
	            TableRow r = storedData.getRow(tableTraversalCounter);
	            double originalMagnitude = r.getDouble("originalMagnitude");
	            double scaledMagnitude = r.getDouble("scaledMagnitude");
	            DataPoint currentReading = new DataPoint(originalMagnitude, scaledMagnitude, lastBigPoint, lastMediumPoint);
	            if (detailedDebugging) PApplet.println("Comparing " + scaledMagnitude + " to " + lastBigPoint.magnitude);
	            
	            if (scaledMagnitude > bigThreshold || (currentReading.time - lastBigPoint.time > timeExpiration)) {
	                if (detailedDebugging) PApplet.println("New big point detected!");
	                lastBigPoint = currentReading;
	            }
	            else if (scaledMagnitude > lastMediumPoint.magnitude || (currentReading.time - lastMediumPoint.time > timeExpiration)) {
	                if (detailedDebugging) PApplet.println("New medium point detected!");
	                lastMediumPoint = currentReading;
	            }
	            newData.add(currentReading);
	            tableTraversalCounter++;
	            expectedDataCounter++;
	        }
	    }
	    
	    return newData;
	}
}
