/*
 * LightLocalizer.java
 * Alessandro Commodari and Asher Wright
 * ECSE 211 DPM Lab 4 - Localization
 * Group 53
 * Given that the robot is facing 0 degrees in the first block,
 * uses the color sensor to localize the robot by reading black lines while spinnning around.
 */
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

public class BlockDetector extends Thread {
	//constants
		//class variables
	//private Odometer odo;
	private static final double DETECTIONTHRESHOLDERROR = 0.5;
	private SampleProvider colorSensor;
	private float[] colorData;
	private boolean isReadingBlock;
	private String blockType;
	private float[] RGBValues;
	private double[] blueBlockReading;
	private double[] darkBlueBlockReading;
	private double[] noObjectReading;
	//motors
	//private EV3LargeRegulatedMotor leftMotor, rightMotor;
	//Navigation navi; //the navigation class
	
	public BlockDetector(SampleProvider colorSensor, float[] colorData) {
		//get incoming values for variables
		//this.odo = odo;
		this.colorSensor = colorSensor;
		this.colorData = colorData;
		blockType = "Other";
		blueBlockReading = new double[3];
		noObjectReading = new double[3];
		darkBlueBlockReading = new double[3];
		blueBlockReading[0] = 0.95;
		blueBlockReading[1] = 1.4;
		blueBlockReading[2] = 1.15;
		darkBlueBlockReading[0] = 0.2;
		darkBlueBlockReading[1] = 0.5;
		darkBlueBlockReading[2] = 0.7;
		noObjectReading[0] = 0;
		noObjectReading[1] = 0;
		noObjectReading[2] = 0;
		isReadingBlock = false;
		
	}
	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 * The method that is called when this thread starts.
	 */
	public void run(){
		while(true){
			//gets the data from the color sensor.
			colorSensor.fetchSample(colorData, 0);
			
			//TODO: create conditions for what is a close match.
			double[] BlueBlockError = new double[3];
			double totalNoObjectError = 0;
			double[] DarkBlueBlockError  = new double[3];
			for(int i = 0; i < 3; i++){
				BlueBlockError[i] = Math.abs(colorData[i]*10 - blueBlockReading[i]);
				totalNoObjectError += Math.abs(colorData[i]*10 - noObjectReading[i]);
				DarkBlueBlockError[i] = Math.abs(colorData[i]*10 - darkBlueBlockReading[i]);
			}
			//If our reading is within the allowed number to consider it a blue block, update what it sees.
			if(totalNoObjectError < 0.25){
				blockType = "";
				isReadingBlock = false;
			}else if(BlueBlockError[0] < DETECTIONTHRESHOLDERROR && BlueBlockError[1] < DETECTIONTHRESHOLDERROR &&  BlueBlockError[2] < DETECTIONTHRESHOLDERROR ){
				blockType = "BLOCK";
				isReadingBlock =true;
			}else if(DarkBlueBlockError[0] < DETECTIONTHRESHOLDERROR && DarkBlueBlockError[1] < DETECTIONTHRESHOLDERROR && DarkBlueBlockError[2] < DETECTIONTHRESHOLDERROR){
				blockType = "BLOCK";
				isReadingBlock = true;
			}else{
				blockType = "NOT BLOCK";
				isReadingBlock = true;
			}
			try {
				Thread.sleep(200);											// sleep for 200 mS
			} catch (Exception e) {
				System.out.println("Error: " + e.getMessage());
			}
		}
	}

	//accessors
	public float[] getColorData(){
		synchronized (this) {
			return colorData;	
		}
	}
	public String getBlockType(){
		synchronized (this) {
			return blockType;	
		}
	
	}
	public boolean isReadingBlock(){
		synchronized (this) {
			return isReadingBlock;	
		}
	}

}
