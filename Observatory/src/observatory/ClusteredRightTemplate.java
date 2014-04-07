package observatory;

public class ClusteredRightTemplate extends Template
{
    public ClusteredRightTemplate() {
        defaultAngle = 0;

        angleDeviance = 20;

        verticalPlacementOffset = -50;
    }
    
    public float horizontalPlacement(DataPoint p) {
        return (float) (100 - (Math.sin(Math.random() * Math.PI/2) * 100));
    }
}
