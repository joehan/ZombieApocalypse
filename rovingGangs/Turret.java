package rovingGangs;

import java.util.Random;

import battlecode.common.*;

public class Turret {
	
	public void run(RobotController rc, Brain brain) throws GameActionException{
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