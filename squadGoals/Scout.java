package squadGoals;

import java.util.Random;

import battlecode.common.*;

public class Scout {
	
	public void run(RobotController rc, Brain brain) throws GameActionException{
		Random rand = new Random(rc.getID());
		Direction randomDir = Entity.directions[rand.nextInt(8)];
		Direction currentDir = randomDir;
		
		while (true) {
			Entity.searchForDen(rc, brain);
			if (rc.isCoreReady()) {
				RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), rc.getType().sensorRadiusSquared);
				Entity.safeMoveRanged(rc, brain, enemies, currentDir);
				rc.setIndicatorString(1, "Moving Random "+ randomDir.toString());
			}
			Clock.yield();
		}
	}
}