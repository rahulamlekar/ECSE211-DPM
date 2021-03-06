/*
 * Lab3.java
 * Alessandro Commodari and Asher Wright
 * ECSE 211 DPM Lab 3 - Navigation
 * Group 53
 * This class sets up the main interface for the driver, and sets up the two controllers, one that
 * drives the robot through points, and the other that drives the robto through points while avoiding
 * obstacles.
 */

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;


public class Lab3 {
	// Static Resources:
		// Left motor connected to output A
		// Right motor connected to output D
		private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
		private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
		//private static final EV3ColorSensor colorSensor = new EV3ColorSensor(LocalEV3.get().getPort("B"));
		//private static final EV3ColorSensor colorSensor = new EV3ColorSensor(LocalEV3.get().getPort("S2"));
		private static final EV3UltrasonicSensor uSensor = new EV3UltrasonicSensor(LocalEV3.get().getPort("S1"));
		//Constants
		public static final double WHEEL_RADIUS = 2.1;
		public static final double TRACK = 16.27;

		public static void main(String[] args) {
			int buttonChoice;

			// some objects that need to be instantiated
			//create an array of our positions.
			Position[] positions = new Position[4];
			positions[0] = new Position(60,30,180);
			positions[1] = new Position(30,30,90);
			positions[2] = new Position(30,60,0);
			positions[3] = new Position(60,0,0);
			//different path if we're running the avoider
			Position[] avoiderPositions = new Position[2];
			avoiderPositions[0] = new Position(0,60,0);
			avoiderPositions[1] = new Position(60,0,0);
			//instantiate our objects
			final TextLCD t = LocalEV3.get().getTextLCD();
			Odometer odometer = new Odometer(WHEEL_RADIUS, TRACK, leftMotor, rightMotor);
			final Driver robotDriver = new Driver(odometer, leftMotor, rightMotor, WHEEL_RADIUS, WHEEL_RADIUS, TRACK, positions);
			final DriverAvoiding robotAvoiderDriver = new DriverAvoiding(odometer, leftMotor, rightMotor, WHEEL_RADIUS, WHEEL_RADIUS, TRACK, avoiderPositions);
			OdometryCorrection odometryCorrection = new OdometryCorrection(odometer, null);
			OdometryDisplay odometryDisplay;
			// Setup Ultrasonic Poller	
			@SuppressWarnings("resource")
			UltrasonicPoller usPoller = null;							// the selected controller on each cycle
			SampleProvider usDistance = uSensor.getMode("Distance");	// usDistance provides samples from this instance
			float[] usData = new float[usDistance.sampleSize()];		// usData is the buffer in which data are returned
	
			
			
			
			do {
				// clear the display
				t.clear();

				// ask the user whether the motors should drive in a square or float
				t.drawString("< Left | Right >", 0, 0);
				t.drawString("       |        ", 0, 1);
				t.drawString(" Drive | Drive  ", 0, 2);
				t.drawString("Some   | 'n'    ", 0, 3);
				t.drawString("  where|  avoid ", 0, 4);

				buttonChoice = Button.waitForAnyPress();
			} while (buttonChoice != Button.ID_LEFT
					&& buttonChoice != Button.ID_RIGHT && buttonChoice != Button.ID_DOWN);
			/*
			 * left button means we drive, right button means we drive and avoid
			 */
			if (buttonChoice == Button.ID_LEFT) {
				
				odometryDisplay = new OdometryDisplay(odometer,odometryCorrection,t, robotDriver);
				// start the odometer, the odometry display and (possibly) the
				// odometry correction
				usPoller = new UltrasonicPoller(usDistance, usData, robotDriver);
				odometer.start();
				odometryDisplay.start();
				//odometryCorrection.start();
				robotDriver.start();
				usPoller.start();
				
			} else if (buttonChoice == Button.ID_RIGHT) {
				odometryDisplay = new OdometryDisplay(odometer,odometryCorrection,t, robotAvoiderDriver);
				// start the odometer, the odometry display and (possibly) the
				// odometry correction
				usPoller = new UltrasonicPoller(usDistance, usData, robotAvoiderDriver);
				odometer.start();
				odometryDisplay.start();
				robotAvoiderDriver.start();
				//odometryCorrection.start();
				usPoller.start();
			}else{
				
				
			}
			
			while (Button.waitForAnyPress() != Button.ID_ESCAPE);
			System.exit(0);
		}
}
