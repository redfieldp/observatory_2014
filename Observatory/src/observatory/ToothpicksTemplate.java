package observatory;

public class ToothpicksTemplate extends Template
{
    public ToothpicksTemplate() {
        defaultAngle = 0;

        angleDeviance = 90;

        verticalPlacementOffset = 0;
    }
    
    public float horizontalPlacement() {
        return (float)(Math.random() * 100) + 1;
    }
}
