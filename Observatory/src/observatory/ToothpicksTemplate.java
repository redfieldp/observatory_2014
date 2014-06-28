package observatory;

public class ToothpicksTemplate extends Template
{
    public ToothpicksTemplate() {
        defaultAngle = 0; // 0 to 360

        angleDeviance = 180; // from 0 to 180 (360 is equivalend to 180)

        verticalPlacementOffset = 0;
    }


    public float horizontalPlacement(DataPoint p) {
        return horizontalPlacementRandom(p);
    }
}
