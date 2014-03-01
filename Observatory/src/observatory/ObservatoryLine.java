package observatory;

import java.util.Date;

public class ObservatoryLine
{
    int thickness;
    float angle;
    int hPos;
    int vPos;
    int length;
    int lifeSpan;
    Date birthDate;

    public ObservatoryLine(int thickness, float angle, int hPos, int vPos, int length, int lifeSpan, Date birthDate) { 
        this.thickness = thickness;
        this.angle = angle;
        this.hPos = hPos; 
        this.vPos = vPos;
        this.length = length;
        this.lifeSpan = lifeSpan;
        this.birthDate = birthDate;
    }

    public void draw() {

    }

}
