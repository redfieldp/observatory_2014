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
	
	File saveDataFile; // the file to which we write saved data. 
	FileWriter saveWriter;
	
	boolean firstRun = true;

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
		
		if (firstRun) {
		    // Set the calendar we are creating the URL from back 10 minutes
		    // if it's the first run, since we need a bigger data range
		    currentTime.add(Calendar.MINUTE, -10);
		}

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
		String tempFeedBaseUrl;
		float tempFeedDuration; //
		
		if (firstRun) {	
			tempFeedDuration=600.0f;//
			firstRun = false;
		} else {
			tempFeedDuration=dataTimeInterval;//
		}
			
		tempFeedBaseUrl="http://service.iris.edu/irisws/timeseries/1/query?net=CC&sta=SEP&cha=EHZ&start="+
					currentTime.get(Calendar.YEAR) + "-" +
					fixDigits(currentTime.get(Calendar.MONTH) + 1) +"-" +
					fixDigits(currentTime.get(Calendar.DATE)) + "T" +
					fixDigits(hours) + ":" +
					fixDigits(minutes) + ":" + 
					fixDigits(seconds) +
					"&duration="+tempFeedDuration +
					"&demean=true&bp=0.1-10.0&scale=AUTO&deci=10&envelope=true&loc=--";
		
		
		feedTestUrl = tempFeedBaseUrl+ "&output=ascii"; // used for main animation
		feedGraphUrl = tempFeedBaseUrl+ "&output=plot"; // used for image

		
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
		    try {
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
    	            PApplet.println("DataFeed: Error writing to saveDataFile (1) !");
    	        }
		    }
		    catch (NumberFormatException e) {
		        PApplet.println("Error parsing data from feed: \"" + feedData[i] + "\"");
		    }
		}

		try
        {
            saveWriter.flush();
            //PApplet.println("DataFeed: Successfully wrote data to saveDataFile.");
        }
        catch (IOException e)
        {
            PApplet.println("DataFeed: !!! Error writing to saveDataFile (2) !");
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
		// format date without special characters, so it can be used in filename
	    // We always record data to a file, with filename like this: bin/saveData_Fri_May_16_09-35-10_EDT_2014.csv

        String tempDateString=new Date().toString().replace('/', '-').replace(' ', '_').replace(':', '-');
	    saveDataFile = new File("saveData_" + tempDateString + ".csv");
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
	    
	    if (firstRun) {
	        // On the first run, we want to grab 10 minutes of stored data from the archive
	        expectedPointsPerQuery = 6000;
	    }
	    
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
	    
	    if (firstRun) {
	        expectedPointsPerQuery = (int)dataTimeInterval * 10;
	        firstRun = false;
	    }
	    
	    return newData;
	}
}
