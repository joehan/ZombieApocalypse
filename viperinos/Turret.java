package viperinos;

import java.util.Random;

import battlecode.common.*;

public class Turret {
	private Random rand = new Random();
	public void run(RobotController rc, Brain brain) throws GameActionException{
		while (true) {
			if (rc.getType() ==RobotType.TURRET) {
				turretRun(rc,brain);
			} else if (rc.getType() == RobotType.TTM) {
				ttmRun(rc,brain);
			}
			Clock.yield();
		}
	
	}
	public void turretRun(RobotController rc, Brain brain) throws GameActionException {
		RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), rc.getType().sensorRadiusSquared);
		Boolean attack = turretAttack(rc, brain, enemies);
	}
	
	public void ttmRun(RobotController rc, Brain brain) throws GameActionException {
		rc.unpack();
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
}