package chickenArchon;

import java.util.Random;

import battlecode.common.*;

public class Scout {
	
	public void run(RobotController rc, Brain brain) throws GameActionException{
		Random rand = new Random(rc.getID());
		Direction randomDir = Entity.directions[rand.nextInt(8)];
		Direction currentDir = randomDir;
		
		while (true) {
			RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), rc.getType().sensorRadiusSquared);
			RobotInfo[] opponents = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam().opponent());
			Entity.searchForDen(rc, brain);
			Entity.scoutEnemy(rc, brain, opponents);
			
			if (brain.enemyLocation.size()>0){
				rc.setIndicatorString(0, brain.getMostRecentEnemyLocation().toString());
			}
			if (rc.isCoreReady()) {
				
				
				
				boolean move = false;
				int randomNum = rand.nextInt(8);
				int i = 0;
				while (!move && i < 8){
					move = Entity.safeMoveOneDirectionRanged(rc, enemies, brain, currentDir);
					if (!move){
						currentDir = Entity.directions[(randomNum + i)%8];
					}
					i ++;
				}
				if (!move){
					//scout is trapped
					Entity.moveRandomDirection(rc, brain);
				}
				
				rc.setIndicatorString(1, "Moving Random "+ randomDir.toString());
			}
			Clock.yield();
		}
	}
}