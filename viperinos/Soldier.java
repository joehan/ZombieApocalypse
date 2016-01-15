package viperinos;

import battlecode.common.*;

public class Soldier {

	public void run(RobotController rc, Brain brain) throws GameActionException{
		while(true){
			brain.thisTurnsSignals = rc.emptySignalQueue();
			Squad.listenForCommands(rc, brain);
			if (rc.isCoreReady() && brain.leaderMovingInDirection!=null){
				Direction dirToMove = rc.getLocation().directionTo(brain.leaderLocation.add(brain.leaderMovingInDirection));
				Entity.move(rc, brain, dirToMove, false);
			}
			Clock.yield();
		}
	}
}
