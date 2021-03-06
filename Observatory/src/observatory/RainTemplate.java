package observatory;

public class RainTemplate extends Template
{
    public RainTemplate() {
        defaultAngle = 0;

        angleDeviance = 10;

        verticalPlacementOffset = 20;
    }
    
    public float horizontalPlacement(DataPoint p) {
        return horizontalPlacementRandom(p);
    }
}
