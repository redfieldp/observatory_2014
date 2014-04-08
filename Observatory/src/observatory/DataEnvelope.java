package observatory;

public class DataEnvelope
{
    long duration;
    int deltaMagnitude;
    double angle;
    
    DataEnvelope (DataPoint newPoint, DataPoint oldPoint) {
        duration = newPoint.time - oldPoint.time;
        deltaMagnitude = (int)(newPoint.magnitude - oldPoint.magnitude);
        
        // We have opposite (delta mag) and adjacent (duration) so use cotan to get angle
        if (duration > 0) {
        	angle = Math.atan(deltaMagnitude/duration);
        }
        else {
        	angle = 0;
        }
    }
}
