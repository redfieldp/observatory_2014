package observatory;

import processing.core.PApplet;


public class Observatory extends PApplet {
    String dataUrl = "";
    DataFeed currentDataFeed = new DataFeed(dataUrl);
    
    boolean performancePaused = false;
    
	public void setup() {
	}

	public void draw() {
	    if (!performancePaused) {
	        
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
