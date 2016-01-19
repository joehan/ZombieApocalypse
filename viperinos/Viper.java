package viperinos;

import battlecode.common.*;

public class Viper {
	
	public void run(RobotController rc, Brain brain) throws GameActionException{
		boolean superAggroMode = false;
		MapLocation[] enemyStartLocations = rc.getInitialArchonLocations(rc.getTeam().opponent());
		MapLocation closestArchon = null;
		int distance = 4000;
		for (MapLocation enemyArchon : enemyStartLocations){
			if (enemyArchon != null && rc.getLocation().distanceSquaredTo(enemyArchon) < distance){
				closestArchon = enemyArchon;
				distance = rc.getLocation().distanceSquaredTo(enemyArchon);
			}
		}
		if (rc.getRoundNum() < 50 && closestArchon != null){
			superAggroMode = true;

			for (int i = 0; i < enemyStartLocations.length; i ++){
				brain.addArchon(enemyStartLocations[i], 9999 - i);
			}
		}
		
		
		while (true){
			if (rc.getRoundNum() > 300){
				superAggroMode = false;
			}
			brain.thisTurnsSignals = rc.emptySignalQueue();

			Squad.listenForCommands(rc, brain);
			RobotInfo[] allies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam());
			RobotInfo[] opponents = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam().opponent());
			RobotInfo[] zombies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, Team.ZOMBIE);
			RobotInfo closestEnemy = Entity.findClosestHostile(rc, opponents, zombies);
			if (superAggroMode){
				rc.setIndicatorString(0, "super aggro mode");
				superAggroAttack(rc, brain, opponents, zombies, closestEnemy);
			}else if (closestEnemy != null ){
				kite(rc, brain, closestEnemy, opponents, zombies, allies, false);
			} else if (rc.isCoreReady() && brain.leaderMovingInDirection!=null){
				Entity.move(rc, brain, rc.getLocation().directionTo(brain.leaderLocation.add(brain.leaderMovingInDirection, 4)), false);
			} else if (rc.isCoreReady()){
				Entity.digInDirection(rc, brain, Direction.NORTH);
			}
			Clock.yield();
		}
	}
	
	private void superAggroAttack(RobotController rc, Brain brain, RobotInfo[] opponents, RobotInfo[] zombies,
			RobotInfo closestEnemy) throws GameActionException{

		for (int id : brain.archonIds){
			MapLocation lastKnownArchonInfo = brain.enemyInfo[id];
			if ( lastKnownArchonInfo != null && rc.getLocation().distanceSquaredTo(lastKnownArchonInfo) < 5){
				brain.enemyInfo[id] = null;				
			}
		}
		MapLocation closestArchon = null;
		int distance = 4000;
		for (int id : brain.archonIds){
			if (brain.enemyInfo[id] != null && rc.getLocation().distanceSquaredTo(brain.enemyInfo[id]) < distance){
				closestArchon = brain.enemyInfo[id];
				distance = rc.getLocation().distanceSquaredTo(brain.enemyInfo[id]);
			}
		}
		if (closestEnemy != null && closestEnemy.type != RobotType.ZOMBIEDEN){
			kite(rc, brain, closestEnemy, opponents, zombies, new RobotInfo[0], false);
		} else if (closestArchon != null && rc.isCoreReady()){
			rc.setIndicatorString(1, closestArchon.toString());
			Entity.moveSuperLimited(rc, brain, rc.getLocation().directionTo(closestArchon));
			if (rc.isCoreReady()){
				Entity.digInDirection(rc, brain, rc.getLocation().directionTo(closestArchon));
			}
		}
	}
	
	

	/*
	 *attack attacks nearby enemies if possible, with the following priority
	 * 1 -Uninfected enemies
	 * 2 - Infected enemies
	 * 3 - The closest zombie
	 * 
	 * it returns true if the robot attacked, and false otherwise
	 */
	private boolean attack(RobotController rc, RobotInfo[] enemies, RobotInfo[] zombies, boolean avoidDens
			) throws GameActionException{
		
		boolean attacked = false;
		if (rc.isWeaponReady()){
			if (enemies.length >0){
				for (RobotInfo enemy : enemies){
					if (enemy.viperInfectedTurns == 0){
						//TODO attack enemy with lowest health first
						rc.attackLocation(enemy.location);
						attacked = true;
						break;
					}
				}
				if (!attacked){
					//TODO attack enemy with least time left on viper timer
					rc.attackLocation(enemies[0].location);
					attacked = true;
				}
			} else if (zombies.length >0){
				RobotInfo closestZombie = zombies[0];
				int distanceToClosest = 50000;
				for (RobotInfo zombie : zombies){
					int distanceToZombie = rc.getLocation().distanceSquaredTo(zombie.location);
					if (distanceToZombie < distanceToClosest){
						closestZombie = zombie;
						distanceToClosest = distanceToZombie;
					}
				}
				if (!avoidDens){
					rc.attackLocation(closestZombie.location);
					attacked = true;
				} else if (closestZombie.type != RobotType.ZOMBIEDEN){
					rc.attackLocation(closestZombie.location);
					attacked = true;
				}
				
			}
		}
		return attacked;
	}
	
	private void kite(RobotController rc, Brain brain, RobotInfo closestEnemy, RobotInfo[] enemies, RobotInfo[] zombies,
			RobotInfo[] allies, boolean avoidDens) throws GameActionException {
		RobotInfo nearestOpponent = Entity.findClosestHostile(rc, enemies, new RobotInfo[0]);
		RobotInfo nearestZombie = Entity.findClosestHostile(rc, new RobotInfo[0], zombies);
//		int distanceToNearestEnemy = rc.getLocation().distanceSquaredTo(closestEnemy.location);
		int distanceToNearestZombie = zombies.length > 0 ? rc.getLocation().distanceSquaredTo(nearestZombie.location) : 100;
		if (rc.getHealth() < rc.getType().maxHealth/3 && rc.isCoreReady()){
			if (brain.leaderLocation != null){
				Entity.move(rc, brain, rc.getLocation().directionTo(brain.leaderLocation), false);
			} else {
				Entity.move(rc, brain, rc.getLocation().directionTo(closestEnemy.location).opposite(), false);
			}
		} else if (rc.isCoreReady() && ((zombies.length > 0 && distanceToNearestZombie <= 13) || 
				(enemies.length > 0 && rc.getLocation().distanceSquaredTo(nearestOpponent.location) <= 13))){
			//TODO check to see if moving makes enemy still in sight range
			Entity.move(rc, brain, rc.getLocation().directionTo(closestEnemy.location).opposite(), false);
		} else if (rc.isWeaponReady()){
			attack(rc, enemies, zombies, avoidDens);
		}
	}
	
}