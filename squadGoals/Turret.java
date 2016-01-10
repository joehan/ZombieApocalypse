package squadGoals;

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
		}
	
	}
	public void turretRun(RobotController rc, Brain brain) throws GameActionException {
		Boolean attack = Entity.attackHostiles(rc);
		if (!attack) {
			RobotInfo[] adjacentFriends = rc.senseNearbyRobots(3, rc.getTeam());
			if (adjacentFriends.length >3){
				rc.pack();
			}
		}
	}
	
	public void ttmRun(RobotController rc, Brain brain) throws GameActionException {
		RobotInfo[] adjacentFriends = rc.senseNearbyRobots(3, rc.getTeam());
		RobotInfo[] enemiesInRange = rc.senseHostileRobots(rc.getLocation(), RobotType.TURRET.sensorRadiusSquared);
		if (enemiesInRange.length>0 || adjacentFriends.length < 2){
			rc.unpack();
		} else if (rc.isCoreReady()){
			Direction randomDir = Entity.directions[rand.nextInt(8)];
			Entity.moveInDirection(rc, randomDir);
		}
	}
}