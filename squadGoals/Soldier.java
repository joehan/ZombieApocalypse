package squadGoals;

import java.util.Random;

import battlecode.common.*;

public class Soldier {

	public void run(RobotController rc, Brain brain) throws GameActionException{

		Random rand = new Random(rc.getID());
		Direction randomDir = Entity.directions[rand.nextInt(8)];


		while (true) {
			//			if (rc.isCoreReady()) {
			brain.thisTurnsSignals = rc.emptySignalQueue();

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
				Squad.findLeaderLocation(rc, brain);
				Squad.listenForCommands(rc, brain);
			}
			RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), rc.getType().sensorRadiusSquared);
//			RobotInfo[] allies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam());
			boolean moved = false;
			if (Entity.inDanger(enemies, rc.getLocation(), false)){
				moved = Entity.safeMove(rc, brain, Direction.NONE);
				if (!moved){
					Entity.moveRandomDirection(rc, brain);
				}
			}
			Boolean attack = Entity.attackHostiles(rc);
			if (attack) {
				rc.setIndicatorString(1, "Attacking");
			} else if (brain.goalLocation != null && rc.isCoreReady()){
				Entity.moveTowardLocation(rc, brain.goalLocation);
				//					boolean moved = Entity.moveInDirection(rc, randomDir);
				//					if (!moved){
				//						randomDir = Entity.directions[rand.nextInt(8)];
				//					}
			} else if (rc.isCoreReady() &&
					(brain.leadersLastKnownLocation!= null && 
					rc.getLocation().distanceSquaredTo(brain.leadersLastKnownLocation) > 33)){
				Entity.safeMove(rc, brain, rc.getLocation().directionTo(brain.leadersLastKnownLocation));
			}
			else if (rc.isCoreReady() &&
					(brain.leadersLastKnownLocation!= null && 
					rc.getLocation().distanceSquaredTo(brain.leadersLastKnownLocation) < 15)){
				Entity.safeMove(rc, brain, Direction.NONE);
			}
//			else if (rc.isCoreReady() && (brain.leadersLastKnownLocation!= null && 
//					rc.getLocation().distanceSquaredTo(brain.leadersLastKnownLocation) > 8)))
			Clock.yield();
		}
	}
}
