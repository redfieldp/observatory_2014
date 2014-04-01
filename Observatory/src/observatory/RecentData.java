package observatory;

import java.util.ArrayList;

public class RecentData
{
    int recentDataDuration = 100;

    ArrayList<DataPoint> listOfDataPoints = new ArrayList<DataPoint>();

    int maximumDataPoints = 1000;

    private int recentAverageOfBigShakes = 0;

    private int recentAverageOfMediumShakes = 0;

    private int recentAverageOfSmallShakes = 0;

    private int recentAverageOfAllShakes  = 0;

    int recentPeak = 0;
    //TODO: Thread averages calculation
    public int getBigShakesAverage() {
        // Calculate average before returning
        
        return recentAverageOfBigShakes;
    }
    
    public int getMediumShakesAverage() {
        // Calculate average before returning
        
        return recentAverageOfMediumShakes;
    }
    
    public int getSmallShakesAverage() {
        // Calculate average before returning
        
        return recentAverageOfSmallShakes;
    }
    
    public int getAllShakesAverage() {
        // Calculate average before returning
        
        return recentAverageOfAllShakes;
    }
    
    public void addDataPoint(DataPoint p) {
        if (listOfDataPoints.size() ==  maximumDataPoints) {
            // Remove the oldest point if we have too many
            listOfDataPoints.remove(0);
        }
        listOfDataPoints.add(p);
    }
    
    public void saveData(){
        
    }
}
