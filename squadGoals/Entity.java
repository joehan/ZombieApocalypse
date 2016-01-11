package squadGoals;

import java.util.Arrays;

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
	
	public static boolean safeMoveOneDirection(RobotController rc, Brain brain, Direction dir) throws GameActionException{
		if (rc.isCoreReady()){
			MapLocation robotLocation = rc.getLocation();
			RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), rc.getType().sensorRadiusSquared);
			Direction currentDir = dir;
			MapLocation newLoc = robotLocation.add(currentDir);
			if (!inDanger(enemies, newLoc, false) && rc.canMove(currentDir) && !currentDir.isDiagonal()){
				rc.move(currentDir);
				return true;
			}
			if (!inDanger(enemies, newLoc, false) && rc.canMove(currentDir)
					&& rc.senseRubble(newLoc) > GameConstants.RUBBLE_SLOW_THRESH){
				rc.move(currentDir);
				return true;
			}
		}
		return false;
	}
	
	/*
	 * 
	 */
	public static boolean safeMove(RobotController rc, Brain brain, Direction dir, boolean bug) throws GameActionException{
		if (dir == Direction.NONE){
			return true;
		}
		if (rc.isCoreReady()){
			Direction start;
			RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), rc.getType().sensorRadiusSquared);
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
						&& rc.senseRubble(newLoc) > GameConstants.RUBBLE_SLOW_THRESH){
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
	
	public static boolean safeMove(RobotController rc, Brain brain, MapLocation loc, boolean bug) throws GameActionException{
		if (rc.isCoreReady() && rc.getLocation().distanceSquaredTo(loc) > 8){
			Direction start;
			Direction dir = rc.getLocation().directionTo(loc);
			RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), rc.getType().sensorRadiusSquared);
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
						&& rc.senseRubble(newLoc) > GameConstants.RUBBLE_SLOW_THRESH){
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
	
	public static void searchForDen(RobotController rc, Brain brain) {
		RobotInfo[] zombiesWithinRange = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, Team.ZOMBIE);
		for (RobotInfo zombie : zombiesWithinRange) {
			if (zombie.type == RobotType.ZOMBIEDEN) {
				brain.addDenLocation(zombie.location);
			}
		}
	}
	
	public static RobotInfo findClosestEnemy(RobotController rc, Brain brain, RobotInfo[] enemies){
		int distance = 100;
		MapLocation robotLocation = rc.getLocation();
		RobotInfo closestEnemy = enemies[0];
		for (RobotInfo enemy : enemies){
			if (robotLocation.distanceSquaredTo(enemy.location) < distance){
				distance = robotLocation.distanceSquaredTo(enemy.location);
				closestEnemy = enemy;
			}
		}
		return closestEnemy;
	}
	
	public static boolean moveOptimalAttackRange(RobotController rc, Brain brain, RobotInfo[] enemies) throws GameActionException{
		if (rc.isCoreReady()){
			int maxAttackRange = rc.getType().attackRadiusSquared;
			MapLocation robotLocation = rc.getLocation();
			RobotInfo closestEnemy = findClosestEnemy(rc, brain, enemies);
			Direction dirToEnemy = robotLocation.directionTo(closestEnemy.location);
			Direction[] dirToTri = directionsToTry(dirToEnemy);
			Direction[] directions = new Direction[9];
			System.arraycopy(dirToTri, 0, directions, 0, 8);
			directions[8] = Direction.NONE;
			int[] attackRadiusForDirection = new int[9];
			for (int i = 0; i < 9; i ++){
				Direction newDir = directions[i];
				attackRadiusForDirection[i] = robotLocation.add(newDir).distanceSquaredTo(closestEnemy.location);
			}
			int[] copyAttackRadius = attackRadiusForDirection.clone();
			Arrays.sort(copyAttackRadius);
			for (int j = 8; j > -1; j --){
				for (int i = 0; i < 9; i ++){
					if (attackRadiusForDirection[i] == copyAttackRadius[j] 
							&& attackRadiusForDirection[i] <= maxAttackRange){
						boolean moved = Entity.safeMoveOneDirection(rc, brain, directions[i]);
						if (moved){
							return true;
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
		RobotType.STANDARDZOMBIE, RobotType.BIGZOMBIE, RobotType.SCOUT, RobotType.ZOMBIEDEN};
	
	
	/*
	 * attackHostiles looks for hostile enemies within the current units attack range
	 * and attacks the weakest of them
	 * It returns true if the robot attacked, and false otherwise
	 */
	public static Boolean attackHostiles(RobotController rc) throws GameActionException {
		RobotInfo[] enemiesInAttackRange = Entity.enemiesInRange(rc, rc.getType().attackRadiusSquared);
//		Boolean attacked = false;
		if (enemiesInAttackRange.length > 0){
			if (rc.isWeaponReady()){
				for (int i = 0; i < orderToAttack.length; i ++){
					RobotInfo weakestSoFar=null;
					double healthOfWeakest=1;
					for (RobotInfo enemy : enemiesInAttackRange){
						double currentHealth = enemy.health/enemy.maxHealth;
						if ((weakestSoFar==null || currentHealth < healthOfWeakest) && enemy.type == orderToAttack[i]){
							weakestSoFar=enemy;
							healthOfWeakest = currentHealth;
						}
					}
					if (!(weakestSoFar == null) && rc.canAttackLocation(weakestSoFar.location)){
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