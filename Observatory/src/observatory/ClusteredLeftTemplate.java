package observatory;

public class ClusteredLeftTemplate extends Template
{
    public ClusteredLeftTemplate() {
        defaultAngle = 20;

        angleDeviance = 40;

        verticalPlacementOffset = -50;
    }
    
    public float horizontalPlacement(DataPoint p) {
    	return horizontalPlacementLeft(p);
    }
}
