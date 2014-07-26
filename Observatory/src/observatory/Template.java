package observatory;

import processing.core.PApplet;

public abstract class Template
{
    int defaultAngle;

    float angleDeviance;

    int verticalPlacementOffset;
    
    abstract float horizontalPlacement(DataPoint p);
    
    public float horizontalPlacementCentered(DataPoint p) {
        return (float) .5;
    }

    public float horizontalPlacementRandom(DataPoint p) {
    	// evenly distributed between 0 and 1
        return (float) p.randomized2;
    }

    public float horizontalPlacementLeft(DataPoint p) {
    	return (float) (p.randomized2 /2);
    }
    
    public float horizontalPlacementRight(DataPoint p) {
        return (float) ( .5 + (p.randomized2/2));
    }

    
    public String getName() {
		// Clean up name of current template class for logging purposes
		String templateName = ""+this;
		if (templateName.contains("@")) {
			templateName = templateName.split("@")[0];
		}
		
		return templateName = templateName.substring(12);
    }
}
