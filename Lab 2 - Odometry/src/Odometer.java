import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;

/*
 * Odometer.java :)
 */
public class Odometer extends Thread {
	// robot position
	private double x, y, theta;
	
	public static int lastTachoL;// Tacho L at last sample 
	public static int lastTachoR;// Tacho R at last sample

	
	public static int nowTachoL;// Current tacho L 
	public static int nowTachoR;// Current tacho R
	public static double wheelRadius;
	public static double wheelTrack;
	static EV3LargeRegulatedMotor motorL;  // L 
	static EV3LargeRegulatedMotor motorR; // R 
	
	// odometer update period, in ms
	private static final long ODOMETER_PERIOD = 25;

	// lock object for mutual exclusion
	private Object lock;

	// default constructor
	public Odometer(double wheelR, double wheelT, EV3LargeRegulatedMotor leftM,EV3LargeRegulatedMotor rightM) {
		x = 0.0;
		y = 0.0;
		theta = 0.0;
		lock = new Object();
		wheelRadius = wheelR;
		wheelTrack = wheelT;
		motorL = leftM;
		motorR = rightM;
	}

	// run method (required for Thread)
	public void run() {
		long updateStart, updateEnd;
		//starting up odometer, reset count to 0
		motorL.resetTachoCount();
		motorR.resetTachoCount();
		//get first value.
		lastTachoL = motorL.getTachoCount();
		lastTachoR = motorR.getTachoCount();
		
		int changeLeftTacho;
		int changeRightTacho;
		double changeRadLeft;
		double changeRadRight;
		while (true) {
			updateStart = System.currentTimeMillis();
			// put (some of) your odometer code here
			nowTachoL = motorL.getTachoCount();
			nowTachoR = motorR.getTachoCount();
			
			
			changeLeftTacho = nowTachoL - lastTachoL;
			changeRadLeft = changeLeftTacho*2*Math.PI/360.0;
			changeRightTacho = nowTachoR - lastTachoR;
			changeRadRight = changeRightTacho*2*Math.PI/360.0;
			
			lastTachoL = nowTachoL;
			lastTachoR = nowTachoR;
			
			double arcLengthL = wheelRadius*changeRadLeft;
			double arcLengthR = wheelRadius*changeRadRight;
			
			
			double changeInTheta = (arcLengthR - arcLengthL)/wheelTrack;
			
			double changeInCenterArclength = (arcLengthR + arcLengthL)/2;
			
			double deltaX = changeInCenterArclength*Math.cos((theta + theta + changeInTheta)/2);
			double deltaY = changeInCenterArclength*Math.sin((theta + theta + changeInTheta)/2);
			synchronized (lock) {
				// don't use the variables x, y, or theta anywhere but here!
				theta += changeInTheta;
				x +=deltaX;
				y +=deltaY;
				
			}

			// this ensures that the odometer only runs once every period
			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < ODOMETER_PERIOD) {
				try {
					Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometer will be interrupted by
					// another thread
				}
			}
		}
	}

	// accessors
	public void getPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				position[0] = x;
			if (update[1])
				position[1] = y;
			if (update[2])
				position[2] = getTheta();
		}
	}

	public double getX() {
		double result;

		synchronized (lock) {
			result = x;
		}

		return result;
	}

	public double getY() {
		double result;

		synchronized (lock) {
			result = y;
		}

		return result;
	}

	public double getTheta() {
		double result;
		
		synchronized (lock) {
		result = theta*360.0/(2.0*Math.PI);
		
			if(result >= 360){
				result = result - 360;
			}else if(result <= -360){
				result = result + 360;
			}
		}

		return result;
	}

	// mutators
	public void setPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				x = position[0];
			if (update[1])
				y = position[1];
			if (update[2])
				theta = position[2];
		}
	}

	public void setX(double x) {
		synchronized (lock) {
			this.x = x;
		}
	}

	public void setY(double y) {
		synchronized (lock) {
			this.y = y;
		}
	}

	public void setTheta(double theta) {
		synchronized (lock) {
			this.theta = theta;
		}
	}
}