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

//import themidibus.MidiBus;
import java.awt.Toolkit;
import java.awt.datatransfer.*;

public class Observatory extends PApplet {
	
    //PERFORMANCE VARS
    //Most are adjustable w keyboard
//EFGHIJKIJKLMNOPQR

    boolean performancePaused = false;
    boolean useStoredData = false; // If true, we run in 'prerecorded mode,' using data from 
    boolean systemInit = true; // EL turned this on for debugging. Usually false; // application initially waits for input before animating
    boolean fullScreenMode = false;
    boolean pdfTrigger = false;
    boolean showGraph = true; // if true, we show the currentDataGraph
    //int thresholdIncrement = 10;

    //DATAFEED
    ArrayList<DataPoint> incomingData = new ArrayList<DataPoint>();
    ArrayList<DataPoint> storedDataPoints = new ArrayList<DataPoint>();
    RecentData recentData = new RecentData();
    DataFeed currentDataFeed = new DataFeed(this, recentData.thresholdLarge, recentData.thresholdMedium);
    int magnitudeFactor = 1000000000; // used to scale datapoint magnitudes to a more legible range. 

    // STORED DATA
    // Record data to a file, with filename like this: bin/saveData_Fri_May_16_09-35-10_EDT_2014.csv
    // See DataFeed.saveDataFile
    // When running from saved data, we use: storedData/data.csv
    Table dataToLoad;
    ArrayList<DataPoint> storedData = new ArrayList<DataPoint>();
    int storedDataCounter = 0;

    //LINES
    ArrayList<ObservatoryLine> lines = new ArrayList<ObservatoryLine>();
    int maxNumberOfLines = 120;
    public int lineCounter=0; // total number of lines created in this session
    float thicknessUnit = 0.0009f;
    
    //TIMERS
    Timer dataGrabber;
    Timer templateSwitcher;

    //TEMPLATES
    boolean rotateTemplate = true; //if true, we rotate templates every X minutes
    Template[] templates = {new RainTemplate(), new ToothpicksTemplate(), new ClusteredRightTemplate(), new ClusteredLeftTemplate()};
    Template currentTemplate = templates[0];    
    int rotateTemplateDurationMs = 120 * 1000; // 2 minutes. in miliseconds
    int templateRotationCount = 0;
    
    //GRAPHICS
    int canvasHeight = 480;
    int canvasWidth = 640;
    int bgColor = 255;
    int dataUpdateFrequency = 10;
    
    // MIDI
    //MidiBus midi;
    //int midiDeviceId = 0;
    
    //DEBUGGING
    String currentDataGraphUrl="";
    PImage currentDataGraph; // used to show debugging graph of recent data

    
    ////////// SETUP //////////

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

        //midi = new MidiBus(this);
        //midi.addOutput(midiDeviceId);
    }

    public void printDebug(String s) {
    	String temp="";
//    	if (incomingData != null) {
 //   		println( "here" + incomingData );
   // 		for (DataPoint d : incomingData) {
    //			//println( temp+ " " + d.magnitude);
    	//		if (d != null) {
//    			 temp=temp+ " " + d.magnitude;
    //		}
  //        }
//    		println( "there" + incomingData );
//    	}
    	
        println( s +
                "[lines:" + lines.size() +
                " threshold:"+recentData.thresholdLarge +
                " incomingData:" + incomingData.size() +
                " recentData:" + recentData.listOfDataPoints.size() +
                " ("+ currentTemplate.getName() + ")" );
        //println("    points: "+temp);
        
        //currentData.size()
        //" received:" + currentDataFeed.lastPointCount +
        //" when:" + currentDataFeed.lastDataReceived + 
    
    }

    public ArrayList getLines() {
    	return this.lines;
    }
    
    ////////// MAIN DRAW LOOP //////////

    public void draw() {
    	
        if (!systemInit) {
        		// before init, we start with a black background.
                background(0);
        } else {
            // Clear the background color
            background (bgColor);
            
            // if pdfTrigger, record this draw loop to a PDF
            if (pdfTrigger) {
                // #### will be replaced with the frame number
                // format date without special characters, so it can be used in filename
                String tempDateString=new Date().toString().replace('/', '-').replace(' ', '_').replace(':', '-');
                beginRecord(PDF, "LineDrawing_"+ tempDateString + ".pdf"); 
            }
            
            // if showGraph, show currentDataGraph over the top of the main diplay
            if (showGraph) { 
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
                    //this.println("Showing old graph.");
                }				
                tint(255, 60);
                if (currentDataGraph != null) {
                    image(currentDataGraph, 0,0);
                }
            }

            // if not paused, draw some lines from the data
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
                    recentData.saveData(); // QUESTION: WHAT DOES THIS DO?
                }

            }

            if (pdfTrigger) {
                endRecord();
                pdfTrigger = false;
            }
        }
    }


    ////////// MANAGING LINES //////////

    // Creating a line is handled by ObservatoryLine constructor.
    
    private void destroyOldLines() {
    	// removes all expired lines
        ArrayList<ObservatoryLine> toRemove = new ArrayList<ObservatoryLine>();

        for (ObservatoryLine l : lines) {
            if (l.isExpired()) {
                toRemove.add(l);
            }
        }

        for (ObservatoryLine l : toRemove) {
            //printDebug ("Observatory:Remove line #"+l.id+" lifespan:"+(l.lifeSpan/1000)+"s");
            lines.remove(l);
        }
    }

