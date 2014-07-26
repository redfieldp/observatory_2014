package observatory;

public class ClusteredLeftTemplate extends Template
{
    public ClusteredLeftTemplate() {
        defaultAngle = 0;

        angleDeviance = 20;

        verticalPlacementOffset = -50;
    }
    
    public float horizontalPlacement(DataPoint p) {
    	return horizontalPlacementLeft(p);
    }
}
