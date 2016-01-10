package squadGoals;

import java.util.Random;

import battlecode.common.*;

public class Soldier {
	
	public void run(RobotController rc, Brain brain) throws GameActionException{
		
		Random rand = new Random(rc.getID());
		Direction randomDir = Entity.directions[rand.nextInt(8)];
		
		while (true) {
			if (rc.isCoreReady()) {
				
//				MapLocation[] dens = brain.getDenLocations();
//				//Listen for messages
//				MapLocation aDen = Entity.listenForMessageLocation(rc);
//				Boolean heardDen = !(rc.getLocation().equals(aDen));
//				//If we hear a new den
//				if (heardDen){
//					brain.addDenLocation(aDen);
//				}
				
				//IF you don't have a squad, get one
				if (brain.getSquadNum() == -1){
					Squad.lookForASquad(rc, brain);
				//Otherwise, listen for squad commands
				} else {
					Squad.listenForCommands(rc, brain);
				}
				Boolean attack = Entity.attackHostiles(rc);
				if (attack) {
					rc.setIndicatorString(1, "Attacking");
				} else if (brain.goalLocation != null){
					Entity.moveTowardLocation(rc, brain.goalLocation);
//					boolean moved = Entity.moveInDirection(rc, randomDir);
//					if (!moved){
//						randomDir = Entity.directions[rand.nextInt(8)];
//					}
				}
			}
			Clock.yield();
		}
	}
}