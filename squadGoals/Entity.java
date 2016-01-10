package squadGoals;

import squadGoals.Brain;
import battlecode.common.*;
/*
 * Entity contains functions that will be used by multiple types of units
 */
public class Entity {
	
	public static Direction[] directionsToTry(Direction dir){
		Direction[] ret = {dir, dir.rotateRight(), dir.rotateLeft(), dir.rotateRight().rotateRight(), dir.rotateLeft().rotateLeft(),
			dir.rotateRight().rotateRight().rotateRight(), dir.rotateLeft().rotateLeft().rotateLeft().rotateLeft(),
			dir.opposite()};
		return ret;
	}
	
	
	/*
	 * Note that this method doesn't account for ranged robots like ranged zombies unless ranged=true
	 * In the future we should modify this to see if an enemy (mostly zombie) can attack us the turn after
	 * next if we are on weapon cooldown
	 */
	public static boolean inDanger(RobotInfo[] enemies, MapLocation loc, boolean ranged){
		if (ranged){
			for (RobotInfo enemy : enemies){
				if (enemy.location.distanceSquaredTo(loc) < enemy.type.attackRadiusSquared){
					return true;
				}
			}
		} else {
			for (RobotInfo enemy : enemies){
				if (enemy.location.distanceSquaredTo(loc) < 3){
					return true;
				}
			}
		}
		return false;
	}
	
	/*
	 * Move in random direction to avoid enemies
	 */
	public static boolean safeMove(RobotController rc, Brain brain) throws GameActionException{
		if (rc.isCoreReady()){
			RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), rc.getType().sensorRadiusSquared);
			MapLocation robotLocation = rc.getLocation();
			Direction start = awayFromEnemies(rc, enemies, brain);
			Direction[] dirToTry = directionsToTry(start);
			for (int i = 0; i < 8; i ++){
				Direction currentDir = dirToTry[i];
				MapLocation newLoc = robotLocation.add(currentDir);
				if (!inDanger(enemies, newLoc, false) && rc.canMove(currentDir) && !currentDir.isDiagonal()){
					rc.move(currentDir);
					return true;
				}
			}
			for (int i = 0; i < 8; i ++){
				Direction currentDir = dirToTry[i];
				MapLocation newLoc = robotLocation.add(currentDir);
				if (!inDanger(enemies, newLoc, false) && rc.canMove(currentDir)){
					rc.move(currentDir);
					return true;
				}
			}
		}
		return false;
	}
	
	public static Direction awayFromEnemies(RobotController rc, RobotInfo[] enemies, Brain brain){
		//Get closest enemy
		if (enemies.length > 0){
			MapLocation robotLocation = rc.getLocation();
			int distance = 100;
			MapLocation closest = enemies[0].location;
			for (RobotInfo enemy : enemies){
				int seperation = robotLocation.distanceSquaredTo(enemy.location);
				if (seperation < distance){
					distance = seperation;
					closest = enemy.location;
				}
			}
			return robotLocation.directionTo(closest).opposite();
		}
		return directions[brain.rand.nextInt(8)];
	}
	
	public static boolean moveRandomDirection(RobotController rc, Brain brain) throws GameActionException{
		if (rc.isCoreReady()){
			int start = brain.rand.nextInt(8);
			for (int i = start; i < start + 8; i ++){
				Direction dir = directions[i%8];
				if (rc.canMove(dir)){
					rc.move(dir);
					return true;
				}
			}
		}
		return false;
	}
	
	public static int convertMapToSignal(MapLocation loc){
		return (int) (loc.x + 16000 + (loc.y + 16000)*Math.pow(2, 16));
	}
	
	public static MapLocation convertSignalToMap(int signal){
		int x = (int) (signal % Math.pow(2, 16) - 16000);
		int y = (int) (signal / Math.pow(2, 16) - 16000);
		return new MapLocation(x, y);
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
	
	/*
	 * attackHostiles looks for hostile enemies within the current units attack range
	 * and attacks the weakest of them
	 * It returns true if the robot attacked, and false otherwise
	 */
	public static Boolean attackHostiles(RobotController rc) throws GameActionException {
		RobotInfo[] enemiesInAttackRange = Entity.enemiesInRange(rc, rc.getType().attackRadiusSquared);
		Boolean attacked = false;
		if (enemiesInAttackRange.length > 0){
			if (rc.isWeaponReady()){
				RobotInfo weakestSoFar=null;
				double healthOfWeakest=1;
				for (RobotInfo enemy : enemiesInAttackRange){
					double currentHealth = enemy.health/enemy.maxHealth;
					if (weakestSoFar==null || currentHealth < healthOfWeakest) {
						weakestSoFar=enemy;
						healthOfWeakest = currentHealth;
					}
				}
				if (rc.canAttackLocation(weakestSoFar.location)){
					rc.attackLocation(weakestSoFar.location);
				}
				attacked = true;
			}
		}
		return attacked;
	}
	
	public static void moveTowardLocation(RobotController rc, MapLocation loc) throws GameActionException{
		Direction towardLoc = rc.getLocation().directionTo(loc);
		moveInDirection(rc, towardLoc);
	}
	
	public static boolean moveInDirection(RobotController rc, Direction dir) throws GameActionException{
		boolean moved = false;
		if (rc.canMove(dir)){
			moveOrClear(rc, dir);
			moved=true;
		}
		else if (rc.canMove(dir.rotateLeft())){
			moveOrClear(rc, dir.rotateLeft());
			moved=true;
		}
		else if (rc.canMove(dir.rotateRight())){
			moveOrClear(rc, dir.rotateRight());
			moved=true;
		}
		else if (rc.canMove(dir.rotateLeft().rotateLeft())){
			moveOrClear(rc, dir.rotateLeft().rotateLeft());
			moved=true;
		}
		else if (rc.canMove(dir.rotateRight().rotateRight())){
			moveOrClear(rc, dir.rotateRight().rotateRight());
			moved=true;
		}
		return moved;
	}
	public static void moveOrClear(RobotController rc, Direction dirToMove) throws GameActionException{
		if (rc.senseRubble(rc.getLocation().add(dirToMove)) >= GameConstants.RUBBLE_SLOW_THRESH) {
            rc.clearRubble(dirToMove);
        } else if (rc.canMove(dirToMove)) {
            rc.move(dirToMove);
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
					if (!(locMessage==null)){
						MapLocation loc = new MapLocation(locMessage[0], locMessage[1]);
						return loc;
					} 
				}
			}
			return  rc.getLocation();
		}
	}
	
	/*
	 * findAverageOfLocations takes an array of MapLocations, and returns the average, or center point,
	 * of them.
	 */
	public static MapLocation findAverageOfLocations (MapLocation[] locations){
		int sumX = 0;
		int sumY = 0;
		if (locations.length>0){
			for (MapLocation loc : locations){
				sumX+=loc.x;
				sumY+=loc.y;
			}
			int avgX = sumX/locations.length;
			int avgY = sumY/locations.length;
			return new MapLocation(avgX,avgY);
		}
		return new MapLocation(-1,-1);
	}
	
}