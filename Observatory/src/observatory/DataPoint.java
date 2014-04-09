package observatory;

public class DataPoint
{
    // TODO: What to use for time?
    long time = System.currentTimeMillis();

    double originalMagnitude = 0.0;

    double magnitude;
    
    DataPoint lastBigPoint;

    DataPoint lastMediumPoint;

    DataEnvelope peakEnvelope;

    DataEnvelope smoothedEnvelope;
    
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
    }
    
    // This is the null constructor
    public DataPoint(int threshold) {
        magnitude = threshold;
    }
}
