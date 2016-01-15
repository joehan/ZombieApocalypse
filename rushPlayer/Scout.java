package rushPlayer;

import java.util.Random;

import battlecode.common.*;

public class Scout {
	
	public static void run(RobotController rc){
		
		while (true) {
			if (rc.isCoreReady()) {
				Random rand = new Random(rc.getID());
				MapLocation nearestArchon = Entity.searchForArchon(rc);
				//If no Archon is sensed
				if (rc.getLocation().equals(nearestArchon)){
					
					Direction randomDir = Entity.directions[rand.nextInt(8)];
					try {
						Entity.moveInDirection(rc, randomDir);
					} catch (GameActionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					rc.setIndicatorString(1, "Moving Random "+ randomDir.toString());
				} else {//if we sense a Archon
					
					try {
						Entity.signalMessageLocation(rc, nearestArchon);
					} catch (GameActionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					rc.setIndicatorString(1, "Moving to den at " + nearestArchon.x + ", " + nearestArchon.y);
				}
				
			}
			Clock.yield();
		}
	}
}