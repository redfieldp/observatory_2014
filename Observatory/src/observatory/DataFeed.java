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
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class DataFeed
{
	PApplet processingInstance;
	float dataTimeInterval= 10.0f;
	int bigPointTimeExpiration = 600000; // Time until a big point expires, in ms. 60000 is 10 minutes
	String feedTestUrl = "";
	String feedGraphUrl = "";
	boolean detailedDebugging=true;
	
	// Datafeed saves the most recent dataPoints where the magnitude excedes bigThreshold, or mediumThreshold.
	// These gets updated while processing new data, so should always be current
	DataPoint lastBigPoint; 
	DataPoint lastMediumPoint;
	
	Table storedData;
	int expectedPointsPerQuery = (int)dataTimeInterval * 10;
	int tableTraversalCounter = 0;
	
	File saveDataFile; // the file to which we write saved data. 
	FileWriter saveWriter;
	
	boolean firstRun = true;
	
	public DataFeed(PApplet parentApp, int bigThreshold, int medThreshold) {
		processingInstance = parentApp;
		// set the initial values of lastBigPoint and lastMediumPoint, so that they are non-zero
		lastBigPoint = new DataPoint(bigThreshold);
		lastMediumPoint = new DataPoint(medThreshold);
	}

	// GRAB DATA //

	public ArrayList<DataPoint> getFreshData(int bigThreshold, int medThreshold, int magnitudeFactor) {
	    
	    if (saveDataFile == null) {
	        setupSaveDataFile();
	    }
	    
		ArrayList<DataPoint> newData = new ArrayList<DataPoint>();

		// SET UP TIME //
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

		// SET UP DATA URLs //

		// Each value that can potentially be one digit goes through a "fixer" to give it a leading zero
		String tempFeedBaseUrl;
		float tempFeedDuration; //		
		if (firstRun) {	
			tempFeedDuration=600.0f;//
			firstRun = false;
		} else {
			tempFeedDuration=dataTimeInterval;//
		}
		
		// Start date/time
		String startYear = currentTime.get(Calendar.YEAR)+"";
		String startMonth = fixDigits(currentTime.get(Calendar.MONTH) + 1);
		String startDate = fixDigits(currentTime.get(Calendar.DATE));
		String startHours = fixDigits(hours);
		String startMinutes = fixDigits(minutes);
		String startSeconds = fixDigits(seconds);
		
		// If the service is down, or not returning current data, we can adjust the date to run from historical data.
		startMonth = "05"; // Override month 
		
		tempFeedBaseUrl="http://service.iris.edu/irisws/timeseries/1/query?net=CC&sta=SEP&cha=EHZ&start="+
					startYear + "-" +
					startMonth +"-" +
					startDate + "T" +
					startHours + ":" +
					startMinutes + ":" + 
					startSeconds +
					"&duration="+tempFeedDuration +
					"&demean=true&bp=0.1-10.0&scale=AUTO&deci=10&envelope=true&loc=--";
		
		feedTestUrl = tempFeedBaseUrl+ "&output=ascii"; // used for main animation
		feedGraphUrl = tempFeedBaseUrl+ "&output=plot"; // used for graph image overlay

		// GET DATA //
		
		PApplet.println("Datafeed: getting data from '"+ feedTestUrl+"'");
		String[] feedData = {};
		feedData = processingInstance.loadStrings(feedTestUrl); // gets params from datafeed URL
		
		// PROCESS RESULTS - FAIL //
		
		if (feedData == null || feedData.length == 0) {
			// If retrieval failed just return the empty array list
			PApplet.println("DataFeed: ERROR Could not retrieve data! feedData is null or empty!");
			return newData;
		} else {
			PApplet.println("DataFeed: received "+ (feedData.length-1) +" points");
		}

		// PROCESS RESULTS - SUCCESS //

		// Datafeed result looks like this (line 1 is a description):
		// TIMESERIES CC_SEP__EHZ_D, 101 samples, 10 sps, 2014-05-16T16:32:20.000000, TSPAIR, FLOAT, M/S
		// 2014-05-16T16:32:20.000000  5.2979015e-08
		// 2014-05-16T16:32:20.100000  9.9552274e-08
		// 2014-05-16T16:32:20.200000  9.1067754e-08
		// 2014-05-16T16:32:20.300000  7.493982e-08
		// time-of-event   magnitude-of-event	
		// Note that the times include fractional seconds
		
		// Look over lines, skipping the first one
	    for (int i=1; i < feedData.length; i++) {

			// For each subsequent line, we will create a dataPoint
			
		    try {
    			// Split the string
		    	String[] dataInfo = PApplet.split(feedData[i], " ");
		    	// [0] is the time-of-the-event, including fractional seconds.
		    	// There are actually two spaces, so skip [1] of the array
	    		// [2] is the magnitude
		    	
    			// Parse the date    			
				String[] tempDataPointString = PApplet.split(dataInfo[0], "."); // tempDataPointString looks like ["2014-05-16T16:32:21", "600000"]

				//Formula: dataPointTime = dataPointDate + dataPointMilliSeconds
				Date dataPointDate = new Date (); // Only the date of the datapoint, accurate to seconds e.g. 2014-05-16T16:32:21
			    int dataPointMilliSeconds = 0; // 600 is .6 seconds
			    long dataPointTime = 0; // Correct time of the datapoint, including milliseconds
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss");
				
    			try {
					dataPointDate = format.parse(tempDataPointString[0]); // e.g. 2014-05-16T16:32:21
					dataPointMilliSeconds = Integer.parseInt(tempDataPointString[1])/1000; // e.g. 600
					dataPointTime = dataPointDate.getTime() + dataPointMilliSeconds; // e.g. 1400293222000 + 600 = 1400293222600
					//PApplet.println("dataInfo[0]: "+dataInfo[0] +" ==? "+ dataPointDate + " + "+dataPointMilliSeconds+"ms");
					//PApplet.println(" dataPointTime: "+ (dataPointDate.getTime()) +" + "+ (dataPointMilliSeconds) + " ==? "+dataPointTime);
					//PApplet.println(" check math: "+dataPointDate +" ==? " + (new Date(dataPointTime)) );
        		}
    	        catch(ParseException pe) {
    	        	PApplet.println("ERROR: Cannot parse date from this line /"+dataInfo[0]);
    	        }
    			    			    			
    			// Parse the magnitude
    			// PApplet.println("mag "+dataInfo[2]);
    			double originalMagnitude = Double.parseDouble(dataInfo[2]);
    			double scaledMagnitude = originalMagnitude * magnitudeFactor;
    			
    			// CREATE CURRENT DATA POINT, ADD TO DATA //

    			DataPoint currentDataPoint = new DataPoint(dataPointTime, originalMagnitude, scaledMagnitude, lastBigPoint, lastMediumPoint);

    			// Recalculate lastBigPoint, lastMediumPoint
    			
    			// if our current datapoint is large enough, or if 
    			
    			if (scaledMagnitude > bigThreshold || (currentDataPoint.time - lastBigPoint.time > bigPointTimeExpiration)) {
        			// USE THIS ONE // PApplet.println("Datafeed: New Big Point. bigThreshold:"+bigThreshold+" mag:"+scaledMagnitude);
        			lastBigPoint = currentDataPoint;
    			}
				//else if (scaledMagnitude > lastMediumPoint.magnitude || (currentDataPoint.time - lastMediumPoint.time > bigPointTimeExpiration)) {
				//	if (detailedDebugging) PApplet.println("New medium point detected!");
				//	lastMediumPoint = currentDataPoint;
				//}
    			
    			//Add new dataPoint to our data
    			newData.add(currentDataPoint);
    			
    			//Write to our saved data file...
    			try {
    	            saveWriter.write(originalMagnitude + "," + scaledMagnitude + "\n");
    	        }
    	        catch (IOException e) {
    	            PApplet.println("DataFeed: Error writing to saveDataFile (1) !");
    	        }
		    }
		    catch (NumberFormatException e) {
		    	// this happens when the line of data isn't formatted as expected
		        PApplet.println("Datafeed: Error parsing data from feed: \"" + feedData[i] + "\"");
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

        String dateString=new Date().toString().replace('/', '-').replace(' ', '_').replace(':', '-');
	    saveDataFile = new File("saveData_" + dateString + ".csv");
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
	            
	            /*
	            // I COMMENTED THIS OUT WHILE CHANGING DATAPOINT.TIME
	            // STORED DATA IS BROKEN
	            // WE NEED TO STORE THE DATAPOINT TIME AS WELL AS MAGNITUDE IN THE FILE
	             
	            //Create currentDatapoint, add to data
	            double originalMagnitude = r.getDouble("originalMagnitude");
	            double scaledMagnitude = r.getDouble("scaledMagnitude");
	            DataPoint currentDataPoint = new DataPoint(originalMagnitude, scaledMagnitude, lastBigPoint, lastMediumPoint);
	            
    			// Calculate lastBigPoint, lastMediumPoint
	            if (detailedDebugging) PApplet.println("Comparing " + scaledMagnitude + " to " + lastBigPoint.magnitude);
	            if (scaledMagnitude > bigThreshold || (currentDataPoint.time - lastBigPoint.time > timeExpiration)) {
	                if (detailedDebugging) PApplet.println("New big point detected!");
	                lastBigPoint = currentDataPoint;
	            }
	            else if (scaledMagnitude > lastMediumPoint.magnitude || (currentDataPoint.time - lastMediumPoint.time > timeExpiration)) {
	                if (detailedDebugging) PApplet.println("New medium point detected!");
	                lastMediumPoint = currentDataPoint;
	            }
	            */
	            
	            //KLUDGE
	            DataPoint currentDataPoint = new DataPoint(0);
	            
	            //add new dataPoint to our data
	            newData.add(currentDataPoint);
	            
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
