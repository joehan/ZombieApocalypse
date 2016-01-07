package scoutLure;

import battlecode.common.*;


/*
 * Entity contains functions that will be used by multiple types of units
 */
public class Entity {
	
	public static int calculatePower(MapLocation robotLocation, int sensorRange, MapLocation targetLocation){
		int distance = robotLocation.distanceSquaredTo(targetLocation);
		return Math.max(10, (int) Math.ceil(((distance-2*sensorRange)/2)));
	}
	
	public static void moveTowards(RobotController rc, Direction dir){
		if (rc.isCoreReady()){
			try {
				if (rc.canMove(dir)){
					rc.move(dir);
				}
				else if (rc.canMove(dir.rotateLeft())){
					rc.move(dir.rotateLeft());
				}
				else if (rc.canMove(dir.rotateRight())){
					rc.move(dir.rotateRight());
				}
				else if (rc.canMove(dir.rotateLeft().rotateLeft())){
					rc.move(dir.rotateLeft().rotateLeft());
				}
				else if (rc.canMove(dir.rotateRight().rotateRight())){
					rc.move(dir.rotateRight().rotateRight());
				}
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();

			}
		}
	}
	
	public static void receiveMessages(RobotController rc, Brain brain){
		Signal[] signals = rc.emptySignalQueue();
        for (Signal signal: signals){
        	if (signal.getTeam() == rc.getTeam()){
        		int[] messages = signal.getMessage();
        		switch(messages[0]) {
        		case 0:
        			if ((brain.maxHeight == (Integer) null)){
        				brain.maxHeight = messages[1];
        			}
        			break;
        		
        		case 1:
        			if ((brain.minHeight == (Integer) null)){
        				brain.minHeight = messages[1];
        			}
        			break;
        		case 2:
        			if ((brain.maxWidth == (Integer) null)){
        				brain.maxWidth = messages[1];
        			}
        			break;
        		case 3:
        			if ((brain.minWidth == (Integer) null)){
        				brain.minWidth = messages[1];
        			}
        			break;
        		case 4:
        			brain.haveYScout = true;
        			break;
        		case 5:
        			brain.haveXScout = true;
        			break;
        		}
        		
        	}
		
		}
	}
	
	public static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
            Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	
	public static MapLocation searchForDen(RobotController rc) {
		RobotInfo[] zombiesWithinRange = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, Team.ZOMBIE);
		MapLocation target;
		for (RobotInfo zombie : zombiesWithinRange) {
			if (zombie.type == RobotType.ZOMBIEDEN) {
				return zombie.location;
			}
		}
		return rc.getLocation();
	}
	
	public static RobotInfo[] enemiesInRange(RobotController rc, int squaredRange) {
		
		RobotInfo[] enemiesWithinRange = rc.senseNearbyRobots(squaredRange, rc.getTeam().opponent());
		RobotInfo[] zombiesWithinRange = rc.senseNearbyRobots(squaredRange, Team.ZOMBIE);
		int eLen = enemiesWithinRange.length;
		int zLen = zombiesWithinRange.length;
		
		RobotInfo[] combined = new RobotInfo[eLen+zLen];
		System.arraycopy(enemiesWithinRange, 0, combined, 0, eLen);
	    System.arraycopy(zombiesWithinRange, 0, combined, eLen, zLen);
	    
	    return combined;
	}
	
	public static void moveTowardLocation(RobotController rc, MapLocation loc) throws GameActionException{
		Direction towardLoc = rc.getLocation().directionTo(loc);
		moveInDirection(rc, towardLoc);
	}
	
	public static void moveInDirection(RobotController rc, Direction dirToMove) throws GameActionException{
		for (int i=0; i<8; i++){
			if (rc.senseRubble(rc.getLocation().add(dirToMove)) >= GameConstants.RUBBLE_SLOW_THRESH) {
	            rc.clearRubble(dirToMove);
	            break;
	        } else if (rc.canMove(dirToMove)) {
	            rc.move(dirToMove);
	            rc.setIndicatorString(2, "Moving in direction "+ dirToMove.toString());
	            break;
	        } else {
//	        	dirToMove.rotateLeft();
//	        	rc.setIndicatorString(2, "rotate "+ dirToMove.toString());
	        	break;
	        }
		}
	}
	
//	public static void signalMessageLocation(RobotController rc, MapLocation loc) throws GameActionException{
//		rc.broadcastMessageSignal(loc.x, loc.y, )
//	}
}