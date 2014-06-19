package observatory;

import processing.core.PApplet;


public class ObservatoryLine
{
	int thickness;
	double angle;
	float hPos;
	float vPos;
	int length;
	int lifeSpan;
	long birthTime;
	PApplet parent;
	int id; // line ID
	
	int thicknessScalar = 60; // bigger number, smaller line.
	int minimumThickness=1;
    int lifeSpanScalar = 1000; // bigger number, longer life
    int timeScalar = 100000; // bigger number, longer line
	
	public ObservatoryLine(DataPoint p, Template currentTemplate, PApplet pRef, int lineId) { 
		// create a new line
		this.id = lineId;
		this.birthTime = System.currentTimeMillis();
		this.lifeSpan = (int) ( Math.abs(p.peakEnvelope.deltaMagnitude)*lifeSpanScalar );
		this.thickness = Math.max( (int)(p.magnitude/thicknessScalar), minimumThickness);
		/*
		Let D be a very small number, much smaller than the magnitude of a typical datapoint.
		E.g., if our datapoint.magnitutes are around 5x10-8 (.00000005), then set D to be 1x10-9 (.000000001).
		Datapoint.angle = ( (datapoint.magnitude mod D) / D) * 2pi)
		Line.angle = Template.baseAngle + (datapoint.angle * template.angleVariation)
		*/
		float anglePercentage = (float) (p.peakEnvelope.angle/Math.PI);
		this.angle = PApplet.map((float)(p.peakEnvelope.angle + (anglePercentage * currentTemplate.angleDeviance)), 0, 2 * PApplet.PI, (float)Math.toRadians(currentTemplate.defaultAngle), (float)Math.toRadians(currentTemplate.defaultAngle + currentTemplate.angleDeviance));
		this.angle = Math.round(this.angle*100)/100.00;
		
		this.hPos = currentTemplate.horizontalPlacement(p); 
		this.vPos = 0.50f;
		this.length = (int)(p.time % timeScalar);
		this.parent = pRef;
		PApplet.println("ObservatoryLine: New line #"+this.id+" (" + hPos + ", " + vPos + ") length:" + length + " thickness:" + thickness + " angle:" + angle + " lifespan:" + (lifeSpan/1000) + "ms");
		//PApplet.println("ObservatoryLine: New line #"+id+" lifeSpan:" + (lifeSpan/1000)+"s");
		
		// each line should be visible to the eye. common problems
		// Is the line drawn offscreen?
		// Is the line‘s lifespan too short?
		if (this.lifeSpan < 1000) PApplet.println("!!! ObservatoryLine: line#"+this.id+" lifespan:"+this.lifeSpan +" peak:"+ p.peakEnvelope.deltaMagnitude);
		// Is the line’s thickness too small?
		if (this.thickness < 1) PApplet.println("!!! ObservatoryLine: line#"+this.id+" Thickness:"+this.thickness);
		
	}

	public void draw(int currentWidth, int currentHeight) {
		// visualize the line on the canvas.
		
	    parent.pushMatrix();
	    parent.translate(parent.width/2, parent.height/2);
		parent.rotate((float)angle);
		parent.stroke(0);
		parent.strokeWeight(thickness);
		parent.strokeCap(PApplet.SQUARE);
		parent.noFill();
		float x1 = (hPos * currentWidth) - parent.width/2;
		float y1 = (vPos * (currentHeight - (length/2))) - parent.height/2;
		float x2 = (hPos * currentWidth) - parent.width/2;
		float y2 = (vPos * (currentHeight + (length/2))) - parent.height/2;
		parent.line(x1, y1, x2, y2);
		parent.popMatrix();
	}

	public boolean isExpired() {
		if (birthTime + lifeSpan < System.currentTimeMillis()) {
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

	private void modifyAngle(double angle) {

	}

	private void modifyHPos(long time) {
		int change = ((int)(time % 10)) - 5;
		hPos = hPos + change;
	}

	private void modifyVPos(double angle, long duration, int verticalOffset) {

	}

	private void modifyLength(long time) {

	}
}
