package squadGoalsWithNeuts;

import java.util.Random;

import battlecode.common.*;

public class Soldier {

	public void run(RobotController rc, Brain brain) throws GameActionException{

		Random rand = new Random(rc.getID());

		while (true) {
			//			if (rc.isCoreReady()) {
			brain.thisTurnsSignals = rc.emptySignalQueue();

			//IF you don't have a squad, get one
			if (brain.getSquadNum() == -1){
				RobotInfo[] allies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam());
				Squad.lookForASquad(rc, brain, allies);
				//Otherwise, listen for squad commands
			} else {
				Squad.findLeaderLocation(rc, brain);
				Squad.listenForCommands(rc, brain);
			}
			/*if (!(brain.goalLocation == null)){
				rc.setIndicatorString(0, brain.goalLocation.toString());
			}*/
			RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), rc.getType().sensorRadiusSquared);
			if (enemies.length > 0 && !(brain.leadersLastKnownLocation == null) 
					&& rc.getLocation().distanceSquaredTo(brain.leadersLastKnownLocation) < 100 && 
					rc.getLocation().distanceSquaredTo(Entity.findClosestEnemy(
							rc, brain, enemies, rc.getLocation()).location) > 13){
				rc.broadcastSignal(rc.getType().sensorRadiusSquared*2);
			}
			boolean inDanger = Entity.inDanger(enemies, rc.getLocation(), false);
			if (inDanger){
				Entity.safeMove(rc, brain, enemies, Direction.NONE, false);
			}
			Boolean attack = Entity.attackHostiles(rc, enemies);
			if (attack) {
				rc.setIndicatorString(1, "Attacking");
			} else if (rc.isCoreReady() && inDanger){
				Entity.moveRandomDirection(rc, brain);
			} else if (rc.isCoreReady() && enemies.length != 0){
				Entity.moveOptimalAttackRange(rc, brain, enemies);
			} else if (brain.goalLocation != null && rc.isCoreReady() && enemies.length == 0){
				Entity.safeMove(rc, brain, enemies, brain.goalLocation, true);
			} else if (rc.isCoreReady() &&
					(brain.leadersLastKnownLocation!= null && 
					rc.getLocation().distanceSquaredTo(brain.leadersLastKnownLocation) > 33)
					&& enemies.length == 0){
				Entity.safeMove(rc, brain, enemies, rc.getLocation().directionTo(brain.leadersLastKnownLocation), true);
			}
			else if (rc.isCoreReady() &&
					(brain.leadersLastKnownLocation!= null && 
					rc.getLocation().distanceSquaredTo(brain.leadersLastKnownLocation) < 15)
					&& enemies.length == 0){
				Entity.safeMove(rc, brain, enemies, Direction.NONE, false);
			}
			Clock.yield();
		}
	}
}