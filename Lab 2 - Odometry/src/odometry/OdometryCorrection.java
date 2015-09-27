package odometry;

import lejos.hardware.Sound;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.hardware.lcd.TextLCD;
/* 
 * OdometryCorrection.java
 */
public class OdometryCorrection extends Thread {
	private static final long CORRECTION_PERIOD = 10;
	private static final double SENSORDIST = 3.3;
	private Odometer odometer;
	private EV3ColorSensor colorSensor;
	private SensorMode currentSensorMode; 
	private double lastBrightnessLevel;
	//Change this to be NOT first brightness but the average 
	private double firstBrightnessLevel;
	private double currBrightnessLevel;
	private double significantPercentThreshold = 20;
	private float[] RGBValues = new float[3];
	private String reachedBlackLine = "";
	private boolean isFirstXCorrection = true;
	private boolean isFirstYCorrection = true;
	// constructor
	public OdometryCorrection(Odometer odometer, EV3ColorSensor colorSense) {
		this.odometer = odometer;
		colorSensor = colorSense;
	}

	// run method (required for Thread)
	public void run() {
		long correctionStart, correctionEnd;
		lastBrightnessLevel = 0;
		firstBrightnessLevel = -1;
		while (true) {
			correctionStart = System.currentTimeMillis();
			// put your correction code here
			
			//basically, if we see a black line, update our position to be the nearest 15 + 30k for Y and X?
			//we check to see if we're increasing Y --> update Y. increasing X --> update X?
			colorSensor.setFloodlight(lejos.robotics.Color.WHITE);
			colorSensor.getRGBMode().fetchSample(RGBValues, 0);
			
			//brightness is the average of the magnitude of RGB.
			currBrightnessLevel = (RGBValues[0] + RGBValues[1] + RGBValues[2]);
			
			if (firstBrightnessLevel == -1){
				firstBrightnessLevel = currBrightnessLevel;
			}else{ //if this our at least our second measurement
				if (currBrightnessLevel < 0.20) {
					reachedBlackLine = "true";
				}else if(100*Math.abs(currBrightnessLevel - firstBrightnessLevel)/firstBrightnessLevel > significantPercentThreshold){
					//we have a significant change
					if(currBrightnessLevel < firstBrightnessLevel){
						//we've reached a black line!!!
						reachedBlackLine = "true";
					}
				}else{
					reachedBlackLine = "false";
				}
				
				//lastBrightnessLevel = currBrightnessLevel;
				if(reachedBlackLine.equals("true")){
					correctOdometerPosition();
				}
			}

			
			
			// this ensure the odometry correction occurs only once every period
			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD
							- (correctionEnd - correctionStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometry correction will be
					// interrupted by another thread
				}
			}
		}
	}
	private void correctOdometerPosition(){
		double currX = odometer.getX();
		double currY = odometer.getY();
		int currT = (int) odometer.getTheta();
		double correctedX = 0;
		double correctedY = 0;
		
		int nearestTheta = findNearestTheta(currT);
		
		if(nearestTheta == 0){ //this means we need to correct X position
			correctedX = findCorrectedX(currX);
			correctedY = currY;
			if (currX < correctedX){
				correctedX -= SENSORDIST;
			}else{
				correctedX += SENSORDIST;
			}
			
		}else if(nearestTheta == 90){ //this means we need to correct Y position
			correctedY = findCorrectedY(currY);
			correctedX = currX;
			
			if (currY < correctedY){
				correctedY -= SENSORDIST;
			}else{
				correctedY += SENSORDIST;
			}
		}
		double[] position = new double[3];
		position[0] = correctedX;
		position[1] = correctedY;
		position[2] = 0;
		boolean[] update = new boolean[3];
		update[0] = true;
		update[1] = true;
		update[2] = false;
		odometer.setPosition(position,update);
		
	}
	private double findCorrectedX(double x){
		double result = 0;
		//Sound.beep();
		if(isFirstXCorrection){
			result = 15;
			isFirstXCorrection = false;
		}else{
			for(int i = 0; i < 4; i++){
				if(Math.abs(x - (15-SENSORDIST + 30*i)) < 10){
					result = 15 + 30*i;
					break;
				}
			}	
		}
		return result;
	}
	private double findCorrectedY(double y){
		double result = 0;
		//Sound.buzz();
		if(isFirstYCorrection){
			result = -15;
			isFirstYCorrection = false;
		}else{
			for(int i = 0; i < 4; i++){
				if(Math.abs(y + (15-SENSORDIST + 30*i)) < 10){
					result = (-1)*(15 + 30*i);
					break;
				}
			}
		}
		return result;
	}
	private int findNearestTheta(int t){
		int result = 0;
		
		if (Math.abs(t) < 5 || Math.abs(t-360) < 5 || Math.abs(t+360) < 5 || Math.abs(t-180) < 5 || Math.abs(t+180) < 5){
			result = 0;
		}else if (Math.abs(t-90) < 5 || Math.abs(t+90) < 5 || Math.abs(t-270) < 5 || Math.abs(t+270) < 5){
			result = 90;
		}
		
		return result;
	}
	
	public String isReadingBlack(){
		return this.reachedBlackLine;
	}
	public double getR(){
		return RGBValues[0];
	}
	public double getG(){
		return RGBValues[1];
	}
	public double getB(){
		return RGBValues[2];
	}
	public double getBrightness(){
		return currBrightnessLevel;
	}
}