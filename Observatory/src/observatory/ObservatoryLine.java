package observatory;


public class ObservatoryLine
{
    int thickness;
    float angle;
    float hPos;
    int vPos;
    int length;
    int lifeSpan;
    long birthDate;

    public ObservatoryLine(DataPoint p, Template currentTemplate) { 
        this.thickness = (int)p.magnitude;
        this.lifeSpan = p.peakEnvelope.deltaMagnitude;
        this.birthDate = System.currentTimeMillis();
        float anglePercentage = (float) (p.peakEnvelope.angle/Math.PI);
        this.angle = p.peakEnvelope.angle + (anglePercentage * currentTemplate.angleDeviance);
        this.hPos = currentTemplate.horizontalPlacement(p); 
        this.vPos = 0;
        this.length = (int)(p.time % 500);
    }

    public void draw(int currentWidth, int currentHeight) {
        // TODO: Decide if templates are a constructor of method argument
    }

    public boolean isExpired() {
    	if (System.currentTimeMillis() + lifeSpan < System.currentTimeMillis()) {
    		return true;
    	}
    	return false;
    }
    
    public void modify(DataPoint p, RecentData data, Template currentTemplate) {
    	int modifcationVariable = (int)(p.time % 5);
    	
    	switch (modifcationVariable) {
    		case 0:
    			modifyThickness((int)p.magnitude, data.getMediumShakesAverage());
    			break;
    		case 1:
    			modifyAngle(p.smoothedEnvelope.angle);
    			break;
    		case 2: 
    			modifyHPos(p.time);
    			break;
    		case 3:
    			modifyVPos(this.angle, p.smoothedEnvelope.duration, currentTemplate.verticalPlacementOffset);
    			break;
    		case 4:
    			modifyLength(p.smoothedEnvelope.duration);
    			break;
    	}
    }
    
	private void modifyThickness(int magnitude, int shakesAvg) {
	
	}
	
	private void modifyAngle(float angle) {
		
	}
		
	private void modifyHPos(long time) {
		int change = ((int)(time % 10)) - 5;
		hPos = hPos + change;
	}
		
	private void modifyVPos(float angle, long duration, int verticalOffset) {
		
	}
		
	private void modifyLength(long time) {
		
	}
}
