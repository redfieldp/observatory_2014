package observatory;

import com.ibm.icu.text.DecimalFormat;

import processing.core.PApplet;

//import java.util.Date;

public class DataPoint
{
    long time = 0;// Time should be the time-of-event as reflected in the datafeed. In ms, including fractional seconds from datafeed.
    double magnitude=0.0; // scaled number is easier to deal with. ref: magnitudeFactor
    double originalMagnitude = 0.0; // original value from data service
    double randomized1 = 0.0; // a 'random' number between 0 and 1. Note that this number is not actually random. It is always the same for a given datapoint. For all datapoints, however, it should be smoothly distributed between all values of 0 and 1 
    double randomized2 = 0.0; // a different 'random' number between 0 and 1 
    DataEnvelope peakEnvelope;
    DataEnvelope smoothedEnvelope;

    // DataPoint lastBigPoint; // we don't need to store these. ust use them to calculate envelopes
    // DataPoint lastMediumPoint; // we don't need to store these. ust use them to calculate envelopes
    
    boolean detailedDebugging=false;
    
    public DataPoint(long time, double originalMagnitude, double magnitude, DataPoint lastBig, DataPoint lastMedium) {
    	
        // Parse Data point
        this.magnitude = truncateDecimals(magnitude);
        this.originalMagnitude = truncateDecimals(originalMagnitude);
        this.time = time;
        this.randomized1 = truncateDecimals (magnitude % 1); // produces a 'random' number between 0 and 1.
        this.randomized2 = truncateDecimals (magnitude*100 % 1); // produces another, different 'random' number between 0 and 1.
        //this.randomized2 = (float) (time % 10000000)/10000000; // a different 'random' number between 0 and 1.

        //PApplet.println("p.mag:"+truncateDecimals(magnitude)+" p.random:"+randomized1+" p.random:"+randomized2);
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
        if (detailedDebugging) {
        	DecimalFormat df = new DecimalFormat("####.##");
        	PApplet.println("DataPoint: New DataPoint:" + " mag:" + df.format(magnitude) + " time:"+time + " angle:" + df.format(peakEnvelope.angle) ); 
        }
    }

	public double truncateDecimals(double f) {
		// We go to ten thousandsths, no more.
		double r=Math.round(f*10000)/10000.0000;
		return r;
	}

}
