package squadGoals;

import java.util.Random;

import battlecode.common.*;

public class Soldier {

	public void run(RobotController rc, Brain brain) throws GameActionException{
		int maxRoundNum = rc.getRoundLimit();

		while (true) {
			//Get important info for the turn
			Squad.processMessages(rc, brain);
			RobotInfo[] allies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam());
			RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), rc.getType().sensorRadiusSquared);
			RobotInfo[] opponents = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam().opponent());
			RobotInfo closestEnemy = null;
			if (enemies.length > 0){
				closestEnemy = Entity.findClosestEnemy(rc, brain, enemies, rc.getLocation());
			}
			Entity.updateDenLocations(rc, brain);
			boolean inDanger = Entity.inDanger(enemies, rc.getLocation(), false);
//			int roundNum = rc.getRoundNum();
			boolean underAttack = (rc.getHealth() + 9 < brain.lastTurnHealth && opponents.length == 0
					) || (closestEnemy != null && closestEnemy.type == RobotType.TURRET);
			
			//Squad commands and messages 
			if (brain.getSquadNum() == -1){
				Squad.lookForASquad(rc, brain, allies);
			} else {
				Squad.findLeaderLocation(rc, brain);
				Squad.listenForCommands(rc, brain);
				Squad.listenForIntersquadCommunication(rc, brain);
			}
			if (opponents.length > 0 && rc.getLocation().distanceSquaredTo(closestEnemy.location) > 13){
				rc.broadcastSignal(rc.getType().sensorRadiusSquared*2);
			}
			
			//Special enemy attack. Swarm, then attack
			if (brain.swarmLoc != null && !brain.attack){
				if (underAttack){
					rc.setIndicatorString(0, "moving away from last enemy loc");
					Entity.moveTowards(rc,rc.getLocation().directionTo(brain.swarmLoc).opposite());
				}
				else if (rc.getLocation().distanceSquaredTo(brain.swarmLoc) > 90){
					Entity.attackHostiles(rc, enemies);
					if (rc.isCoreReady()){
						Entity.safeMove(rc, brain, enemies, brain.swarmLoc, true);
					}
				} else {
					//within swarm distance, just chill
				}
			} else if (brain.swarmLoc != null){
				Entity.attackHostiles(rc, enemies);
				if (rc.isCoreReady()){
					if (closestEnemy != null){
						Entity.moveTowards(rc, rc.getLocation().directionTo(closestEnemy.location));
					} else {
						Entity.moveTowards(rc, rc.getLocation().directionTo(brain.swarmLoc));
					}
				}
			} else {
				//Normal attack and micro
				
				if (inDanger){
					Entity.safeMove(rc, brain, enemies, Direction.NONE, false);
				}
				if (underAttack && brain.enemyLocation.size() > 0){
					//Turret attacks us and we can't see it
					Entity.moveTowards(rc, rc.getLocation().directionTo(
							brain.enemyLocation.get(brain.enemyLocation.size())).opposite());
				}
				Entity.attackHostiles(rc, enemies);
				if (rc.isCoreReady()){
					if (rc.getHealth() < rc.getType().maxHealth/3){
						if (enemies.length > 0){

							Entity.retreatMove(rc, brain, enemies, closestEnemy);
						} else if (brain.leadersLastKnownLocation != null){
							Entity.moveTowards(rc, rc.getLocation().directionTo(brain.leadersLastKnownLocation));
						}
					} else if (inDanger){
						Entity.moveRandomDirection(rc, brain);
					} else if ( enemies.length != 0){
						boolean move= Entity.moveOptimalAttackRange(rc, brain, enemies, closestEnemy);
						if (!move){
							Entity.digInDirection(rc, brain, Entity.awayFromEnemies(rc, enemies, brain));
						}
					} else if (brain.enemyLocation.size() > 0 && rc.getLocation().distanceSquaredTo(brain.getMostRecentEnemyLocation()) < 70){
						rc.setIndicatorString(1, brain.getMostRecentEnemyLocation().toString());
						Entity.moveTowards(rc, rc.getLocation().directionTo(brain.getMostRecentEnemyLocation()).opposite());
					} else if (brain.goalLocation != null){
						if (brain.enemyLocation.size()>0){
							rc.setIndicatorString(0, brain.getMostRecentEnemyLocation().toString());
						}
						if (!Entity.moveTowards(rc, rc.getLocation().directionTo(brain.goalLocation))){
							Entity.digInDirection(rc, brain, Entity.awayFromEnemies(rc, enemies, brain));
						}
					} else if (brain.leadersLastKnownLocation != null) {
						 if (rc.getLocation().distanceSquaredTo(brain.leadersLastKnownLocation) > 33){
							 Entity.moveTowards(rc, rc.getLocation().directionTo(brain.leadersLastKnownLocation));
						 } else if (rc.getLocation().distanceSquaredTo(brain.leadersLastKnownLocation) < 15){
							 Entity.moveTowards(rc, rc.getLocation().directionTo(brain.leadersLastKnownLocation).opposite());
						 }
					}
				}
				if (brain.goalLocation!=null) {
					rc.setIndicatorString(1, "Goal: " + brain.goalLocation.x + ", " + brain.goalLocation.y);
				} else {
					rc.setIndicatorString(1, "No goal yet");
				}
			}
			Clock.yield();
		}
	}
}
