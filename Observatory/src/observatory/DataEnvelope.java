package observatory;

import com.ibm.icu.text.DecimalFormat;

import processing.core.PApplet;

public class DataEnvelope
{
	
	// Data Envelope is a line from a dataPoint to another, earlier dataPoint.
	// Represents the angle resulting from the difference in time and magnitude between two datapoints.
	// Useful as another parameter for a stream of data

	long duration;
    double deltaMagnitude;
    double angle;
    double durationScalar = .05; // highly important for calculating angle.
    
	boolean detailedDebugging=true;
	
    DataEnvelope (DataPoint newPoint, DataPoint oldPoint) {
        duration = (newPoint.time - oldPoint.time);
        deltaMagnitude = (newPoint.magnitude - oldPoint.magnitude);
        
        // We have opposite (delta mag) and adjacent (duration) so use cotan to get angle
        if (duration > 0) {
        	angle = Math.atan(deltaMagnitude/ (duration * durationScalar) );
        }
        else {
        	PApplet.println("Envelope: ERROR: Duration is zero");
        	angle = 0;
        }

        
        if (detailedDebugging) {

        	DecimalFormat df = new DecimalFormat("####.######");
//        	PApplet.println("Envelope: newPoint.time:" + newPoint.time +
//        			" oldPoint.time:" + oldPoint.time );
        	
//        	PApplet.println("Envelope: duration:" + duration +
//        			" deltaMag:" + df.format(deltaMagnitude) +
//        			" angle:" + df.format(angle) );
        	
        }
        
    }
}
