package observatory;

public class DataFeed
{
    String URL;

    int delayBeforeProcessing = 30; 

    int dataUpdateFrequency = 10;

    float dataTimeInterval= 0.1f;

    int thresholdBig = 100;

    int thresholdMedium = 40;

    int thresholdSmall = 0;
    
    public DataFeed(String feedURL) {
        URL = feedURL;
    }

}
