package rovingGangs;

import java.util.Random;

import battlecode.common.*;

public class Turret {
	
	public void run(RobotController rc, Brain brain) throws GameActionException{
		if (rc.isCoreReady()) {
			Boolean attack = Entity.attackHostiles(rc);
			if (!attack) {
				
			}
		}

	}
}