//    private void modifyExistingLine(DataPoint p) {
//        // Only try to find a line to modify if there are lines in existence
//        if (lines.size() > 0) {
//            ObservatoryLine lineToModify = lines.get((int)(p.time % lines.size()));
//            lineToModify.modify(p, recentData, currentTemplate);
//        }
//    }

    ////////// PROCESS DATA POINTS //////////

    private void processDataPoint(ArrayList<DataPoint> currentData) {
        //println("Processing data point from incoming data of size " + currentData.size() + " with magnitude of " + (currentData.get(0).magnitude * magnitudeFactor));
        // Grab the last point in the list
        DataPoint p = currentData.get(0);

        //if ( (p.magnitude > recentData.thresholdLarge) && (lines.size() < maxNumberOfLines)) {
         if ( lines.size() < maxNumberOfLines) {
           	// Create new line
            lineCounter++;
            ObservatoryLine l = new ObservatoryLine(p, currentTemplate, this, lineCounter, lines.size(), recentData.thresholdLarge);
            lines.add(l);
        }
        // else if (p.magnitude > recentData.thresholdMedium) {
        //    modifyExistingLine(p);
        // }

        currentData.remove(p);
        recentData.addDataPoint(p);

    }

    ////////// TIMERS //////////
    
    private void dataTimerSetup() {
        dataGrabber = new Timer();
        dataGrabber.schedule(new GrabDataTask(), 0, dataUpdateFrequency * 1000);
    }

    private void templateTimerSetup() {
        templateSwitcher = new Timer();
        templateSwitcher.schedule(new TemplateRotationTask(), 0, rotateTemplateDurationMs);
    }
    
    
    // GRAB DATA //
    // This is the main data loop //
    class GrabDataTask extends TimerTask {
        public void run() {
            ArrayList<DataPoint> newData;
            if (!useStoredData) {
                newData = currentDataFeed.getFreshData(recentData.thresholdLarge, recentData.thresholdMedium, magnitudeFactor);
            }
            else {
                newData = currentDataFeed.loadStoredData(recentData.thresholdLarge, recentData.thresholdMedium, magnitudeFactor);
            }
            
            for (DataPoint d : newData) {
                incomingData.add(d);
            }
            
            printDebug( "Observatory.Grabdata ");
        }
    }

    class TemplateRotationTask extends TimerTask {
        public void run() {
        	if (rotateTemplate) switchToNextTemplate();
        }
    }

   
    
    ////////// INTERACTIVITY //////////

    public void keyPressed() {
        if (key == 'T'){
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
        else if (key == 'U'){
            copyDataFeedURLtoClipboard();
        }
        else if (key == '}'){
            increaseThreshold();
        }
        else if (key == '{'){
            decreaseThreshold();
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
    
    ////////// HELPERS //////////

    private void savePDF()
    {
    	println("savePDF. ");
        pdfTrigger = true;
    }

    private void toggleFullScreen()
    {
    	println("toggleFullScreen. ");
        fullScreenMode = !fullScreenMode;
    }
    private void toggleShowGraph()
    {
    	println("toggleShowGraph. ");
        showGraph = !showGraph;
    }

    private void togglePause()
    {
    	println("togglePause ");
        performancePaused = !performancePaused;
    }

    private void switchToNextTemplate()
    {
        templateRotationCount = (templateRotationCount + 1) % templates.length;
        currentTemplate = templates[templateRotationCount];
    	println("switchToNextTemplate "+currentTemplate.getName());
    }
    
    public void increaseThreshold(){
    	recentData.thresholdLarge  = recentData.thresholdLarge +10;
    	println("increaseThreshold "+recentData.thresholdLarge);    	
    }
    public void decreaseThreshold(){
    	recentData.thresholdLarge  = recentData.thresholdLarge -10;
    	println("decreaseThreshold "+recentData.thresholdLarge);    	
    }
    public boolean sketchFullScreen() {
    	println(" ");
        return fullScreenMode;
    }
//
//    public void sendMidiMessage(DataPoint d) {
//        int channel = 0;
//        int pitch = 0;
//        int velocity = 0;
//
//        // TODO: Figure out values based on the datapoint
//
//        //midi.sendNoteOn(channel, pitch, velocity);
//    }
    
    Clipboard clipboard;
    
    public void copyDataFeedURLtoClipboard(){
     println("copyDataFeedURLtoClipboard "+currentDataFeed.feedGraphUrl);
     clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
     clipboard.setContents(new StringSelection(currentDataFeed.feedGraphUrl), null);
    }
    public void loadStoredData() {
        dataToLoad = loadTable("storedData/data.csv", "header");
        println(dataToLoad.getRowCount() + " total rows of storedData"); 
        currentDataFeed.setStoredData(dataToLoad);
    }

    public static void main(String args[]) {
        PApplet.main(new String[] { "--present", "observatory.Observatory" });
    }
}
