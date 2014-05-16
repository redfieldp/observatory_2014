package observatory;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import processing.core.PApplet;

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

    Timer calculationTimer, thresholdTimer;

    int thresholdRecalcInterval = 10000, thresholdCalcScope = 600000;
    int drawnLinesPerSecond = 5; 
    int thresholdLineLimit = (thresholdCalcScope/1000) * drawnLinesPerSecond;

    int thresholdLarge = 25, thresholdMedium = 5;

    public RecentData() {
        calculationTimer = new Timer();
        calculationTimer.schedule(new RecentDataCalculation(), 0, 5000);
        thresholdTimer = new Timer();
        thresholdTimer.schedule(new ThresholdCalculation(), 0, thresholdRecalcInterval);
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

    class ThresholdCalculation extends TimerTask {
        public void run() {
            PApplet.println("RecentData: Try to calculate threshold...");
            ArrayList<DataPoint> thresholdPoints = new ArrayList<DataPoint>();
            if (listOfDataPoints.size() > 0) {
                synchronized(listOfDataPoints) {
                    for (DataPoint d : listOfDataPoints) {
                        if (System.currentTimeMillis()  - d.time < thresholdCalcScope) {
                            if (thresholdPoints.size() < thresholdLineLimit) {
                                thresholdPoints.add(d);
                            }
                            else {
                                DataPoint toRemove = null;
                                for (DataPoint d2 : thresholdPoints) {
                                    if (d.magnitude > d2.magnitude) {
                                        toRemove = d2;
                                        break;
                                    }
                                }
                                if (toRemove != null) {
                                    thresholdPoints.remove(toRemove);
                                    thresholdPoints.add(d);
                                }
                            }
                        }
                    }

                    PApplet.println("RecentData: Calculating new threshold based on " + thresholdPoints.size() + " points...");
                    int newThreshold = 1000000; // temp value
                    for (DataPoint d : thresholdPoints) {
                        if (d.magnitude < newThreshold) {
                            newThreshold = (int)d.magnitude;
                        }
                    }
                    if (newThreshold != 1000000) {
                        PApplet.println("RecentData: New threshold set to " + newThreshold + "!");
                        thresholdLarge = newThreshold;
                    }
                    else {
                        PApplet.println("RecentData: insufficient data to recalculate threshold."); // EL What does this mean? are there no points with mag large enough?
                    }
                }
            }
            else {
                PApplet.println("RecentData: listOfDataPoints has zero points!");
            }
        }
    }
}
