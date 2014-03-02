package observatory;

import java.util.Date;

public class ObservatoryLine
{
    int thickness;
    float angle;
    float hPos;
    int vPos;
    int length;
    int lifeSpan;
    Date birthDate;

    public ObservatoryLine(DataPoint p, Template currentTemplate) { 
        this.thickness = p.magnitude;
        this.lifeSpan = p.peakEnvelope.deltaMagnitude;
        this.birthDate = new Date();
        
        // TODO: Clarify these variables
        this.angle = p.peakEnvelope.angle;
        this.hPos = currentTemplate.horizontalPlacement(); 
        this.vPos = 0;
        this.length = 0;
    }

    public void draw() {

    }

}
