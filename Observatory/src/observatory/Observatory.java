package observatory;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import processing.core.PApplet;
import processing.core.PImage;
import processing.data.Table;
import processing.data.TableRow;
import themidibus.MidiBus;

public class Observatory extends PApplet {
    Template currentTemplate;
    ArrayList<DataPoint> incomingData = new ArrayList<DataPoint>();
    ArrayList<DataPoint> storedDataPoints = new ArrayList<DataPoint>();
    ArrayList<ObservatoryLine> lines = new ArrayList<ObservatoryLine>();
    Template[] templates = {new RainTemplate(), new ToothpicksTemplate(), new ClusteredRightTemplate(), new ClusteredLeftTemplate()};
    Timer dataGrabber;
    Timer templateSwitcher;
    String currentDataGraphUrl="";
    PImage currentDataGraph; // used to show debugging graph of recent data
    //EL new repo test

    boolean performancePaused = false;
    boolean useStoredData = false;
    boolean systemInit = false;
    boolean fullScreenMode = false;
    boolean pdfTrigger = false;
    boolean showGraph = false; // if true, we show the currentDataGraph

    int maxNumberOfLines = 100;
    int rotateTemplateDuration = 20;
    int thresholdIncrement = 10;
    int canvasHeight = 480;
    int canvasWidth = 640;
    int bgColor = 255;
    int dataUpdateFrequency = 10;
    int templateRotationCount = 0;
    int thresholdLarge = 5;
    int thresholdMedium = 25;
    int magnitudeFactor = 1000000000;
    RecentData recentData = new RecentData(thresholdLarge, thresholdMedium);
    DataFeed currentDataFeed = new DataFeed(this, thresholdLarge, thresholdMedium);

    public int lineCounter=0; // total number of lines created in this session

    float thicknessUnit = 0.0001f;

    // MIDI Stuff
    MidiBus midi;
    int midiDeviceId = 0;
    
    // Stored Data Stuff
    Table dataToLoad;
    ArrayList<DataPoint> storedData = new ArrayList<DataPoint>();
    int storedDataCounter = 0;

    public void setup() {
        //***** figure out the display environment ****/
        GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice devices[] = environment.getScreenDevices(); //gets resolution of devices

        canvasWidth = devices[0].getDisplayMode().getWidth();
        canvasHeight = devices[0].getDisplayMode().getHeight();
        println("Adjusting animation size to "+canvasWidth+"x"+canvasHeight+" to fit primary display");

        size(canvasWidth, canvasHeight);

        // Schedule the timers
        dataTimerSetup();
        templateTimerSetup();

        frameRate(10);

        midi = new MidiBus(this);
        midi.addOutput(midiDeviceId);
    }

    public void printDebug(String s) {
        println( s +
                "[lines:" + lines.size() +
                " incomingData:" + incomingData.size() +
                " recentData:" + recentData.listOfDataPoints.size() +
                " received:" + currentDataFeed.lastPointCount +
                " when:" + currentDataFeed.lastDataReceived + 
                " ("+ currentTemplate.getName() + ")" );
        //currentData.size()
    }

