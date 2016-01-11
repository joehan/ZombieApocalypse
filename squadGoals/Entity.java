package squadGoals;

import java.util.Arrays.*;
import java.util.*;

import org.apache.commons.lang3.ArrayUtils;

import squadGoals.Brain;
import battlecode.common.*;
/*
 * Entity contains functions that will be used by multiple types of units
 */
public class Entity {

	
	public static Direction[] bugDirectionsToTry(Direction dir){
		Direction[] ret = {dir, dir.rotateRight(), dir.rotateRight().rotateRight(), dir.rotateRight().rotateRight().rotateRight(),
				dir.opposite(), dir.opposite().rotateRight(), dir.rotateLeft().rotateLeft(), dir.rotateLeft()};
		return ret;
	}
	
	public static Direction[] directionsToTry(Direction dir){
		Direction[] ret = {dir, dir.rotateRight(), dir.rotateLeft(), dir.rotateRight().rotateRight(), dir.rotateLeft().rotateLeft(),
			dir.rotateRight().rotateRight().rotateRight(), dir.rotateLeft().rotateLeft().rotateLeft(),
			dir.opposite()};
		return ret;
	}
	
	public static void findPartsInRange(RobotController rc, Brain brain, int squaredRange){
		MapLocation[] spacesInRange = MapLocation.getAllMapLocationsWithinRadiusSq(rc.getLocation(), squaredRange);
		for (MapLocation space : spacesInRange){
			if (rc.senseParts(space) > 10 && rc.senseRubble(space)<GameConstants.RUBBLE_OBSTRUCTION_THRESH){
				brain.addPartLocation(space);
			}
		}
	}
	
