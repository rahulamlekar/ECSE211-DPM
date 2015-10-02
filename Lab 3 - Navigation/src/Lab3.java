import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;


public class Lab3 {
	// Static Resources:
		// Left motor connected to output A
		// Right motor connected to output D
		private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
		private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
		//private static final EV3ColorSensor colorSensor = new EV3ColorSensor(LocalEV3.get().getPort("B"));
		private static final EV3ColorSensor colorSensor = new EV3ColorSensor(LocalEV3.get().getPort("S2"));
		private static final EV3UltrasonicSensor uSensor = new EV3UltrasonicSensor(LocalEV3.get().getPort("S1"));
		//Constants
		public static final double WHEEL_RADIUS = 2.1;
		public static final double TRACK = 16.27;

		public static void main(String[] args) {
			int buttonChoice;

			// some objects that need to be instantiated
			Position[] positions = new Position[2];
			positions[0] = new Position(60,30,180);
			positions[1] = new Position(10,50,90);
			positions[2] = new Position(60,60,0);
			final TextLCD t = LocalEV3.get().getTextLCD();
			Odometer odometer = new Odometer(WHEEL_RADIUS, TRACK, leftMotor, rightMotor);
			final Driver robotDriver = new Driver(odometer, leftMotor, rightMotor, WHEEL_RADIUS, WHEEL_RADIUS, TRACK, positions);
			OdometryCorrection odometryCorrection = new OdometryCorrection(odometer, colorSensor);
			OdometryDisplay odometryDisplay = new OdometryDisplay(odometer,odometryCorrection,t);
			
			
			Position toTravelto = new Position(60,30,0);
			Position currPos = odometer.getPositionObject();
			
			
			do {
				// clear the display
				t.clear();

				// ask the user whether the motors should drive in a square or float
				t.drawString("< Left | Right >", 0, 0);
				t.drawString("       |        ", 0, 1);
				t.drawString(" Float | Drive  ", 0, 2);
				t.drawString("motors | to a   ", 0, 3);
				t.drawString("       |  spot  ", 0, 4);

				buttonChoice = Button.waitForAnyPress();
			} while (buttonChoice != Button.ID_LEFT
					&& buttonChoice != Button.ID_RIGHT);

			if (buttonChoice == Button.ID_LEFT) {
				
				leftMotor.forward();
				leftMotor.flt();
				rightMotor.forward();
				rightMotor.flt();
				
				odometer.start();
				odometryDisplay.start();
				
			} else {
				// start the odometer, the odometry display and (possibly) the
				// odometry correction
				
				odometer.start();
				odometryDisplay.start();
				//odometryCorrection.start();
				robotDriver.start();
				
				// spawn a new Thread to avoid SquareDriver.drive() from blocking
			/*	(new Thread() {
					public void run() {
						robotDriver.driveToPosition(new Position(60,30,180));
					}
			 	}).start();*/
			}
			
			while (Button.waitForAnyPress() != Button.ID_ESCAPE);
			System.exit(0);
		}
}