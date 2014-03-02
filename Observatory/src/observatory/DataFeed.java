package observatory;

import java.util.ArrayList;

public class DataFeed
{
    String URL;

    float dataTimeInterval= 0.1f;
    
    public DataFeed(String feedURL) {
        URL = feedURL;
    }

    public ArrayList<DataPoint> getFreshData() {
        ArrayList<DataPoint> newData = new ArrayList<DataPoint>();
        
        // TODO: Grab new data and populate list
        
        return newData;
    }
    
}