	/*
	 * Note that this method doesn't account for ranged robots like ranged zombies unless ranged=true
	 * In the future we should modify this to see if an enemy (mostly zombie) can attack us the turn after
	 * next if we are on weapon cooldown
	 */
	public static boolean inDanger(RobotInfo[] enemies, MapLocation loc, boolean ranged){
		if (ranged){
			for (RobotInfo enemy : enemies){
				if (enemy.location.distanceSquaredTo(loc) <= enemy.type.attackRadiusSquared){
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
	
	public static boolean safeMoveOneDirectionRanged(RobotController rc, RobotInfo[] enemies,
			Brain brain, Direction dir) throws GameActionException{
		if (rc.isCoreReady()){
			MapLocation robotLocation = rc.getLocation();
			Direction currentDir = dir;
			MapLocation newLoc = robotLocation.add(currentDir);
			if (!inDanger(enemies, newLoc, true) && rc.canMove(currentDir)){
				rc.move(currentDir);
				return true;
			}
		}
		return false;
	}
	
	public static boolean safeMoveOneDirection(RobotController rc, RobotInfo[] enemies,
			Brain brain, Direction dir) throws GameActionException{
		if (rc.isCoreReady()){
			MapLocation robotLocation = rc.getLocation();
			Direction currentDir = dir;
			MapLocation newLoc = robotLocation.add(currentDir);
			if (!inDanger(enemies, newLoc, false) && rc.canMove(currentDir)){
				rc.move(currentDir);
				return true;
			}
		}
		return false;
	}
	
	public static boolean safeMoveRanged(RobotController rc, Brain brain, RobotInfo[] enemies, Direction dir) throws GameActionException{
		if (rc.isCoreReady()){
			Direction start;
			if (dir == Direction.NONE){
				start = awayFromEnemies(rc, enemies, brain);
			}
			else {
				start = dir;
			}
			MapLocation robotLocation = rc.getLocation();Direction[] dirToTry;
			dirToTry = directionsToTry(start);
			for (int i = 0; i < 8; i ++){
				Direction currentDir = dirToTry[i];
				MapLocation newLoc = robotLocation.add(currentDir);
				if (!inDanger(enemies, newLoc, true) && rc.canMove(currentDir) && !currentDir.isDiagonal()){
					rc.move(currentDir);
					return true;
				}
			}
			for (int i = 0; i < 8; i ++){
				Direction currentDir = dirToTry[i];
				MapLocation newLoc = robotLocation.add(currentDir);
				if (!inDanger(enemies, newLoc, true) && rc.canMove(currentDir)){
					rc.move(currentDir);
					return true;
				}
			}
		}
		return false;
	}
	
	/*
	 * 
	 */
	public static boolean safeMove(RobotController rc, Brain brain, RobotInfo[] enemies,
			Direction dir, boolean bug) throws GameActionException{
		if (rc.isCoreReady()){
			Direction start;
			if (dir == Direction.NONE){
				start = awayFromEnemies(rc, enemies, brain);
			}
			else {
				start = dir;
			}
			MapLocation robotLocation = rc.getLocation();Direction[] dirToTry;
			if (bug){
				dirToTry = bugDirectionsToTry(start);
			}else {
				dirToTry = directionsToTry(start);
			}
			for (int i = 0; i < 8; i ++){
				Direction currentDir = dirToTry[i];
				MapLocation newLoc = robotLocation.add(currentDir);
				if (!inDanger(enemies, newLoc, false) && rc.canMove(currentDir) && !currentDir.isDiagonal()
						&& rc.senseRubble(newLoc) < GameConstants.RUBBLE_SLOW_THRESH){
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
	
	public static boolean safeMove(RobotController rc, Brain brain, RobotInfo[] enemies, 
			MapLocation loc, boolean bug) throws GameActionException{
		if (rc.isCoreReady() && rc.getLocation().distanceSquaredTo(loc) > 8){
			Direction start;
			Direction dir = rc.getLocation().directionTo(loc);
			if (dir == Direction.NONE){
				start = awayFromEnemies(rc, enemies, brain);
			}
			else {
				start = dir;
			}
			MapLocation robotLocation = rc.getLocation();
			Direction[] dirToTry;
			if (bug){
				dirToTry = bugDirectionsToTry(start);
			}else {
				dirToTry = directionsToTry(start);
			}
			for (int i = 0; i < 8; i ++){
				Direction currentDir = dirToTry[i];
				MapLocation newLoc = robotLocation.add(currentDir);
				if (!inDanger(enemies, newLoc, false) && rc.canMove(currentDir) && !currentDir.isDiagonal()
						&& rc.senseRubble(newLoc) < GameConstants.RUBBLE_SLOW_THRESH){
					rc.move(currentDir);
					return true;
				}
			}
			for (int i = 0; i < 8; i ++){
				Direction currentDir = dirToTry[i];
				MapLocation newLoc = robotLocation.add(currentDir);
				if (!inDanger(enemies, newLoc, false) && rc.canMove(currentDir) 
						&& rc.senseRubble(newLoc) < GameConstants.RUBBLE_SLOW_THRESH){
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
	
	public static void searchForDen(RobotController rc, Brain brain) throws GameActionException {
		RobotInfo[] zombiesWithinRange = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, Team.ZOMBIE);
		for (RobotInfo zombie : zombiesWithinRange) {
			if (zombie.type == RobotType.ZOMBIEDEN) {
				if (brain.isDenNew(zombie.location)){
					brain.addDenLocation(zombie.location);
//					Entity.signalMessageLocation(rc, zombie.location, (int) (1.3*rc.getLocation().distanceSquaredTo(brain.getStartingLocation())));
					Squad.shareDenLocation(rc, brain, zombie.location, 
							(int) (1.3*rc.getLocation().distanceSquaredTo(brain.getStartingLocation())));
					rc.setIndicatorString(1, "Found den at " + zombie.location.x + ", " + zombie.location.y);
				}
			}
		}
	}
	
	public static RobotInfo findClosestEnemy(RobotController rc, Brain brain, RobotInfo[] enemies, MapLocation loc){
		int distance = 100;
		RobotInfo closestEnemy = enemies[0];
		for (RobotInfo enemy : enemies){
			int distToEnemy = loc.distanceSquaredTo(enemy.location);
			if (distToEnemy < distance){
				distance = distToEnemy;
				closestEnemy = enemy;
			}
		}
		return closestEnemy;
	}
	
	
	
	public static boolean moveOptimalAttackRange(RobotController rc, Brain brain, RobotInfo[] enemies) throws GameActionException{
		if (rc.isCoreReady()){
			int maxAttackRange = rc.getType().attackRadiusSquared;
			MapLocation robotLocation = rc.getLocation();
			RobotInfo enemy = Entity.findClosestEnemy(rc, brain, enemies, robotLocation);
			MapLocation enemyLoc = enemy.location;
			Direction dirToEnemy = robotLocation.directionTo(enemyLoc);
			Direction[] dirToTri = directionsToTry(dirToEnemy.opposite());
			if (robotLocation.distanceSquaredTo(enemyLoc) < 8 || (robotLocation.distanceSquaredTo(enemyLoc) > 13 &&
					(enemy.coreDelay > 2.0 || !(enemy.team == Team.ZOMBIE) || enemy.type == RobotType.ZOMBIEDEN))){
				for (Direction dir : dirToTri){
					if (!dir.isDiagonal() && rc.senseRubble(robotLocation.add(dir)) < GameConstants.RUBBLE_SLOW_THRESH){
						int moveDistToEnemy = robotLocation.add(dir).distanceSquaredTo(enemyLoc);
						if (moveDistToEnemy <= maxAttackRange && moveDistToEnemy >= 8 ){
							boolean moved = Entity.safeMoveOneDirection(rc, enemies, brain, dir);
							if (moved){
								return true;
							}
						}
					}
				}
				for (Direction dir : dirToTri){
					if (rc.senseRubble(robotLocation.add(dir)) < GameConstants.RUBBLE_SLOW_THRESH && dir.isDiagonal()){
						int moveDistToEnemy = robotLocation.add(dir).distanceSquaredTo(enemyLoc);
						if (moveDistToEnemy <= maxAttackRange && moveDistToEnemy >= 8 ){
							boolean moved = Entity.safeMoveOneDirection(rc, enemies, brain, dir);
							if (moved){
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
	
	
	public static void updateDenLocations(RobotController rc, Brain brain) throws GameActionException {
		for (MapLocation den : brain.getDenLocations()){
			if (rc.canSenseLocation(den)){
				RobotInfo maybeDen = rc.senseRobotAtLocation(den);
				if (maybeDen == null || maybeDen.type!=RobotType.ZOMBIEDEN){
					brain.removeDenLocation(den);
					if (brain.goalLocation.equals(den)){
						brain.goalLocation = null;
					}
				}
			}
		}
		searchForDen(rc, brain);
		
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
	
	public static RobotType[] orderToAttack = {RobotType.VIPER, RobotType.TURRET, RobotType.TTM,
		RobotType.SOLDIER, RobotType.GUARD, RobotType.ARCHON, RobotType.RANGEDZOMBIE, RobotType.FASTZOMBIE, 
		RobotType.BIGZOMBIE, RobotType.STANDARDZOMBIE, RobotType.SCOUT, RobotType.ZOMBIEDEN};
	
	
	/*
	 * attackHostiles looks for hostile enemies within the current units attack range
	 * and attacks the weakest of them
	 * It returns true if the robot attacked, and false otherwise
	 */
	public static Boolean attackHostiles(RobotController rc, RobotInfo[] enemiesInAttackRange) throws GameActionException {
//		Boolean attacked = false;
		if (enemiesInAttackRange.length > 0){
			if (rc.isWeaponReady()){
				RobotInfo weakestSoFar=null;
				double healthOfWeakest=1;
				for (int i = 0; i < orderToAttack.length; i ++){
					for (RobotInfo enemy : enemiesInAttackRange){
						double currentHealth = enemy.health/enemy.maxHealth;
						if ((weakestSoFar==null || currentHealth < healthOfWeakest) && enemy.type == orderToAttack[i]
								&& rc.canAttackLocation(enemy.location)){
							weakestSoFar=enemy;
							healthOfWeakest = currentHealth;
						}
					}
					if (!(weakestSoFar == null)){
						rc.attackLocation(weakestSoFar.location);
						return true;
					}
				}
//				attacked = true;
			}
		}
		return false;
	}
	
	public static void moveTowardLocation(RobotController rc, MapLocation loc) throws GameActionException{
		Direction towardLoc = rc.getLocation().directionTo(loc);
		if (rc.getLocation().distanceSquaredTo(loc) > 8){
			moveTowards(rc, towardLoc);
		}
	}
	
	public static void moveToLocation(RobotController rc, MapLocation loc) throws GameActionException{
		Direction towardLoc = rc.getLocation().directionTo(loc);
		if (rc.getLocation() != loc) {
			moveTowards(rc, towardLoc);
		}
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
	
	public static void signalMessageLocation(RobotController rc, MapLocation loc, int distance) throws GameActionException {
		rc.broadcastMessageSignal(loc.x, loc.y, distance);
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