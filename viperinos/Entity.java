package viperinos;

import battlecode.common.*;
/*
 * Entity contains functions that will be used by multiple types of units
 */
public class Entity {
	
	public static boolean canSenseArchon(RobotController rc, RobotInfo[] allies){
		int alliesLength = 0;
		for (int i = 0; i < alliesLength; i++){
			if (allies[i].type == RobotType.ARCHON){
				return true;
			}
		}
		return false;
	}
	
	public static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
            Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	
	public static RobotInfo[] concat(RobotInfo[] a, RobotInfo[] b) {
	   int aLen = a.length;
	   int bLen = b.length;
	   RobotInfo[] c= new RobotInfo[aLen+bLen];
	   System.arraycopy(a, 0, c, 0, aLen);
	   System.arraycopy(b, 0, c, aLen, bLen);
	   return c;
	}
	
	/*
	 * findClosestHostile takes a array of enemies and a array of zombies,
	 * and returns the closest robot in the two arrays
	 */
	public static RobotInfo findClosestHostile(RobotController rc, RobotInfo[] enemies, RobotInfo[] zombies){
		RobotInfo closestHostile = null;
		int distanceToClosest = 50000;
		for (RobotInfo enemy : enemies){
			int distanceToEnemy = rc.getLocation().distanceSquaredTo(enemy.location);
			if (distanceToEnemy < distanceToClosest){
				closestHostile = enemy;
				distanceToClosest = distanceToEnemy;
			}
		}
		for (RobotInfo zombie : zombies){
			int distanceToZombie = rc.getLocation().distanceSquaredTo(zombie.location);
			if (distanceToZombie < distanceToClosest){
				closestHostile = zombie;
				distanceToClosest = distanceToZombie;
			}
		}
		return closestHostile;
	}
	
	/*
	 *move attempts to move in dir.
	 *if Bug is try, it uses bug movement. OTherwise, it uses closest direction movement.
	 *Returns true if the robot moved, and false otherwise
	 */
	public static boolean move(RobotController rc, Brain brain, Direction dir, boolean bug) throws GameActionException{
		Direction[] directionsToTry;
		if(bug){
			Direction[] bugDirections = {dir, dir.rotateLeft(), dir.rotateLeft().rotateLeft(),
					dir.opposite().rotateRight(), dir.opposite(), dir.opposite().rotateLeft(),
					dir.rotateRight().rotateRight(), dir.rotateRight()};
			directionsToTry = bugDirections;
		} else {
			Direction[] normalDirections = {dir, dir.rotateLeft(), dir.rotateRight(),
					dir.rotateLeft().rotateLeft(), dir.rotateRight().rotateRight(),
					dir.opposite().rotateRight(), dir.opposite().rotateLeft(),
					dir.opposite()};
			directionsToTry = normalDirections;
		}
		for (Direction d : directionsToTry){
			if (rc.canMove(d)){
				rc.move(d);
				brain.lastDirectionMoved = d;
				return true;
			}
		}
		return false;
	}
	
	/*
	 * fleeEnemies looks for nearby enemies, and runs away from the closest one.
	 * It returns true if the robot moved, and false otherwise
	 */
	public static boolean fleeEnemies(RobotController rc, Brain brain, RobotInfo[] enemies, RobotInfo[] zombies, RobotInfo closestEnemy) throws GameActionException{
		boolean moved = false;
		if (enemies.length > 0 || zombies.length > 0){
			move(rc, brain, closestEnemy.location.directionTo(rc.getLocation()), false);
			moved = true;
		}
		return moved;
	}
	
