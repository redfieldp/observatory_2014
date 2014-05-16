package observatory;

import com.ibm.icu.text.DecimalFormat;

import processing.core.PApplet;

public class DataPoint
{
    // TODO: What to use for time?
    long time = System.currentTimeMillis(); // Time should be the time-of-event as reflected in the datafeed

    double magnitude; // scaled number is easier to deal with. ref: magnitudeFactor
    double originalMagnitude = 0.0; // original value from data service
    DataPoint lastBigPoint;
    DataPoint lastMediumPoint;
    DataEnvelope peakEnvelope;
    DataEnvelope smoothedEnvelope;
    
    boolean detailedDebugging=true;
    
    public DataPoint(double originalMagnitude, double magnitude, DataPoint lastBig, DataPoint lastMedium) {
    	
        // Parse Data point
        this.magnitude = magnitude;
        this.originalMagnitude = originalMagnitude;
        
        // Grab other data point arguments
        lastBigPoint = lastBig;
        lastMediumPoint = lastMedium;
        
        // Create envelopes using points
        peakEnvelope = new DataEnvelope(this, lastBigPoint);
        smoothedEnvelope = new DataEnvelope(this, lastMediumPoint);
        
        if (detailedDebugging) {
        	DecimalFormat df = new DecimalFormat("####.##");
        	PApplet.println("New DataPoint:" + " mag:" + df.format(magnitude)); // + " (" + originalMagnitude + ")" );
        }
    }
        
    // This is the null constructor
    public DataPoint(int threshold) {
        magnitude = threshold;
    }
}
