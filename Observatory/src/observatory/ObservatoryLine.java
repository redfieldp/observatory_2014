package observatory;

//import java.util.ArrayList;

import processing.core.PApplet;


public class ObservatoryLine
{
	int thickness; // larger than minimumThickness
	int alpha; // 255=near white, 0=black
	double angle;
	float hPos;
	float vPos;
	int length;
	int lifeSpan;
	long birthTime;
	PApplet parent;
	int id; // line ID
	
	int thicknessScalar = 120; // bigger number, smaller line.
	double thicknessScalar2 = .5; // bigger number, bigger line.
	int minimumThickness=1;
	int minimumLifespan=1000;
	int maximumThickness=50;
    int lifeSpanScalar = 1000; // bigger number, longer life
    int timeScalar = 100000; // bigger number, longer line
	
	
	public ObservatoryLine(DataPoint p, Template currentTemplate, PApplet pRef, int lineId, int numLines, int thresholdLarge) { 
		// create a new line
		this.id = lineId;
		this.birthTime = System.currentTimeMillis();
		this.lifeSpan = Math.max( (int) ( Math.abs(p.peakEnvelope.deltaMagnitude)*lifeSpanScalar ), minimumLifespan);
		
		// thickness
		// Start by using the amount by which the datapoint magnitude exceeds our threshold
		//double relativeMagnitude = Math.max(1, truncateDecimals(p.magnitude-thresholdLarge) ); // expected values between 1 and 1000 or more
		// log it
		//double logRelativeMagnitude= truncateDecimals( Math.log(relativeMagnitude) / Math.log(100)); // between 0 and 10. (log10=2, log100=4, log500=6, log50000= 10)
		
		//double tempThickness = truncateDecimals( PApplet.map((float) logRelativeMagnitude, (float)0.0, (float)10.0, (float)minimumThickness, (float)maximumThickness));
		//this.thickness = (int) tempThickness;

		//PApplet.println("... thickness:"+this.thickness+". thresholdLarge:"+thresholdLarge+" relativeMagnitude:"+ relativeMagnitude + " map:"+ tempThickness+" thickness:"+thickness);
		//PApplet.println("... "+this.thickness+". mag:"+ truncateDecimals(p.magnitude) +"-"+ thresholdLarge + "="+ relativeMagnitude + ", "+ logRelativeMagnitude + ", "+ tempThickness);
		
		// OLD
		this.thickness = Math.max( (int)((p.magnitude - thresholdLarge) /thicknessScalar), minimumThickness);

		// ALPHA
		// scale alpha between 255 (mag=0, white) to 0 (mag=thresholdLarge, black)
		this.alpha = (p.magnitude>thresholdLarge)? 0:240;
		//this.alpha = (p.magnitude>=thresholdLarge)? 0: (int) Math.round(255*((thresholdLarge-p.magnitude)/thresholdLarge));
		
		/*
		Let D be a very small number, much smaller than the magnitude of a typical datapoint.
		E.g., if our datapoint.magnitutes are around 5x10-8 (.00000005), then set D to be 1x10-9 (.000000001).
		Datapoint.angle = ( (datapoint.magnitude mod D) / D) * 2pi)
		Line.angle = Template.baseAngle + (datapoint.angle * template.angleVariation)
		*/
		p.peakEnvelope.angle = truncateDecimals (p.peakEnvelope.angle);
		//double anglePercentage = truncateDecimals ( (double) (p.peakEnvelope.angle/Math.PI) );
		//this.angle = truncateDecimals ( PApplet.map((float)(p.peakEnvelope.angle + (anglePercentage * currentTemplate.angleDeviance)), 0, 2 * PApplet.PI, (float)Math.toRadians(currentTemplate.defaultAngle), (float)Math.toRadians(currentTemplate.defaultAngle + currentTemplate.angleDeviance)) );
		// this is perfectly random:
		//this.angle = truncateDecimals ( PApplet.map ((float)p.randomized1, (float)0.0, (float)1.0, (float)0.0, (float)Math.PI) );
		this.angle = truncateDecimals (
				PApplet.map (
						(float)p.randomized1,
						(float)0.0,
						(float)1.0,
						(float)Math.toRadians(- currentTemplate.angleDeviance/2),
						(float)Math.toRadians(currentTemplate.angleDeviance/2)
						)
						+ Math.toRadians(currentTemplate.defaultAngle)
				);
		//debug
		//this.angle=0;
		//this.lifeSpan=5000;
		
		this.hPos = (float) truncateDecimals(currentTemplate.horizontalPlacement(p)); 
		this.vPos = (float) truncateDecimals( 0.50f );
		this.length = (int)(p.time % timeScalar);
		this.parent = pRef;
		String tempString= (p.magnitude > thresholdLarge)?">":"<"; 
		PApplet.println("ObservatoryLine: New line #"+this.id+" ("+numLines+","+currentTemplate.getName()+") mag:"+p.magnitude+tempString+thresholdLarge+" pos(" + hPos + ", " + vPos + ") length:" + length + " thickness:" + thickness + " angle:" + angle + " lifespan:" + (lifeSpan/1000) + "ms");
		//PApplet.println("... *p.peakEnvelope.angle:" + p.peakEnvelope.angle + " *anglePercentage:" + anglePercentage + " *angle:" + angle);
		//PApplet.println("ObservatoryLine: New line #"+id+" lifeSpan:" + (lifeSpan/1000)+"s");
		
		
		// each line should be visible to the eye. common problems
		// Is the line drawn offscreen?
		// Is the line's lifespan too short?
		if (this.lifeSpan < minimumLifespan) PApplet.println("!!! ObservatoryLine: line#"+this.id+" lifespan:"+this.lifeSpan +" peak:"+ p.peakEnvelope.deltaMagnitude);
		// Is the lineÕs thickness too small?
		if (this.thickness < minimumThickness) PApplet.println("!!! ObservatoryLine: line#"+this.id+" Thickness:"+this.thickness);
		
	}
	public double truncateDecimals(double f) {
		// We go to ten thousandsths, no more.
		double r=Math.round(f*10000)/10000.0000;
		return r;
	}
	
	public void draw(int currentWidth, int currentHeight) {
		// visualize the line on the canvas.
		
	    parent.pushMatrix();
	    parent.translate(parent.width/2, parent.height/2);
		parent.rotate((float)angle);
		parent.stroke(alpha);
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

//	public void modify(DataPoint p, RecentData data, Template currentTemplate) {
//		int modifcationVariable = (int)(p.time % 5);
//
//		switch (modifcationVariable) {
//		case 0:
//			modifyThickness((int)p.magnitude, data.getMediumShakesAverage());
//			break;
//		case 1:
//			modifyAngle(p.smoothedEnvelope.angle);
//			break;
//		case 2: 
//			modifyHPos(p.time);
//			break;
//		case 3:
//			modifyVPos(this.angle, p.smoothedEnvelope.duration, currentTemplate.verticalPlacementOffset);
//			break;
//		case 4:
//			modifyLength(p.smoothedEnvelope.duration);
//			break;
//		}
//	}
//
//	private void modifyThickness(int magnitude, int shakesAvg) {
//
//	}
//
//	private void modifyAngle(double angle) {
//
//	}
//
//	private void modifyHPos(long time) {
//		int change = ((int)(time % 10)) - 5;
//		hPos = hPos + change;
//	}
//
//	private void modifyVPos(double angle, long duration, int verticalOffset) {
//
//	}
//
//	private void modifyLength(long time) {
//
//	}
	
}
