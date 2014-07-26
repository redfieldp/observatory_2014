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
    	// sin wave with max 75% to the left
    	//PApplet.println ("p.randomized2:"+p.randomized2+", "+(p.randomized2*Math.PI*2) + ","+ Math.sin( p.randomized2 * Math.PI*2)+", "+(1+ (Math.sin( p.randomized2 * Math.PI*2) )));
    	return (float) ( (Math.sin(p.randomized2*Math.PI*2) + 1) / 2 );
    }
    
    public float horizontalPlacementRight(DataPoint p) {
    	// sin wave with max 75% to the right
        return (float) ( ( - Math.sin(p.randomized2*Math.PI*2) + 1) / 2 );
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
