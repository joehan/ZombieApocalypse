package rovingGangs;

import java.util.Random;

import battlecode.common.*;

public class Soldier {
	
	public void run(RobotController rc, Brain brain) throws GameActionException{
		
		Random rand = new Random(rc.getID());
		Direction randomDir = Entity.directions[rand.nextInt(8)];
		
		while (true) {
			if (rc.isCoreReady()) {
				
				MapLocation[] dens = brain.getDenLocations();
				//Listen for messages
				MapLocation aDen = Entity.listenForMessageLocation(rc);
				Boolean heardDen = !(rc.getLocation().equals(aDen));
				//If we hear a new den
				if (heardDen){
					brain.addDenLocation(aDen);
				}
				
				//Look for enemies in attack range
				RobotInfo[] enemiesInAttackRange = Entity.enemiesInRange(rc, rc.getType().attackRadiusSquared);
				if (enemiesInAttackRange.length > 0 && rc.isWeaponReady()){
					MapLocation enemyLocation = enemiesInAttackRange[0].location;
					rc.attackLocation(enemyLocation);
					rc.setIndicatorString(1, "Attacking enemy");
				//Look for dens
				} else if(dens.length > 0 ){
					Entity.moveTowardLocation(rc, aDen);
					rc.setIndicatorString(1, "Moving to den at " + aDen.x + ", " + aDen.y);
				} else {
					Entity.moveInDirection(rc, randomDir);
					randomDir = Entity.directions[rand.nextInt(8)];
				}
			}
			Clock.yield();
		}
	}
}