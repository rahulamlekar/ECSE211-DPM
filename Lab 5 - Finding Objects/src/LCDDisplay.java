
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.robotics.SampleProvider;
import lejos.utility.Timer;
import lejos.utility.TimerListener;

public class LCDDisplay implements TimerListener{
	public static final int LCD_REFRESH = 100;
	private BlockDetector detector;
	private TextLCD LCD = LocalEV3.get().getTextLCD();
	private Odometer odo;
	private Timer lcdTimer;
	double[] position;
	private SampleProvider usSensor;
	private float[] usData;
	private static int FILTER_OUT = 3;
	private int filterControl;
	private float lastDistance;
	// constructor
	public LCDDisplay(Odometer odo, BlockDetector detector, SampleProvider usSensor, float[] usData) {
		this.odo = odo;
		this.usSensor = usSensor;
		this.usData = usData;
		this.detector = detector;
		position = new double[3];
		this.lcdTimer = new Timer(LCD_REFRESH, this);
		// start the timer
		lcdTimer.start();
	}
	@Override
	public void timedOut() {
		LCD.clear();
		// TODO Auto-generated method stub
		odo.getPosition(position);
		String isThereBlock = "";
		String blockType = "";
		blockType = detector.getBlockType();
		if(detector.isReadingBlock()){
			isThereBlock = "Object Detected";
			//blockType = detector.getBlockType();
		}else{
			
		}
		float[] RGB = detector.getColorData();
		// clear the lines for displaying odometry information
		LCD.drawString(isThereBlock, 0, 0);
		LCD.drawString(blockType, 0, 1);
		LCD.drawString(String.valueOf(formattedDoubleToString(position[0],2).toCharArray()), 3, 2);
		LCD.drawString(String.valueOf(formattedDoubleToString(position[1],2).toCharArray()), 3, 3);
		LCD.drawString(String.valueOf(formattedDoubleToString(position[2],2).toCharArray()), 3, 4);
		//LCD.drawString(String.valueOf(getFilteredData()), 3, 3);
		//LCD.drawString("B:              ", 0, 2);
		// get the odometry information
		//odometer.getPosition(position, new boolean[] { true, true, true });
		
		LCD.drawString(formattedDoubleToString(RGB[0]*10, 2) + "," + formattedDoubleToString(RGB[1]*10, 2)+ "," + formattedDoubleToString(RGB[2]*10, 2) , 3, 5);
		LCD.drawString(formattedDoubleToString(getFilteredData(), 3), 3, 6);

	}
	
	
	private static String formattedDoubleToString(double x, int places) {
		String result = "";
		String stack = "";
		long t;
		
		// put in a minus sign as needed
		if (x < 0.0)
			result += "-";
		
		// put in a leading 0
		if (-1.0 < x && x < 1.0)
			result += "0";
		else {
			t = (long)x;
			if (t < 0)
				t = -t;
			
			while (t > 0) {
				stack = Long.toString(t % 10) + stack;
				t /= 10;
			}
			
			result += stack;
		}
		
		// put the decimal, if needed
		if (places > 0) {
			result += ".";
		
			// put the appropriate number of decimals
			for (int i = 0; i < places; i++) {
				x = Math.abs(x);
				x = x - Math.floor(x);
				x *= 10.0;
				result += Long.toString((long)x);
			}
		}
		
		return result;
	}
	
	private float getFilteredData() {
		usSensor.fetchSample(usData, 0);
		float distance = (int)(usData[0]*100.0);
		float result = 0;
		if (distance > 200){
			// true 255, therefore set distance to 255
			result = 200; //clips it at 50
		} else {
			// distance went below 255, therefore reset everything.
			filterControl = 0;
			result = distance;
		}
		//lastDistance = distance;
		return result;
	}


}
