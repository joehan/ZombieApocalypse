package rushPlayer;

import java.util.Random;

import battlecode.common.*;

public class Turret {
	
	public static void run(RobotController rc) throws GameActionException{
		if (rc.isCoreReady()) {
			RobotInfo[] enemiesInAttackRange = Entity.enemiesInRange(rc, rc.getType().attackRadiusSquared);
			if (enemiesInAttackRange.length > 0){
				if (rc.isWeaponReady()){
					MapLocation enemyLocation = enemiesInAttackRange[0].location;
					rc.attackLocation(enemyLocation);
				}
			}
		}

	}
}