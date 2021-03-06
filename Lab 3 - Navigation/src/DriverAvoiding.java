/*
 * Driver_avoiding.java
 * Alessandro Commodari and Asher Wright
 * ECSE 211 DPM Lab 3 - Navigation
 * Group 53
 * This class drives the robot through points, while avoiding obstacles in the way.
 */
import lejos.hardware.Sound;
/*
 * SquareDriver.java
 */
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class DriverAvoiding extends Thread implements UltrasonicController {
	//global variables
	private static final int FORWARD_SPEED = 200;
	private static final int ROTATE_SPEED = 125;
	private Odometer odo;
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	private double leftRadius, rightRadius, width;
	private Position[] positions;
	private int sensorDistance;
	private boolean isDrivingToPosition;
	private int positionIndex;
	Position firstPosition;
	private boolean followingWall;
	private double blockToFinish;
	private double meToFinish;
	//constructor
	public DriverAvoiding(Odometer odo, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
		double leftRadius, double rightRadius, double width, Position[] positions){
		// reset the motors
		for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] { leftMotor, rightMotor }) {
			motor.stop();
			motor.setAcceleration(600);
		}
		this.odo = odo;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.leftRadius = leftRadius;
		this.rightRadius = rightRadius;
		this.width = width;
		this.positions = positions;
		positionIndex = 0;
		isDrivingToPosition = false;
		blockToFinish = 0;
		meToFinish = 0;
		followingWall = false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 * Disclaimer:
	 * This code is not as well layed out as my driver. I didn't have time to clean it up.
	 * As it is now, all of the controlling of the robot occurs in one method.
	 */
	public void run(){
		
		//run this repeatedly 
		while(true){
			if(isDrivingToPosition){ //as long as we are driving to a position
				//if we are done all of our points, laeave the thread.
				if(positionIndex >= positions.length){
					break;
				}
				//some state variables
				boolean avoidedWall = false;
				int movesAfterWall = 0;
				Position currPos = odo.getPositionObject();
				Position finalPos = positions[positionIndex];
				positionIndex++;
				//whether or not we have "tagged/marked" the block
				boolean blockHasNotBeenMarked = true;
				//the blocks x and y positions
				double xBlock = 0;
				double yBlock = 0;
				//how far me and the block are to the finish.
				meToFinish = 0;
				blockToFinish = 0;
				//distance between me and position.
				double deltaY = finalPos.getY()-currPos.getY();
				double deltaX = finalPos.getX()-currPos.getX();
				//System.out.println("our curr position is: " + deltaX + ", " + deltaY);
				double hypotenuse = Math.sqrt(Math.pow(deltaY,2) + Math.pow(deltaX,2));
				double newTheta = Math.atan((double) deltaY/((double) deltaX))*360.0/(2*Math.PI);
				//boolean overshot = false;
				if(deltaX < 0){
					newTheta = 180.0 + newTheta;
				}
				//System.out.println("new theta: " + newTheta);
				double thetaToRotate = newTheta - currPos.getTheta();
				
				if(thetaToRotate > 180){
					thetaToRotate = thetaToRotate-360.0;
				}else if(thetaToRotate < -180){
					thetaToRotate = thetaToRotate+360.0;
				}
				
				//System.out.println("we want to rotate: " + thetaToRotate);
				leftMotor.setSpeed(ROTATE_SPEED);
				rightMotor.setSpeed(ROTATE_SPEED);
				leftMotor.rotate(-convertAngle(leftRadius, width, thetaToRotate), true);
				rightMotor.rotate(convertAngle(rightRadius, width, thetaToRotate), false);
				
				leftMotor.setSpeed(FORWARD_SPEED);
				rightMotor.setSpeed(FORWARD_SPEED);
				boolean reachedPoint = false;
				//here we want a loop, where we're checking if our US distance is too close (in which case we change)
				while(reachedPoint == false){
					if(sensorDistance < 50 && blockHasNotBeenMarked){
						xBlock = sensorDistance*Math.cos(newTheta*2*Math.PI/360.0);
						yBlock = sensorDistance*Math.sin(newTheta*2*Math.PI/360.0);
						blockHasNotBeenMarked = false;
						blockToFinish = Math.sqrt(Math.pow((finalPos.getX()-xBlock), 2) + Math.pow((finalPos.getY()-yBlock), 2));
						Sound.buzz();
					}
					meToFinish = Math.sqrt(Math.pow((finalPos.getX()-odo.getX()), 2) + Math.pow((finalPos.getY()-odo.getY()), 2));
					
	
					//see if we're going to hit a wall eventually!.
					if(sensorDistance < 25 || followingWall == true){
						//Sound.beep();
						//set local distance variable to passed in value
						followingWall = true;
						//if our robot is closer to the wall than the lowest point of the allowed band (bandCenter-bandwidth)
						if(sensorDistance < 26){
							//if we are closer than our closest allowed distance, we will rotate in spot (spin right backwards)
							if(sensorDistance <= 14){
								leftMotor.setSpeed(100);				//set the speed to the lower speed (both motors)
								rightMotor.setSpeed(100);				
								leftMotor.forward();						// Spin left motor forward
								rightMotor.backward();						// spin right motor BACKWARDS. This will rotate in space
							}else{	//if we aren't too close, we just try to realign to get back into our allowed BAND.
								leftMotor.setSpeed(200);				// set left high
								rightMotor.setSpeed(100);				// set right low (this means we'll move to the right, away from wall)
								//move both motors forward (to move robot forward).
								leftMotor.forward();						
								rightMotor.forward();	
							}
						//if our robot is farther than the highest point of the allowed band (bandCenter + bandwidth)
						}else if (sensorDistance > 50){
							leftMotor.setSpeed(120);				// set left low
							rightMotor.setSpeed(160);				// set right high (this will move robot closer to wall)
							//spin both motors forward
							leftMotor.forward();
							rightMotor.forward();
							if(meToFinish + 3< blockToFinish){
								Sound.buzz();
								followingWall = false;
							}
						
						//if our robot is inside the band, we just want to move forward. 
						}else{
							//set both motor speeds to high, 
							leftMotor.setSpeed(200);
							rightMotor.setSpeed(200);
							//spin them (move robot forward).
							leftMotor.forward();
							rightMotor.forward();
						}
						//we have avoided a wall (this will affect how we move)
						avoidedWall = true;
					}else{ 	//if we are passed the block, just get on track/finish...
						//do this only if on track.
						//First, see if we are on track. If so, do what is below
						double correctYPos = deltaY/deltaX*(odo.getX()-firstPosition.getX()) + firstPosition.getY();
						double yError = correctYPos - odo.getY();
						double thetaError = newTheta - odo.getTheta();
						
							/*
							 * Checks to see if we are on track, or if we are above/below where we need to be
							 * and what direction we need to move to get back on track!
							 */
							if(Math.abs(yError) < 0.4){
								if(Math.abs(thetaError) < 4){
									leftMotor.setSpeed(150);
									rightMotor.setSpeed(150);
									leftMotor.forward();
									rightMotor.forward();	
								}else{ //need to then adjust theta.
									leftMotor.setSpeed(80);
									rightMotor.setSpeed(80);
									leftMotor.rotate(-convertAngle(leftRadius, width, thetaError), true);
									rightMotor.rotate(convertAngle(rightRadius, width, thetaError), false);
								}
								
							}else if (yError < 0 && newTheta < 180 && newTheta > 0){ //we're off track to the 
								//do something p-controller-esque.OR just try to get to a position that is back on the line...
								//the second option may be safer... so just barely go around the wall or something.
								
								leftMotor.setSpeed(150);
								rightMotor.setSpeed(50);
								leftMotor.forward();
								rightMotor.forward();
								
							}else if (yError > 0 && newTheta > -180 && newTheta < 0){
								//do something p-controller-esque.OR just try to get to a position that is back on the line...
								//the second option may be safer... so just barely go around the wall or something.
								
								leftMotor.setSpeed(50);
								rightMotor.setSpeed(150);
								leftMotor.forward();
								rightMotor.forward();
								
							}else if (yError > 0 && newTheta < 180 && newTheta > 0){
								/*
								 * if we have recently avoided a wall, then go straight for a bit
								 * Otherwise, correct ourselves.
								 */
								if(avoidedWall){
									try { Thread.sleep(100); } catch(Exception e){}
									leftMotor.setSpeed(100);
									rightMotor.setSpeed(100);
									leftMotor.forward();
									rightMotor.forward();
									movesAfterWall ++;
									if(movesAfterWall == 10){
										movesAfterWall = 0;
										avoidedWall = false;
									}
								}else{
									if(Math.abs(thetaError) > 80){
										leftMotor.setSpeed(50);
										rightMotor.setSpeed(50);
										leftMotor.rotate(-convertAngle(leftRadius, width, thetaError), true);
										rightMotor.rotate(convertAngle(rightRadius, width, thetaError), false);
									}else{
										leftMotor.setSpeed(70);
										rightMotor.setSpeed(180);
										leftMotor.forward();
										rightMotor.forward();
									}
									
								}
								
							}else if (yError < 0 && newTheta > -180 && newTheta <0){
								/*
								 * check to see if we have avoided a wall and if we are below
								 * If we have, go straight for a bit, otherwise get back on track.
								 */
								if(avoidedWall){
									try { Thread.sleep(100); } catch(Exception e){}
									leftMotor.setSpeed(100);
									rightMotor.setSpeed(100);
									leftMotor.forward();
									rightMotor.forward();
									movesAfterWall ++;
									if(movesAfterWall == 10){
										movesAfterWall = 0;
										avoidedWall = false;
									}
								}else{
									/*
									 * If our theta is large, then readjust (so we don't spin in circles).
									 */
									if(Math.abs(thetaError) > 80){
										leftMotor.setSpeed(50);
										rightMotor.setSpeed(50);
										leftMotor.rotate(convertAngle(leftRadius, width, thetaError), true);
										rightMotor.rotate(-convertAngle(rightRadius, width, thetaError), false);
									}else{
										leftMotor.setSpeed(180);
										rightMotor.setSpeed(70);
										leftMotor.forward();
										rightMotor.forward();
									}
									
								}
							
						}
											
						//This checks to see if we made it to our final position!!!
						if (Math.abs(odo.getX() - finalPos.getX()) < 0.5 && Math.abs(odo.getY() - finalPos.getY()) < 0.5){
							leftMotor.stop();
							rightMotor.stop();
							reachedPoint = true;
							break;
						//we are passed it.
						}else if(odo.getY() > finalPos.getY() && odo.getY() > 0 && finalPos.getY() > 0){
							leftMotor.stop();
							rightMotor.stop();
							reachedPoint = true;
							break;
						}
					
						
					}
				
				}
				
				isDrivingToPosition =false;
			}else{
				
			}
			try {
				Thread.sleep(200);											// sleep for 200 mS
			} catch (Exception e) {
				System.out.println("Error: " + e.getMessage());
			}
		}
	}
	//Conversion methods.
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
	//ultrasonic sensor methods
	@Override
	public void processUSData(int distance) {
		// gets the sensor distance and sets to a global variable
		sensorDistance = distance;
		//System.out.println("reading in");
		if(isDrivingToPosition){
			//don't do anything if we are driving to a position already (updating the variable is enough)
			
		}else{	
			firstPosition = odo.getPositionObject();
			isDrivingToPosition = true;
		}
	}
	@Override
	public int readUSDistance() {
		return this.sensorDistance;
	}
	@Override
	public double meToFinish(){
		return meToFinish;
	}
	@Override
	public double blockToFinish(){
		return blockToFinish;
	}

}