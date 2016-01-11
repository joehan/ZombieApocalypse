package squadGoals;

import java.util.Random;

import battlecode.common.*;

public class Scout {
	
	public void run(RobotController rc, Brain brain) throws GameActionException{
		Random rand = new Random(rc.getID());
		Direction randomDir = Entity.directions[rand.nextInt(8)];
		
		while (true) {
//			if (rc.isCoreReady()) {
//				MapLocation nearestDen = Entity.searchForDen(rc, brain);
//				
//				//If we see a new den
//				if ( (!(nearestDen.equals(rc.getLocation()))) && brain.isDenNew(nearestDen) ){
//					Entity.signalMessageLocation(rc, nearestDen);
//					rc.setIndicatorString(1, "Found den at " + nearestDen.x + ", " + nearestDen.y);
//				} else {
				Entity.moveInDirection(rc, randomDir);
				randomDir = Entity.directions[rand.nextInt(8)];
				rc.setIndicatorString(1, "Moving Random "+ randomDir.toString());
//				} 
				
//			}
			Clock.yield();
		}
	}
}