package betterScouting;

import battlecode.common.*;

public class Scout {
	
	public static void run(RobotController rc, Brain brain){
		brain.getMapSymmetry(rc.getInitialArchonLocations(rc.getTeam()), 
				rc.getInitialArchonLocations(rc.getTeam().opponent()));
		
		MapLocation toGo = brain.flipAcrossAxis(rc.getLocation());
		while (true){
			try {
				RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), rc.getType().sensorRadiusSquared);
				Signal[] signals = rc.emptySignalQueue();
				for (Signal signal : signals){
					if (brain.isArchon(signal.getID())){
						rc.setIndicatorString(1, "saw an archon signal");
					}
				}
				
				Entity.moveTowards(rc, rc.getLocation().directionTo(toGo));
				Entity.addArchonsToBrain(rc, enemies, brain);
				Clock.yield();
			}
			catch (Exception e){
				
			}
		}
	}
}