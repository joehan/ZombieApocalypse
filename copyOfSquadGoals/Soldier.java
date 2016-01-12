package copyOfSquadGoals;

import java.util.Random;

import battlecode.common.*;

public class Soldier {

	public void run(RobotController rc, Brain brain) throws GameActionException{

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
			Entity.updateDenLocations(rc, brain);
			RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), rc.getType().sensorRadiusSquared);
			RobotInfo[] opponents = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam().opponent());
			RobotInfo nearestEnemy = null;
			if (opponents.length>0){
				nearestEnemy = Entity.findClosestEnemy(rc, brain, opponents, rc.getLocation());
			}
			if (opponents.length > 0 && !(brain.leadersLastKnownLocation == null) 
					&& rc.getLocation().distanceSquaredTo(Entity.findClosestEnemy(
							rc, brain, opponents, rc.getLocation()).location) > 13){
				rc.broadcastSignal(rc.getType().sensorRadiusSquared*2);
			}
			
			boolean inDanger = Entity.inDanger(enemies, rc.getLocation(), false);
			if (inDanger){
				Entity.safeMove(rc, brain, enemies, Direction.NONE, false);
			}
			Boolean attack = Entity.attackHostiles(rc, enemies);
			if (attack) {
				rc.setIndicatorString(1, "Attacking");
			} else if (nearestEnemy != null && nearestEnemy.type == RobotType.TURRET){
				Entity.moveTowards(rc, rc.getLocation().directionTo(nearestEnemy.location));
			} else if (rc.getHealth() < rc.getType().maxHealth /2 && enemies.length > 0){
					Entity.retreatMove(rc, brain, enemies);
			} else if (rc.getHealth() < rc.getType().maxHealth /2 && brain.leadersLastKnownLocation != null) {
				Entity.moveTowards(rc, rc.getLocation().directionTo(brain.leadersLastKnownLocation));
			} else if (rc.isCoreReady() && inDanger){
				Entity.moveRandomDirection(rc, brain);
			} else if (rc.isCoreReady() && enemies.length != 0){
				boolean move= Entity.moveOptimalAttackRange(rc, brain, enemies);
				if (!move){
					Entity.digInDirection(rc, brain, Entity.awayFromEnemies(rc, enemies, brain));
				}
			} else if (brain.goalLocation != null && rc.isCoreReady() && enemies.length == 0){
				Entity.moveTowards(rc, rc.getLocation().directionTo(brain.goalLocation));
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
			if (brain.goalLocation!=null) {
				rc.setIndicatorString(1, "Goal: " + brain.goalLocation.x + ", " + brain.goalLocation.y);
			} else {
				rc.setIndicatorString(1, "No goal yet");
			}
			Clock.yield();
		}
	}
}
