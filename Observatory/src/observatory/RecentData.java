package observatory;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import observatory.Observatory.GrabDataTask;

public class RecentData
{
	int recentDataDuration = 100;

	ArrayList<DataPoint> listOfDataPoints = new ArrayList<DataPoint>();

	int maximumDataPoints = 6000;

	private int recentAverageOfBigShakes = 0;

	private int recentAverageOfMediumShakes = 0;

	private int recentAverageOfSmallShakes = 0;

	private int recentAverageOfAllShakes  = 0;

	int recentPeak = 0;

	Timer calculationTimer;

	int thresholdLarge = 25, thresholdMedium = 5;

	public RecentData() {
		calculationTimer = new Timer();
		calculationTimer.schedule(new RecentDataCalculation(), 0, 5000);
	}

	public int getBigShakesAverage() {
		return recentAverageOfBigShakes;
	}

	public int getMediumShakesAverage() {
		return recentAverageOfMediumShakes;
	}

	public int getSmallShakesAverage() {
		return recentAverageOfSmallShakes;
	}

	public int getAllShakesAverage() {
		return recentAverageOfAllShakes;
	}

	public void addDataPoint(DataPoint p) {
		synchronized(listOfDataPoints) {
			if (listOfDataPoints.size() ==  maximumDataPoints) {
				// Remove the oldest point if we have too many
				listOfDataPoints.remove(0);
			}
			listOfDataPoints.add(p);
		}
	}

	public void saveData(){

	}
	
	public void setLargeThreshold(int lt) {
		thresholdLarge = lt;
	}
	
	public void setMediumThreshold(int mt) {
		thresholdMedium = mt;
	}

	class RecentDataCalculation extends TimerTask {
		public void run() {
			int counter = 0;
			if (listOfDataPoints.size() > 0) {
				synchronized(listOfDataPoints) {
					recentAverageOfBigShakes = 0;
					recentAverageOfMediumShakes = 0;
					recentAverageOfSmallShakes = 0;
					recentAverageOfAllShakes = 0;
					for (DataPoint d : listOfDataPoints) {
						recentAverageOfAllShakes += d.magnitude;
						counter++;

						if (d.magnitude > thresholdLarge) {
							recentAverageOfBigShakes += d.magnitude;
						}
						else if (d.magnitude > thresholdMedium) {
							recentAverageOfMediumShakes += d.magnitude;
						}
						else{
							recentAverageOfSmallShakes += d.magnitude;
						}
					}

					recentAverageOfBigShakes = recentAverageOfBigShakes/counter;
					recentAverageOfMediumShakes = recentAverageOfMediumShakes/counter;
					recentAverageOfSmallShakes = recentAverageOfSmallShakes/counter;
					recentAverageOfAllShakes = recentAverageOfAllShakes/counter;
				}
			}
		}
	}
}