	public static boolean digInDirection(RobotController rc, Brain brain, Direction dir) throws GameActionException{
		if (rc.isCoreReady()){
			Direction[] dirToTry = directionsToTry(dir);
			for (Direction direction : dirToTry){
				if (rc.senseRubble(rc.getLocation().add(direction)) > 0){
					rc.clearRubble(direction);
					return true;
				}
			}
		}
		return false;
	}
	
	
	public static boolean moveOptimalAttackRange(RobotController rc, Brain brain, RobotInfo[] enemies, RobotInfo enemy)
			throws GameActionException{
		if (rc.isCoreReady()){
			int maxAttackRange = rc.getType().attackRadiusSquared;
			MapLocation robotLocation = rc.getLocation();
//			RobotInfo enemy = Entity.findClosestEnemy(rc, brain, enemies, robotLocation);
			MapLocation enemyLoc = enemy.location;
			Direction dirToEnemy = robotLocation.directionTo(enemyLoc);
			Direction[] dirToTri = directionsToTry(dirToEnemy.opposite());
			int currentDistance = robotLocation.distanceSquaredTo(enemyLoc);
			if (robotLocation.distanceSquaredTo(enemyLoc) < 8 || (robotLocation.distanceSquaredTo(enemyLoc) > 13)){
				for (Direction dir : dirToTri){
					if (rc.senseRubble(robotLocation.add(dir)) < GameConstants.RUBBLE_SLOW_THRESH){
						int moveDistToEnemy = robotLocation.add(dir).distanceSquaredTo(enemyLoc);
						if (moveDistToEnemy <= maxAttackRange && 
								(moveDistToEnemy >= currentDistance || currentDistance > maxAttackRange)){
							boolean moved = Entity.safeMoveOneDirection(rc, enemies, brain, dir);
							if (moved){
								return true;
							}
						}
					}
				}
				for (Direction dir : dirToTri){
					if (rc.senseRubble(robotLocation.add(dir)) >= GameConstants.RUBBLE_SLOW_THRESH){
						int moveDistToEnemy = robotLocation.add(dir).distanceSquaredTo(enemyLoc);
						if (moveDistToEnemy <= maxAttackRange && 
								(moveDistToEnemy >= currentDistance || currentDistance > maxAttackRange)){
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
	
	public static Direction[] directionsToTry(Direction dir){
		Direction[] ret = {dir, dir.rotateRight(), dir.rotateLeft(), dir.rotateRight().rotateRight(), dir.rotateLeft().rotateLeft(),
			dir.rotateRight().rotateRight().rotateRight(), dir.rotateLeft().rotateLeft().rotateLeft(),
			dir.opposite()};
		return ret;
	}
	
	public static boolean safeMove(RobotController rc, RobotInfo[] enemies, Brain brain, Direction dir
			) throws GameActionException{
		Direction[] dirToTry = directionsToTry(dir);
		for (int i = 0; i < 8; i++){
			Direction currentDir = dirToTry[i];
			MapLocation newLoc = rc.getLocation().add(currentDir);
			if (rc.canMove(currentDir) && rc.senseRubble(newLoc) < GameConstants.RUBBLE_SLOW_THRESH &&
					!currentDir.isDiagonal()){
				boolean valid = true;
				for (int j = enemies.length; --j >= 0;){
					RobotInfo currentEnemy = enemies[j];
					if (newLoc.distanceSquaredTo(currentEnemy.location) <= currentEnemy.type.attackRadiusSquared){
						valid = false;
						break;
					}
				}
				if (valid){
					rc.move(currentDir);
					return true;
				}
			}
		}
		for (int i = 0; i < 8 ; i++){
			Direction currentDir = dirToTry[i];
			MapLocation newLoc = rc.getLocation().add(currentDir);
			if (rc.canMove(currentDir) && rc.senseRubble(newLoc) < GameConstants.RUBBLE_SLOW_THRESH){
				boolean valid = true;
				for (int j = enemies.length; --j >= 0;){
					RobotInfo currentEnemy = enemies[j];
					if (newLoc.distanceSquaredTo(currentEnemy.location) <= currentEnemy.type.attackRadiusSquared){
						valid = false;
						break;
					}
				}
				if (valid){
					rc.move(currentDir);
					return true;
				}
			}
		}
		for (int i = 0; i < 8 ; i++){
			Direction currentDir = dirToTry[i];
			MapLocation newLoc = rc.getLocation().add(currentDir);
			if (rc.canMove(currentDir)){
				boolean valid = true;
				for (int j = enemies.length; --j >= 0;){
					RobotInfo currentEnemy = enemies[j];
					if (newLoc.distanceSquaredTo(currentEnemy.location) <= currentEnemy.type.attackRadiusSquared){
						valid = false;
						break;
					}
				}
				if (valid){
					rc.move(currentDir);
					return true;
				}
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
	
	/*
	 * trackDens updates the lists of living and dead dens in brain
	 */
	public static void trackDens(RobotController rc, Brain brain, RobotInfo[] zombies) throws GameActionException{
		for (MapLocation denLocation : brain.denLocations){
			if (rc.canSenseLocation(denLocation) && !brain.isDenDead(denLocation)){
				RobotInfo maybeDen = rc.senseRobotAtLocation(denLocation);
				if (maybeDen == null || maybeDen.type  != RobotType.ZOMBIEDEN){
					brain.addDeadDenLocation(denLocation);
				}
			}
		}
		for (RobotInfo zombie:zombies){
			if (zombie.type==RobotType.ZOMBIEDEN && !brain.isDenKnown(zombie.location)){
				brain.addDenLocation(zombie.location);
			}
		}
	}
}