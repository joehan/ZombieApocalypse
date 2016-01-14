package betterScouting;

import battlecode.common.*;

/*
 * Entity contains functions that will be used by multiple types of units
 */
public class Entity {
	
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