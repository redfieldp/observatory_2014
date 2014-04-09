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
	long birthDate;
	PApplet parent;
	int magnitudeFactor = 10000000;

	public ObservatoryLine(DataPoint p, Template currentTemplate, PApplet pRef) { 
		this.lifeSpan = p.peakEnvelope.deltaMagnitude + 1000;
		this.birthDate = System.currentTimeMillis();
		float anglePercentage = (float) (p.peakEnvelope.angle/Math.PI);
		this.thickness = (int)(p.magnitude * magnitudeFactor);
		this.angle = p.peakEnvelope.angle + (anglePercentage * currentTemplate.angleDeviance);
		this.hPos = currentTemplate.horizontalPlacement(p); 
		this.vPos = 0.50f;
		this.length = (int)(p.time % 500);
		this.parent = pRef;
		PApplet.println("Drawing line at " + hPos + ", " + vPos + " with length " + length + " and thickness " + thickness + " and angle of " + angle + " and life span of " + lifeSpan);
		PApplet.println("Here's a change in th sconstructor.");
	}

	public void draw(int currentWidth, int currentHeight) {
	    parent.pushMatrix();
		parent.rotate((float)angle);
		parent.stroke(0);
		parent.strokeWeight(thickness);
		parent.strokeCap(PApplet.SQUARE);
		parent.noFill();
		float x1 = (hPos * currentWidth);
		float y1 = (vPos * currentHeight) - (length/2);
		float x2 = (hPos * currentWidth);
		float y2 = (vPos * currentWidth) + (length/2);
		parent.line(x1, y1, x2, y2);
		parent.popMatrix();
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
