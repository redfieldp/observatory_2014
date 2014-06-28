package observatory;

public class ToothpicksTemplate extends Template
{
    public ToothpicksTemplate() {
        defaultAngle = 0;

        angleDeviance = 90;

        verticalPlacementOffset = 0;
    }
    public float horizontalPlacementCentered(DataPoint p) {
        return (float) .5;
    }

    public float horizontalPlacementRandom(DataPoint p) {
    	// evently distributed between 0 and 1
        return (float) p.randomized2;
    }

    public float horizontalPlacement(DataPoint p) {
        return horizontalPlacementRandom(p);
    }
}
