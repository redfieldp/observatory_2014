package observatory;

public class ClusteredRightTemplate extends Template
{
    public ClusteredRightTemplate() {
        defaultAngle = -20;

        angleDeviance = 40;

        verticalPlacementOffset = 0;
    }
    
    public float horizontalPlacement(DataPoint p) {
    	return horizontalPlacementRight(p);
    }
}
