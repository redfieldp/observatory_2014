package observatory;

import com.ibm.icu.text.DecimalFormat;

import processing.core.PApplet;

import java.util.Date;

public class DataPoint
{
    long time = 0;// Time should be the time-of-event as reflected in the datafeed. In ms, including fractional seconds from datafeed.
    double magnitude=0.0; // scaled number is easier to deal with. ref: magnitudeFactor
    double originalMagnitude = 0.0; // original value from data service
    DataEnvelope peakEnvelope;
    DataEnvelope smoothedEnvelope;

    // DataPoint lastBigPoint; // we don't need to store these. ust use them to calculate envelopes
    // DataPoint lastMediumPoint; // we don't need to store these. ust use them to calculate envelopes
    
    boolean detailedDebugging=true;
    
    public DataPoint(long time, double originalMagnitude, double magnitude, DataPoint lastBig, DataPoint lastMedium) {
    	
        // Parse Data point
        this.magnitude = magnitude;
        this.originalMagnitude = originalMagnitude;
        this.time = time;
        
        // Create envelopes using points
        peakEnvelope = new DataEnvelope(this, lastBig);
        //smoothedEnvelope = new DataEnvelope(this, lastMediumPoint);
        
        if (detailedDebugging) {
        	DecimalFormat df = new DecimalFormat("####.##");
        	PApplet.println("DataPoint: New DataPoint:" + " mag:" + df.format(magnitude) + " time:"+time + " angle:" + df.format(peakEnvelope.angle) ); 
        }
    }
        
    // This is the null constructor
    public DataPoint(int threshold) {
        magnitude = threshold;
    	PApplet.println("Creating New DataPoint:" + " mag:" + this.magnitude + " time:"+this.time); 
    }
}
