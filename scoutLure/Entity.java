package scoutLure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import battlecode.common.*;

import java.util.Comparator;


/*
 * Entity contains functions that will be used by multiple types of units
 */
public class Entity {
	
	public static int convertMapToSignal(MapLocation loc){
		return (int) (loc.x + loc.y*Math.pow(2, 16));
	}
	
	public static MapLocation convertSignalToMap(int signal){
		int x = (int) (signal % Math.pow(2, 16));
		int y = (int) (signal / Math.pow(2, 16));
		return new MapLocation(x, y);
	}
	
	/*
	 * returns an array with the closest n locations to the givenLoc
	 * Note: this is untested code. Test before you use it
	 */
	public static MapLocation[] sortByDistance(final MapLocation loc, MapLocation[] otherLocs){
		ArrayList<MapLocation> returnList = new ArrayList<MapLocation>(Arrays.asList(otherLocs));
		Collections.sort(returnList, new Comparator<MapLocation>(){
		     public int compare(MapLocation o1, MapLocation o2){
		         return o1.distanceSquaredTo(loc) - o2.distanceSquaredTo(loc);
		     }
		});
		return (MapLocation[]) returnList.toArray();
	}
	
	public static MapLocation split(MapLocation loc1, MapLocation loc2){
		return new MapLocation((loc1.x + loc2.x)/2, (loc1.y + loc2.y)/2);
	}

	public static Direction moveSemiRandom(RobotController rc, Direction currentDir) throws GameActionException{
		if (rc.canMove(currentDir) && rc.isCoreReady()){
			rc.move(currentDir);
			return currentDir;
		}
		if (!rc.canMove(currentDir)){
			Random rand = new Random(rc.getID()+ rc.getRoundNum());
			int num = rand.nextInt(8);
			Direction orig = directions[num];
			num += 1;
			Direction dirToMove = directions[num%8];

			while (!rc.canMove(dirToMove) && dirToMove != orig){
				num += 1;
				dirToMove = directions[num%8];
			}
			if (rc.canMove(dirToMove) && rc.isCoreReady()){
				rc.move(dirToMove);
			}
			currentDir = dirToMove;
		}
		return currentDir;      
	}
	
	public static int calculatePower(MapLocation robotLocation, int sensorRange, MapLocation targetLocation){
		int distance = robotLocation.distanceSquaredTo(targetLocation);
//		System.out.println(distance);
		int ret = Math.max(1, (int) Math.ceil(distance));
		return ret;
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
        				rc.setIndicatorString(0, "received maxWidth data");
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
        		case 6:
        			brain.enemyBaseFound = true;
        			int x = (int) (messages[1] % Math.pow(2, 16));
        			int y = (int) (messages[1] / Math.pow(2, 16));
        			brain.enemyBase = new MapLocation(x, y);
        			break;
        		case 7:
        			MapLocation newLoc = new MapLocation((int) (messages[1]%Math.pow(2, 16)), (int) (messages[1]/Math.pow(2, 16)));
        			brain.denLocations.add(newLoc);
        			break;
        		//Guard zombie base
        		case 8:
        			MapLocation denLoc = new MapLocation((int) (messages[1]%Math.pow(2, 16)), (int) (messages[1]/Math.pow(2, 16)));
        			brain.denGuarded.add(denLoc);
        			break;
        		case 9:
        			MapLocation denNotGuard = new MapLocation((int) (messages[1]%Math.pow(2, 16)), (int) (messages[1]/Math.pow(2, 16)));
        			brain.denGuarded.remove(denNotGuard);
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