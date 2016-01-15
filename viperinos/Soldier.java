package viperinos;

import battlecode.common.*;

public class Soldier {

	public void run(RobotController rc, Brain brain) throws GameActionException{
		while(true){
			brain.thisTurnsSignals = rc.emptySignalQueue();
			Squad.listenForCommands(rc, brain);
			if (rc.isCoreReady() && brain.leaderMovingInDirection!=null){
				Entity.move(rc, brain, brain.leaderMovingInDirection);
			}
			Clock.yield();
		}
	}
}
