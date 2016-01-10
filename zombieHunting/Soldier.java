package zombieHunting;

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
				
				Boolean attack = Entity.attackHostiles(rc);
				if (attack) {
					rc.setIndicatorString(1, "Attacking");
				//Look for dens
				} else if(dens.length > 0 ){
					Entity.moveTowardLocation(rc, dens[0]);
					//is the den still there
					if (rc.canSense(dens[0])){
						RobotInfo maybeDen = rc.senseRobotAtLocation(dens[0]);
						if (maybeDen == null || maybeDen.type != RobotType.ZOMBIEDEN){
							brain.removeDenLocation(dens[0]);
						}
					}
					rc.setIndicatorString(1, "Moving to den at " + aDen.x + ", " + aDen.y);
				} else {
					boolean moved = Entity.moveInDirection(rc, randomDir);
					if (!moved){
						randomDir = Entity.directions[rand.nextInt(8)];
					}
				}
			}
			Clock.yield();
		}
	}
}