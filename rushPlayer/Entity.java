package rushPlayer;

import battlecode.common.*;
/*
 * Entity contains functions that will be used by multiple types of units
 */
public class Entity {
	
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
	
	public static MapLocation searchForArchon(RobotController rc) {
		Team oppTeam = rc.getTeam().opponent();
		RobotInfo[] otherTeamWithinRange = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, oppTeam);
		MapLocation target;
		for (RobotInfo opp : otherTeamWithinRange) {
			if (opp.type == RobotType.ARCHON) {
				return opp.location;
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
	
	public static void signalMessageLocation(RobotController rc, MapLocation loc) throws GameActionException {
		rc.broadcastMessageSignal(loc.x, loc.y, 900);
	}
	
	/*
	 * listenForMessageLocation listens to the message queue, looking for location messages sent by the friendly team.
	 * If it hears one, it returns the MapLocation described in the message
	 * Otherwise, it returns the location of the current robot
	 */
	public static MapLocation listenForMessageLocation(RobotController rc) throws GameActionException {
		Signal[] messages = rc.emptySignalQueue();
		if (messages.length == 0){
			return rc.getLocation();
		} else {
			for (Signal message : messages) {
				if (message.getTeam() == rc.getTeam()){
					int[] locMessage = messages[0].getMessage();
					MapLocation loc = new MapLocation(locMessage[0], locMessage[1]);
					return loc;
				} 
			}
			return  rc.getLocation();
		}
	}
	
	
}