package viperinos;

import battlecode.common.*;

public class Viper {
	
	public void run(RobotController rc, Brain brain) throws GameActionException{
		
		while (true){
			brain.thisTurnsSignals = rc.emptySignalQueue();
			RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam().opponent());
			RobotInfo[] zombies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, Team.ZOMBIE);
			RobotInfo closestRobot = Entity.findClosestHostile(rc, enemies, zombies);
			if (closestRobot != null ){
				kite(rc, brain, closestRobot, enemies, zombies);
			}
			Clock.yield();
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
	private boolean attack(RobotController rc, RobotInfo[] enemies, RobotInfo[] zombies) throws GameActionException{
		
		boolean attacked = false;
		if (rc.isWeaponReady()){
			if (enemies.length >0){
				for (RobotInfo enemy : enemies){
					if (enemy.viperInfectedTurns == 0){
						rc.attackLocation(enemy.location);
						attacked = true;
						break;
					}
				}
				if (!attacked){
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
				rc.attackLocation(closestZombie.location);
				attacked = true;
			}
		}
		return attacked;
	}
	
	private void kite(RobotController rc, Brain brain, RobotInfo closestEnemy, RobotInfo[] enemies, RobotInfo[] zombies) throws GameActionException {
		double coreDelay = rc.getCoreDelay();
		double weaponDelay = rc.getWeaponDelay();
		
		if (weaponDelay < 1){
			attack(rc, enemies, zombies);
		} else if (coreDelay < 1){
			Entity.move(rc, brain, closestEnemy.location.directionTo(rc.getLocation()));
		}
		
	}
}