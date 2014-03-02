package observatory;

import java.util.ArrayList;

import processing.core.PApplet;


public class Observatory extends PApplet {
    String dataUrl = "";
    DataFeed currentDataFeed = new DataFeed(dataUrl);
    Template currentTemplate;
    ArrayList<DataPoint> incomingData = new ArrayList<DataPoint>();
    ArrayList<DataPoint> storedDataPoints = new ArrayList<DataPoint>();
    ArrayList<ObservatoryLine> lines = new ArrayList<ObservatoryLine>();
    Template[] templates = {new RainTemplate(), new ToothpicksTemplate(), new ClusteredRightTemplate(), new ClusteredLeftTemplate()};

    boolean performancePaused = false;
    boolean useStoredData = false;
    boolean fullScreenMode = false;
    boolean saveDataToFile = false;
    int maxNumberOfLines = 100;
    int rotateTemplateDuration = 20;
    int thresholdIncrement = 10;
    int canvasHeight = 480;
    int canvasWidth = 640;
    float thicknessUnit = 0.0001f;
    
    public void setup() {
        size(canvasWidth, canvasHeight);
    }

    public void draw() {
        if (!performancePaused) {
            if (useStoredData) {

            }
            else {

            }
        }
    }

    public void keyPressed() {
        if (key == '+') {
            increaseLargeThreshold();
        }
        else if (key == '-'){
            decreaseLargeThreshold();
        }
        else if (key == '9'){
            decreaseMediumThreshold();
        }
        else if (key == '0'){
            increaseMediumThreshold();
        }
        else if (key == 'T'){
            switchToNextTemplate();
        }
        else if (key == ' '){
            togglePause();
        }
        else if (key == 'P'){
            savePDF();
        }
        else if (key == 'L'){
            toggleLiveData();
        }
        else if (key == 'F'){
            toggleFullScreen();
        }
        else if (key == 'Q'){
            exit();
        }
    }

    private void savePDF()
    {
        // TODO Auto-generated method stub

    }

    private void toggleFullScreen()
    {
        // TODO Auto-generated method stub

    }

    private void toggleLiveData()
    {
        // TODO Auto-generated method stub

    }

    private void togglePause()
    {
        // TODO Auto-generated method stub

    }

    private void increaseMediumThreshold()
    {
        // TODO Auto-generated method stub

    }

    private void switchToNextTemplate()
    {
        // TODO Auto-generated method stub

    }

    private void decreaseMediumThreshold()
    {
        // TODO Auto-generated method stub

    }

    private void decreaseLargeThreshold()
    {
        // TODO Auto-generated method stub

    }

    private void increaseLargeThreshold()
    {
        // TODO Auto-generated method stub

    }
}
