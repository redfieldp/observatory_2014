package observatory;

public abstract class Template
{
    int defaultAngle;

    float angleDeviance;

    int verticalPlacementOffset;
    
    abstract float horizontalPlacement();
}
