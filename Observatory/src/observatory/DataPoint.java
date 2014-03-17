package observatory;

import java.util.Date;

public class DataPoint
{
    long time = System.currentTimeMillis();

    double originalMagnitude = 0.0;

    int magnitude;
    
    DataPoint lastBigPoint;

    DataPoint lastMediumPoint;

    DataEnvelope peakEnvelope;

    DataEnvelope smoothedEnvelope;
    
    public DataPoint(String dataPoint, DataPoint lastBig, DataPoint lastMedium) {
        // Parse Data point
        
        // Grab other data point arguments
        lastBigPoint = lastBig;
        lastMediumPoint = lastMedium;
        
        // Create envelopes using points
        peakEnvelope = new DataEnvelope(this, lastBigPoint);
        smoothedEnvelope = new DataEnvelope(this, lastMediumPoint);
    }
}
