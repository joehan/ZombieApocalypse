package rovingGangs;

import java.util.Random;

import battlecode.common.*;

public class Scout {
	
	public static void run(RobotController rc) throws GameActionException{
		Random rand = new Random(rc.getID());
		Direction randomDir = Entity.directions[rand.nextInt(8)];
		
		while (true) {
			if (rc.isCoreReady()) {
				MapLocation nearestDen = Entity.searchForDen(rc);
				//If no den is sensed
				if (rc.getLocation().equals(nearestDen)){
					Entity.moveInDirection(rc, randomDir);
					randomDir = Entity.directions[rand.nextInt(8)];
					rc.setIndicatorString(1, "Moving Random "+ randomDir.toString());
				} else {//if we sense a den
					
					Entity.signalMessageLocation(rc, nearestDen);
					rc.setIndicatorString(1, "Moving to den at " + nearestDen.x + ", " + nearestDen.y);
				}
				
			}
			Clock.yield();
		}
	}
}