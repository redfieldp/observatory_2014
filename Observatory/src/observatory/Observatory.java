package observatory;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import processing.core.PApplet;


public class Observatory extends PApplet {
    String dataUrl = "";
    DataFeed currentDataFeed = new DataFeed(dataUrl);
    Template currentTemplate;
    ArrayList<DataPoint> incomingData = new ArrayList<DataPoint>();
    ArrayList<DataPoint> storedDataPoints = new ArrayList<DataPoint>();
    ArrayList<ObservatoryLine> lines = new ArrayList<ObservatoryLine>();
    Template[] templates = {new RainTemplate(), new ToothpicksTemplate(), new ClusteredRightTemplate(), new ClusteredLeftTemplate()};
    Timer dataGrabber;
    Timer templateSwitcher;
    RecentData recentData = new RecentData();

    boolean performancePaused = false;
    boolean useStoredData = false;
    boolean fullScreenMode = false;
    boolean saveDataToFile = false;
    boolean pdfTrigger = false;

    int maxNumberOfLines = 100;
    int rotateTemplateDuration = 20;
    int thresholdIncrement = 10;
    int canvasHeight = 480;
    int canvasWidth = 640;
    int dataUpdateFrequency = 10;
    int templateRotationCount = 0;
    int thresholdLarge = 400;
    int thresholdMedium = 200;

    float thicknessUnit = 0.0001f;

    public void setup() {
        size(canvasWidth, canvasHeight);
        
        // Initialize Stored Data Points
        loadStoredData();

        // Schedule the timers
        dataTimerSetup();
        templateTimerSetup();
    }

    public void draw() {
        if (pdfTrigger) {
            // #### will be replaced with the frame number
            beginRecord(PDF, "LineDrawing_"+ new Date() + ".pdf"); 
        }

        if (!performancePaused) {
            if (useStoredData) {

            }
            else {

            }
        }

        if (pdfTrigger) {
            endRecord();
            pdfTrigger = false;
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
        pdfTrigger = true;
    }

    private void toggleFullScreen()
    {
        fullScreenMode = !fullScreenMode;
    }

    private void toggleLiveData()
    {
        useStoredData = !useStoredData;
    }

    private void togglePause()
    {
        performancePaused = !performancePaused;
    }

    private void switchToNextTemplate()
    {
        templateRotationCount = (templateRotationCount + 1) % templates.length;
        currentTemplate = templates[templateRotationCount];
    }

    private void increaseMediumThreshold()
    {
        if (thresholdMedium < thresholdLarge - thresholdIncrement) {
            thresholdMedium += thresholdIncrement;
        }
    }

    private void decreaseMediumThreshold()
    {
        if (thresholdMedium > 0 + thresholdIncrement) {
            thresholdMedium -= thresholdIncrement;
        }
    }

    private void decreaseLargeThreshold()
    {
        if (thresholdLarge > thresholdMedium + thresholdIncrement) {
            thresholdLarge -= thresholdIncrement;
        }
    }

    private void increaseLargeThreshold()
    {
        thresholdLarge += thresholdIncrement;
    }
    
    private void processDataPoint(DataPoint p) {
        if (p.magnitude > thresholdLarge) {
            // TODO: Create a new line
            
        }
        else if (p.magnitude > thresholdMedium) {
            // TODO: Modify an existing line
            
        }
        
        incomingData.remove(p);
        recentData.addDataPoint(p);
    }
    
    private void loadStoredData() {
        // TODO: Load to incoming data from a stored data file
    }

    private void dataTimerSetup() {
        dataGrabber = new Timer();
        dataGrabber.schedule(new GrabDataTask(), 0, dataUpdateFrequency * 1000);
    }

    private void templateTimerSetup() {
        templateSwitcher = new Timer();
        templateSwitcher.schedule(new TemplateRotationTask(), 0, rotateTemplateDuration * 1000);
    }

    public boolean sketchFullScreen() {
        return fullScreenMode;
    }
    
    class GrabDataTask extends TimerTask {
        public void run() {
            if (!useStoredData) {
                ArrayList<DataPoint> newData = currentDataFeed.getFreshData();
                
                for (DataPoint d : newData) {
                    incomingData.add(d);
                }
            }
        }
    }

    class TemplateRotationTask extends TimerTask {
        public void run() {
            templateRotationCount = (templateRotationCount + 1) % templates.length;
            currentTemplate = templates[templateRotationCount];
        }
    }
}
