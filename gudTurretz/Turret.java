package gudTurretz;

import java.util.Random;

import battlecode.common.*;

public class Turret {
	private Random rand = new Random();
	public void run(RobotController rc, Brain brain) throws GameActionException{
		while (true) {
			brain.thisTurnsSignals = rc.emptySignalQueue();
			if (brain.getSquadNum()!=-1){
				Squad.findLeaderLocation(rc, brain);
			} else {
				Squad.lookForASquad(rc, brain, rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam()));
			}
			if (rc.getType() ==RobotType.TURRET) {
				turretRun(rc,brain);
			} else if (rc.getType() == RobotType.TTM) {
				ttmRun(rc,brain);
			}
		}
	
	}
	public void turretRun(RobotController rc, Brain brain) throws GameActionException {
		RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), rc.getType().sensorRadiusSquared);
		boolean attacked = turretAttack(rc, brain, enemies);
		boolean attackedBySound = false;
		if (brain.getSquadNum()==-1){
			RobotInfo[] allies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam());
			Squad.lookForASquad(rc, brain, allies);
		}
		if (!attacked && !attackedBySound && enemies.length == 0 && brain.getSquadNum()!=-1) {
			Squad.findLeaderLocation(rc, brain);
			if (rc.getLocation().distanceSquaredTo(brain.leadersLastKnownLocation) <= 8){
				rc.pack();
			}
		}
	}
	
	public void ttmRun(RobotController rc, Brain brain) throws GameActionException {
		RobotInfo[] enemiesInRange = rc.senseHostileRobots(rc.getLocation(), RobotType.TURRET.sensorRadiusSquared);
		RobotInfo[] allies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam());
		if (enemiesInRange.length>0 || rc.getLocation().distanceSquaredTo(Entity.findClosestArchon(rc, brain, allies))>8){
			rc.unpack();
		} else if (rc.isCoreReady()){
			Direction dirToMove = brain.leadersLastKnownLocation.directionTo(rc.getLocation());
			Entity.moveTowards(rc, dirToMove);
		}
	}
	
	public boolean turretAttack(RobotController rc, Brain brain, RobotInfo[] enemies) throws GameActionException {
		if (enemies.length > 0){
			if (rc.isWeaponReady()){
				RobotInfo weakestSoFar=null;
				double healthOfWeakest=1;
				
				for (RobotInfo enemy : enemies){
					double currentHealth = enemy.health/enemy.maxHealth;
					if ((weakestSoFar==null || currentHealth < healthOfWeakest)
							&& rc.canAttackLocation(enemy.location)){
						weakestSoFar=enemy;
						healthOfWeakest = currentHealth;
					}
				}
				if (!(weakestSoFar == null)){
					rc.attackLocation(weakestSoFar.location);
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean attackBySound(RobotController rc, Brain brain) throws GameActionException {
		
		return false;
	}
}