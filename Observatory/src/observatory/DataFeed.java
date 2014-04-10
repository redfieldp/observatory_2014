package observatory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import processing.core.PApplet;

public class DataFeed
{
	PApplet processingInstance;
	float dataTimeInterval= 10.0f;
	int timeExpiration = 10; // Time until a big point expires
	String lastDataReceived;
	int lastPointCount = 0;

	DataPoint lastBigPoint, lastMediumPoint;

	public DataFeed(PApplet parentApp, int bigThreshold, int medThreshold) {
		processingInstance = parentApp;
		lastBigPoint = new DataPoint(bigThreshold);
		lastMediumPoint = new DataPoint(medThreshold);
	}

	public ArrayList<DataPoint> getFreshData(int bigThreshold, int medThreshold, int magnitudeFactor) {
		ArrayList<DataPoint> newData = new ArrayList<DataPoint>();

		// Set up time for URL query
		Calendar currentTime = Calendar.getInstance();

		int minutes = currentTime.get(Calendar.MINUTE);
		int hours = currentTime.get(Calendar.HOUR);
		int seconds = currentTime.get(Calendar.SECOND);

		// Account for minutes being on a different scale, and hours being in GMT
		
		if (minutes == 0) {
			minutes = 60;
			hours = hours + 4 - 1;
		}
		else {
			minutes = minutes - 1;
			hours = hours + 4;
		}

		if (seconds == 60) {
			seconds = 0;
		}

		// Build URL from time variables
		// Each value that can potentially be one digit goes through a "fixer" to give it a leading zero
		String feedTestUrl="http://service.iris.edu/irisws/timeseries/1/query?net=CC&sta=SEP&cha=EHZ&start="+
				currentTime.get(Calendar.YEAR) + "-" +
				fixDigits(currentTime.get(Calendar.MONTH) + 1) +"-" +
				fixDigits(currentTime.get(Calendar.DATE)) + "T" +
				fixDigits(hours) + ":" +
				fixDigits(minutes) + ":" + 
				fixDigits(seconds) +
				"&duration="+dataTimeInterval+"&demean=true&bp=0.1-10.0&scale=AUTO&deci=10&envelope=true&output=ascii&loc=--";

		PApplet.println("Attempting to retrieve data set from '"+ feedTestUrl+"'");
		String[] feedData = {};
		feedData = processingInstance.loadStrings(feedTestUrl); // gets params from datafeed URL
		
		if (feedData.length == 0) {
			// If retrieval failed just return the empty array list
			lastDataReceived = "ERROR: Could not retrieve data.";
			lastPointCount = 0;
			return newData;
		}
		
		lastDataReceived = ""+ new Date();
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
			PApplet.println("Comparing " + scaledMagnitude + " to " + lastBigPoint.magnitude);
			if (scaledMagnitude > bigThreshold && (scaledMagnitude > lastBigPoint.magnitude || (currentReading.time - lastBigPoint.time > timeExpiration))) {
				PApplet.println("New big point detected!");
				lastBigPoint = currentReading;
			}
			else if (scaledMagnitude > lastMediumPoint.magnitude && (scaledMagnitude > lastMediumPoint.magnitude || (currentReading.time - lastMediumPoint.time > timeExpiration))) {
				PApplet.println("New medium point detected!");
				lastMediumPoint = currentReading;
			}
			newData.add(currentReading);
		}

		PApplet.println("Acquired " + (feedData.length - 1) + " new data points.");

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

}
