package betterScouting;



import battlecode.common.*;

public class Viper {
	
	public void run(RobotController rc, Brain brain){
		
		
		while (true){
			try {
				brain.thisTurnsSignals = rc.emptySignalQueue();
		
				Squad.listenForCommands(rc, brain);
				RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam().opponent());
				RobotInfo[] zombies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, Team.ZOMBIE);
				RobotInfo closestEnemy = enemies.length > 0 ? Entity.findClosestEnemy(rc, brain, enemies, rc.getLocation()) : null;
				if (closestEnemy != null ){
					kite(rc, brain, closestEnemy, enemies, zombies);
				} else if (rc.isCoreReady() && brain.leaderMovingInDirection!=null){
					Entity.move(rc, brain, rc.getLocation().directionTo(brain.leaderLocation.add(brain.leaderMovingInDirection)), false);
				}
				Clock.yield();
			} catch (Exception e){
				e.printStackTrace();
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
	private boolean attack(RobotController rc, RobotInfo[] enemies, RobotInfo[] zombies) throws GameActionException{
		
		boolean attacked = false;
		if (rc.isWeaponReady()){
			if (enemies.length >0){
				for (RobotInfo enemy : enemies){
					if (enemy.viperInfectedTurns == 0){
						//TODO find enemy with lowest health first
						rc.attackLocation(enemy.location);
						attacked = true;
						break;
					}
				}
				if (!attacked){
					//TODO attack enemy with lowest infect turns remaining instead
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
			int distanceToClosest = rc.getLocation().distanceSquaredTo(closestEnemy.location);
			if (distanceToClosest<16){
				Entity.fleeEnemies(rc, brain, enemies, zombies, closestEnemy);
			}
		}
		
	}
}