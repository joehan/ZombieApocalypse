package rushPlayer;

import java.util.Random;

import battlecode.common.*;

public class Scout {
	
	public static void run(RobotController rc) throws GameActionException{
		Random rand = new Random(rc.getID());
		Direction randomDir = Entity.directions[rand.nextInt(8)];
		
		while (true) {
			if (rc.isCoreReady()) {
				MapLocation nearestArchon = Entity.searchForDen(rc);
				//If no Archon is sensed
				if (rc.getLocation().equals(nearestArchon)){
					Entity.moveInDirection(rc, randomDir);
					randomDir = Entity.directions[rand.nextInt(8)];
					rc.setIndicatorString(1, "Moving Random "+ randomDir.toString());
				} else {//if we sense a Archon
					
					Entity.signalMessageLocation(rc, nearestArchon);
					rc.setIndicatorString(1, "Moving to den at " + nearestArchon.x + ", " + nearestArchon.y);
				}
				
			}
			Clock.yield();
		}
	}
}