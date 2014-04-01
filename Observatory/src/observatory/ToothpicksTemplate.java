package observatory;

public class ToothpicksTemplate extends Template
{
    public ToothpicksTemplate() {
        defaultAngle = 0;

        angleDeviance = 90;

        verticalPlacementOffset = 0;
    }
    
    public float horizontalPlacement(DataPoint p) {
        return (float) (p.time % 1000)/1000;
    }
}
