package betterScouting;

import battlecode.common.*;

/*
 * Entity contains functions that will be used by multiple types of units
 */
public class Entity {
	
	/*
	 * This method will look at all enemies in enemies, check if there are any archons, and if so
	 * add them to the archon store in brain.
	 */
	public static void addArchonsToBrain(RobotController rc, RobotInfo[] enemies, Brain brain){
		int enemiesLength = enemies.length;
		for (int i = 0; i < enemiesLength; i ++){
			if (enemies[i].type == RobotType.ARCHON){
				brain.addArchon(enemies[i]);
				rc.setIndicatorString(0, "found archon");
			}
		}
	}
	
	public static void moveTowards(RobotController rc, Direction dir) throws GameActionException{
		if (rc.isCoreReady()){
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
	}
	
}