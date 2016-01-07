package rovingGangs;

import java.util.Random;

import battlecode.common.*;

public class Soldier {
	
	public static void run(RobotController rc) throws GameActionException{
		
		Random rand = new Random(rc.getID());
		Direction randomDir = Entity.directions[rand.nextInt(8)];
		
		while (true) {
			if (rc.isCoreReady()) {
				RobotInfo[] enemiesInAttackRange = Entity.enemiesInRange(rc, rc.getType().attackRadiusSquared);
				if (enemiesInAttackRange.length > 0 && rc.isWeaponReady()){
					MapLocation enemyLocation = enemiesInAttackRange[0].location;
					rc.attackLocation(enemyLocation);
					rc.setIndicatorString(1, "Attacking enemy");
				} else {
					MapLocation aDen = Entity.listenForMessageLocation(rc);
					//If no den is sensed
					if (rc.getLocation().equals(aDen)){
						
						Entity.moveInDirection(rc, randomDir);
						randomDir = Entity.directions[rand.nextInt(8)];
						rc.setIndicatorString(1, "Moving Random "+ randomDir.toString());
					} else {//if we sense a den
						
						Entity.moveTowardLocation(rc, aDen);
						rc.setIndicatorString(1, "Moving to den at " + aDen.x + ", " + aDen.y);
					}
				}
			}
			Clock.yield();
		}
	}
}