    public void draw() {
        if (systemInit) {
            // Clear the background color
            background (bgColor);
            if (pdfTrigger) {
                // #### will be replaced with the frame number
                // format date without special characters, so it can be used in filename
                String tempDateString=new Date().toString().replace('/', '-').replace(' ', '_').replace(':', '-');
                beginRecord(PDF, "LineDrawing_"+ tempDateString + ".pdf"); 
            }

            if (showGraph) { // show currentDataGraph over the top of the main diplay
                if (currentDataGraphUrl!=currentDataFeed.feedGraphUrl) {
                    // url has changed. load a new image
                    this.println("Loading new Graph. currentDataGraphUrl = "+currentDataGraphUrl);
                    currentDataGraphUrl=currentDataFeed.feedGraphUrl;
                    try {
                        currentDataGraph = loadImage (currentDataGraphUrl, "png");
                    }
                    catch(Exception e) {
                        println("Error loading data graph " + e);
                    }
                } else {
                    // url has not changed. we don't need to load a new image.	
                }				
                tint(255, 60);
                if (currentDataGraph != null) {
                    image(currentDataGraph, 0,0);
                }
            }

            if (!performancePaused) {
                // Load data from either file or feed
                ArrayList<DataPoint> currentData;
                if (useStoredData) {
                    currentData = storedDataPoints;
                }
                else {
                    currentData = incomingData;
                }

                // Clear out old lines, using a placeholder arraylist to prevent
                // concurrent modification exceptions
                destroyOldLines();

                // Only try to process data if there is some
                // TODO: Should this be in its own Timer thread?
                if (currentData.size() > 0) {
                    processDataPoint(currentData);
                }

                // Draw the current lines
                for (ObservatoryLine l : lines) {
                    l.draw(width, height);
                }

                if (!useStoredData) {
                    recentData.saveData();
                }

            }

            if (pdfTrigger) {
                endRecord();
                pdfTrigger = false;
            }
        }
        else {
            background(0);
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
        else if (key == 'F'){
            toggleFullScreen();
        }
        else if (key == 'G'){
            toggleShowGraph();
        }
        else if (key == 'Q'){
            exit();
        }
        else if (key == '1'){
            if (!systemInit) {
                useStoredData = false;
                systemInit = true;
            }
        }
        else if (key == '2') {
            if (!systemInit) {
                useStoredData = true;
                loadStoredData();
                systemInit = true;
            }
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
    private void toggleShowGraph()
    {
        showGraph = !showGraph;
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
        recentData.setMediumThreshold(thresholdMedium);
    }

    private void decreaseMediumThreshold()
    {
        if (thresholdMedium > 0 + thresholdIncrement) {
            thresholdMedium -= thresholdIncrement;
        }
        recentData.setMediumThreshold(thresholdMedium);
    }

    private void decreaseLargeThreshold()
    {
        if (thresholdLarge > thresholdMedium + thresholdIncrement) {
            thresholdLarge -= thresholdIncrement;
        }
        recentData.setLargeThreshold(thresholdLarge);
    }

    private void increaseLargeThreshold()
    {
        thresholdLarge += thresholdIncrement;
        recentData.setLargeThreshold(thresholdLarge);
    }

    private void destroyOldLines() {
        ArrayList<ObservatoryLine> toRemove = new ArrayList<ObservatoryLine>();

        for (ObservatoryLine l : lines) {
            if (l.isExpired()) {
                toRemove.add(l);
            }
        }

        for (ObservatoryLine l : toRemove) {
            printDebug("Remove line #"+l.id);
            lines.remove(l);
        }
    }

    private void modifyExistingLine(DataPoint p) {
        ObservatoryLine lineToModify = lines.get((int)(p.time % lines.size()));
        lineToModify.modify(p, recentData, currentTemplate);
    }

    private void processDataPoint(ArrayList<DataPoint> currentData) {
        //println("Processing data point from incoming data of size " + currentData.size() + " with magnitude of " + (currentData.get(0).magnitude * magnitudeFactor));
        // Grab the last point in the list
        DataPoint p = currentData.get(0);
        String tempString="";

        if (p.magnitude * magnitudeFactor > thresholdLarge) {
            if (lines.size() < maxNumberOfLines) {
                lineCounter++;
                ObservatoryLine l = new ObservatoryLine(p, currentTemplate, this, lineCounter);
                if (l.thickness > 1) {
                    lines.add(l);
                }
                else {
                    println("non-visible line");
                }
                tempString = "Create line #" + lineCounter;
            }
        }
        else if (p.magnitude > thresholdMedium) {
            modifyExistingLine(p);
            tempString = "Modify line #";
        }

        currentData.remove(p);
        recentData.addDataPoint(p);

        printDebug(tempString);
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

    public void sendMidiMessage(DataPoint d) {
        int channel = 0;
        int pitch = 0;
        int velocity = 0;

        // TODO: Figure out values based on the datapoint

        midi.sendNoteOn(channel, pitch, velocity);
    }
    
    public void loadStoredData() {
        dataToLoad = loadTable("storedData/data.csv", "header");

        println(dataToLoad.getRowCount() + " total rows of storedData"); 

        currentDataFeed.setStoredData(dataToLoad);
    }

    class GrabDataTask extends TimerTask {
        public void run() {
            ArrayList<DataPoint> newData;
            if (!useStoredData) {
                newData = currentDataFeed.getFreshData(thresholdLarge, thresholdMedium, magnitudeFactor);
            }
            else {
                newData = currentDataFeed.loadStoredData(thresholdLarge, thresholdMedium, magnitudeFactor);
            }
            
            for (DataPoint d : newData) {
                incomingData.add(d);
            }
            
            printDebug("");
        }
    }

    class TemplateRotationTask extends TimerTask {
        public void run() {
            templateRotationCount = (int)random(0,templates.length);
            currentTemplate = templates[templateRotationCount];
        }
    }

    public static void main(String args[]) {
        PApplet.main(new String[] { "--present", "observatory.Observatory" });
    }
}
