package betterScouting;

import battlecode.common.*;

public class Scout {
	
	public static void run(RobotController rc, Brain brain){
		brain.getMapSymmetry(rc.getInitialArchonLocations(rc.getTeam()), 
				rc.getInitialArchonLocations(rc.getTeam().opponent()));
		
		MapLocation toGo = brain.flipAcrossAxis(rc.getLocation());
		while (true){
			try {
				Entity.moveTowards(rc, rc.getLocation().directionTo(toGo));
				Clock.yield();
			}
			catch (Exception e){
				
			}
		}
	}
}