package observatory;

public class ClusteredRightTemplate extends Template
{
    public ClusteredRightTemplate() {
        defaultAngle = 30;

        angleDeviance = 60;

        verticalPlacementOffset = -50;
    }
    
    public float horizontalPlacement(DataPoint p) {
        return horizontalPlacementRandom(p); // (float) (100 - (Math.sin(Math.random() * Math.PI/2) * 100));
    }
}
