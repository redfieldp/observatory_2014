package observatory;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import processing.core.PApplet;


public class Observatory extends PApplet {
	Template currentTemplate;
	ArrayList<DataPoint> incomingData = new ArrayList<DataPoint>();
	ArrayList<DataPoint> storedDataPoints = new ArrayList<DataPoint>();
	ArrayList<ObservatoryLine> lines = new ArrayList<ObservatoryLine>();
	Template[] templates = {new RainTemplate(), new ToothpicksTemplate(), new ClusteredRightTemplate(), new ClusteredLeftTemplate()};
	Timer dataGrabber;
	Timer templateSwitcher;

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
	int bgColor = 255;
	int dataUpdateFrequency = 10;
	int templateRotationCount = 0;
	int thresholdLarge = 500;
	int thresholdMedium = 25;
	int magnitudeFactor = 1000000000;
	RecentData recentData = new RecentData(thresholdLarge, thresholdMedium);
	DataFeed currentDataFeed = new DataFeed(this, thresholdLarge, thresholdMedium);

	float thicknessUnit = 0.0001f;

	public void setup() {
		//***** figure out the display environment ****/
		GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice devices[] = environment.getScreenDevices(); //gets resolution of devices

		canvasWidth = devices[0].getDisplayMode().getWidth();
		canvasHeight = devices[0].getDisplayMode().getHeight();
		println("Adjusting animation size to "+canvasWidth+"x"+canvasHeight+" to fit primary display");
		
		size(canvasWidth, canvasHeight);

		// Initialize Stored Data Points
		loadStoredData();

		// Schedule the timers
		dataTimerSetup();
		templateTimerSetup();
		
		frameRate(10);
	}

	public void draw() {
		// Clear the background color
		background (bgColor);
		if (pdfTrigger) {
			// #### will be replaced with the frame number
			beginRecord(PDF, "LineDrawing_"+ new Date() + ".pdf"); 
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

			if (saveDataToFile) {
				recentData.saveData();
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
			lines.remove(l);
			println("Removing line");
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

		if (p.magnitude * magnitudeFactor > thresholdLarge) {
			if (lines.size() < maxNumberOfLines) {
				println("Creating new line from data point");
				lines.add(new ObservatoryLine(p, currentTemplate, this));
			}
		}
		else if (p.magnitude > thresholdMedium) {
			println("Modifying existing line from data point");
			modifyExistingLine(p);
		}

		currentData.remove(p);
		recentData.addDataPoint(p);
		//println("Processing complete. Recent data size is " + recentData.listOfDataPoints.size() + " and data repo has size of " + currentData.size());
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
				ArrayList<DataPoint> newData = currentDataFeed.getFreshData(thresholdLarge, thresholdMedium, magnitudeFactor);

				for (DataPoint d : newData) {
					incomingData.add(d);
				}
			}
		}
	}

	class TemplateRotationTask extends TimerTask {
		public void run() {
			// TODO: Decide random/in order for template rotation
			templateRotationCount = (templateRotationCount + 1) % templates.length;
			currentTemplate = templates[templateRotationCount];
		}
	}

	public static void main(String args[]) {
		PApplet.main(new String[] { "--present", "observatory.Observatory" });
	}
}
