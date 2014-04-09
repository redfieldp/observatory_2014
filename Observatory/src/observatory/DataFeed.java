package observatory;

import java.util.ArrayList;
import java.util.Calendar;

import processing.core.PApplet;

public class DataFeed
{
    PApplet processingInstance;
    float dataTimeInterval= 10.0f;
    
    DataPoint lastBigPoint = new DataPoint(), lastMediumPoint = new DataPoint();

    public DataFeed(PApplet parentApp) {
        processingInstance = parentApp;
    }

    public ArrayList<DataPoint> getFreshData(int bigThreshold, int medThreshold, int magnitudeFactor) {
        ArrayList<DataPoint> newData = new ArrayList<DataPoint>();

        // Set up time for URL query
        Calendar currentTime = Calendar.getInstance();

        int minutes = currentTime.get(Calendar.MINUTE);
        int hours = currentTime.get(Calendar.HOUR);

        // Account for minutes being on a different scale, and hours being in GMT
        if (minutes == 0) {
            minutes = 60;
            hours = hours - 1 + 4;
        }
        else {
            minutes = minutes - 1;
            hours = hours + 4;
        }

        // Build URL from time variables
        // Each value that can potentially be one digit goes through a "fixer" to give it a leading zero
        String feedTestUrl="http://service.iris.edu/irisws/timeseries/1/query?net=CC&sta=SEP&cha=EHZ&start="+
                currentTime.get(Calendar.YEAR) + "-" +
                fixDigits(currentTime.get(Calendar.MONTH)) +"-" +
                fixDigits(currentTime.get(Calendar.DATE)) + "T" +
                fixDigits(hours) + ":" +
                fixDigits(minutes) +
                ":00&duration="+dataTimeInterval+"&demean=true&bp=0.1-10.0&scale=AUTO&deci=10&envelope=true&output=ascii&loc=--";

        PApplet.println("Attempting to retrieve data set from '"+ feedTestUrl+"'");

        String[] feedData = processingInstance.loadStrings(feedTestUrl); // gets params from datafeed URL

        // Skip the first line since it is a description and then generate data objects for all others
        for (int i=1; i < feedData.length; i++) {
            // Split the string
            String[] dataInfo = PApplet.split(feedData[i], " ");
            // There are actually two spaces, so skip [1] of the array
            //PApplet.println("time "+dataInfo[0]);
            //PApplet.println("mag "+dataInfo[2]);
            // Create a new DataPoint object and add to the array
            double originalMagnitude = Double.parseDouble(dataInfo[2]);
            DataPoint currentReading = new DataPoint(originalMagnitude, originalMagnitude * magnitudeFactor, lastBigPoint, lastMediumPoint);
            PApplet.println("Comparing " + (originalMagnitude * magnitudeFactor) + " to " + lastBigPoint.magnitude);
            if (originalMagnitude * magnitudeFactor > lastBigPoint.magnitude) {
            	PApplet.println("New big point detected!");
            	lastBigPoint = currentReading;
            }
            else if (originalMagnitude * magnitudeFactor > lastMediumPoint.magnitude) {
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
