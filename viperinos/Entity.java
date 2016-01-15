package viperinos;

import battlecode.common.*;
/*
 * Entity contains functions that will be used by multiple types of units
 */
public class Entity {
	
	public static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
            Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	
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
	 *move attempts to move in the closest possible direction to dir.
	 *Returns true if the robot moved, and false otherwise
	 */
	public static boolean move(RobotController rc, Brain brain, Direction dir) throws GameActionException{
		Direction[] directionsToTry = {dir, dir.rotateLeft(), dir.rotateRight(),
				dir.rotateLeft().rotateLeft(), dir.rotateRight().rotateRight(),
				dir.opposite().rotateRight(), dir.opposite().rotateLeft(),
				dir.opposite()};
		for (Direction d : directionsToTry){
			if (rc.canMove(d)){
				rc.move(d);
				brain.lastDirectionMoved = d;
				return true;
			}
		}
		return false;
	}
	
}