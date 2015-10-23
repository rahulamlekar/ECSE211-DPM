import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;


public class Driver extends Thread {
	private static final int ROTATESPEED = 30;
	private static final int FORWARDSPEED = 50;
	
	private SampleProvider usSensor;
	private float[] usData;
	private int filterControl;
	private static int FILTER_OUT = 3;
	private float lastDistance;
	EV3LargeRegulatedMotor leftMotor;
	EV3LargeRegulatedMotor rightMotor;
	EV3LargeRegulatedMotor armMotor1;
	EV3LargeRegulatedMotor armMotor2;
	Odometer odo;
	Navigation navi;
	BlockDetector detector;
	/*
	 * constructor. We need the odometer (for position info), the detector (for block detection), and the US data
	 */
	public Driver(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,EV3LargeRegulatedMotor armMotor1,EV3LargeRegulatedMotor armMotor2, Odometer odo, BlockDetector detector, SampleProvider usSensor, float[] usData, Navigation navi){
		this.usData = usData;
		this.usSensor = usSensor;
		filterControl = 0;
		lastDistance = 100;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.odo = odo;
		this.navi = navi;
		this.detector= detector;
		this.armMotor1 = armMotor1;
		this.armMotor2 = armMotor2;
	}
	
	/*
	 * Finds the block on the field, grabs it, and returns it.
	 */
	public void run(){
		//begin spinning clockwise to scan for blocks
		leftMotor.setSpeed(ROTATESPEED);
		rightMotor.setSpeed(ROTATESPEED);
		leftMotor.forward();
		rightMotor.backward();
		boolean haveTurned = false;
		while(true){
			double USDistance = getFilteredUSData();
			/*
			 * if we see a block
			 */
			if(USDistance < 4){
				leftMotor.stop(true);
				rightMotor.stop(true);
				break;
			}else if(USDistance <22 && haveTurned == false){
				leftMotor.setSpeed(ROTATESPEED);
				rightMotor.setSpeed(ROTATESPEED);
				leftMotor.forward();
				rightMotor.backward();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				haveTurned = true;
			haveTurned = true;
			}else if(USDistance < 90){
				leftMotor.setSpeed(FORWARDSPEED);
				rightMotor.setSpeed(FORWARDSPEED);
				leftMotor.forward();
				rightMotor.forward();
			}else if(USDistance > 90){
				leftMotor.setSpeed(ROTATESPEED);
				rightMotor.setSpeed(ROTATESPEED);
				leftMotor.forward();
				rightMotor.backward();
			}
			
		}
		/*
		 * we beep once for a block and twice for the wooden block.
		 */
		if(detector.isReadingBlock()){
			Sound.beep();
			bringDownArms();
			navi.travelTo(65, 65);
		}else{
			Sound.beep();
			Sound.beep();
		}
		
	}
	private void bringDownArms(){
		armMotor1.setSpeed(20);
		armMotor2.setSpeed(20);
		armMotor1.rotate(110, true);
		armMotor2.rotate(110, false);
		//Sound.buzz();
	}
	private float getFilteredUSData() {
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
	//Conversion methods.
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}
	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
}
