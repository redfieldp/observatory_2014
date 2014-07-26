package observatory;

public class ClusteredRightTemplate extends Template
{
    public ClusteredRightTemplate() {
        defaultAngle = 30;

        angleDeviance = 60;

        verticalPlacementOffset = 0;
    }
    
    public float horizontalPlacement(DataPoint p) {
    	return horizontalPlacementRight(p);
    }
}
