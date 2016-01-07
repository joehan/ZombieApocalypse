package scoutLure;

import battlecode.common.*;
import scoutLure.ScoutJob.*;


/*
 * Entity contains functions that will be used by multiple types of units
 */
public class Entity {
	
	public static void receiveMessages(RobotController rc, BoardLimits boardLimits){
		Signal[] signals = rc.emptySignalQueue();
        for (Signal signal: signals){
        	if (signal.getTeam() == rc.getTeam()){
        		int[] messages = signal.getMessage();
        		switch(messages[0]) {
        		case 0:
        			if ((boardLimits.maxHeight == (Integer) null)){
        				boardLimits.maxHeight = messages[1];
        			}
        			break;
        		
        		case 1:
        			if (boardLimits.minHeight == (Integer) null){
        				boardLimits.minHeight = messages[1];
        			}
        			break;
        		case 2:
        			if (boardLimits.maxWidth == (Integer) null){
        				boardLimits.maxWidth = messages[1];
        			}
        			break;
        		case 3:
        			if (boardLimits.minWidth == (Integer) null){
        				boardLimits.minWidth = messages[1];
        			}
        			break;
        		}
        	}
        }
	}
	
	public static void receiveMessages(RobotController rc, BoardLimits boardLimits, ScoutJob scoutAssign){
		Signal[] signals = rc.emptySignalQueue();
        for (Signal signal: signals){
        	if (signal.getTeam() == rc.getTeam()){
        		int[] messages = signal.getMessage();
        		switch(messages[0]) {
        		case 0:
        			if ((boardLimits.maxHeight == (Integer) null)){
        				boardLimits.maxHeight = messages[1];
        			}
        			break;
        		
        		case 1:
        			if (boardLimits.minHeight == (Integer) null){
        				boardLimits.minHeight = messages[1];
        			}
        			break;
        		case 2:
        			if (boardLimits.maxWidth == (Integer) null){
        				boardLimits.maxWidth = messages[1];
        			}
        			break;
        		case 3:
        			if (boardLimits.minWidth == (Integer) null){
        				boardLimits.minWidth = messages[1];
        			}
        			break;
        		case 4:
        			scoutAssign.haveYScout = true;
        			break;
        		case 5:
        			scoutAssign.haveXScout = true;
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