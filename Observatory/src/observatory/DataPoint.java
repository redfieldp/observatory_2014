package observatory;

import com.ibm.icu.text.DecimalFormat;

import processing.core.PApplet;

import java.util.Date;

public class DataPoint
{
    long time = 0;// Time should be the time-of-event as reflected in the datafeed. In ms, including fractional seconds from datafeed.
    
    double magnitude; // scaled number is easier to deal with. ref: magnitudeFactor
    double originalMagnitude = 0.0; // original value from data service
    DataPoint lastBigPoint;
    DataPoint lastMediumPoint;
    DataEnvelope peakEnvelope;
    DataEnvelope smoothedEnvelope;
    
    boolean detailedDebugging=false;

	//DataPoint currentDataPoint = new DataPoint(originalMagnitude, scaledMagnitude, lastBigPoint, lastMediumPoint, dataPointTime, dataPointFractionalSeconds);

    
    public DataPoint(double originalMagnitude, double magnitude, DataPoint lastBig, DataPoint lastMedium, long time) {
    	
        // Parse Data point
        this.magnitude = magnitude;
        this.originalMagnitude = originalMagnitude;
        this.time = time;
        
        // Grab other data point arguments
        lastBigPoint = lastBig;
        //lastMediumPoint = lastMedium;
        
        // Create envelopes using points
        peakEnvelope = new DataEnvelope(this, lastBigPoint);
        //smoothedEnvelope = new DataEnvelope(this, lastMediumPoint);
        
        if (detailedDebugging) {
        	DecimalFormat df = new DecimalFormat("####.##");
        	PApplet.println("New DataPoint:" + " mag:" + df.format(magnitude) + " time:"+time); 
        }
    }
        
    // This is the null constructor
    public DataPoint(int threshold) {
        magnitude = threshold;
    }
}
