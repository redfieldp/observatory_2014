package observatory;

public abstract class Template
{
    int defaultAngle;

    float angleDeviance;

    int verticalPlacementOffset;
    
    abstract float horizontalPlacement(DataPoint p);
    
    public String getName() {
		// Clean up name of current template class for logging purposes
		String templateName = ""+this;
		if (templateName.contains("@")) {
			templateName = templateName.split("@")[0];
		}
		
		return templateName = templateName.substring(12);
    }
}
