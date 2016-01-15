package betterScouting;

import battlecode.common.*;

public class Guard {
	
	public static void run(RobotController rc, Brain brain){
		
		boolean attack = false;
		
		while (true){
			try {
				Signal[] signals = rc.emptySignalQueue();
				for (Signal signal : signals){
					int[] message = signal.getMessage();
					if (message != null && message[0] == 1){
						attack = true;
					}
				}
				
				RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), rc.getType().sensorRadiusSquared);
				RobotInfo nearestEnemy =  enemies.length > 0 ? Entity.findClosestEnemy(rc, brain, enemies, rc.getLocation()) : null;
				if (attack && enemies.length > 0){
					if (rc.getLocation().distanceSquaredTo(nearestEnemy.location) < 3 && rc.isWeaponReady()) {
						Entity.basicAttack(rc, nearestEnemy.location);
					}
					else {
						Entity.moveInDirectionClearRubble(rc, rc.getLocation().directionTo(nearestEnemy.location));
					}
				} else if (attack){
					MapLocation[] initialEnemy = rc.getInitialArchonLocations(rc.getTeam().opponent());
					int avgx = 0;
					int avgy = 0;
					for (int i = 0; i < initialEnemy.length; i ++){
						avgx += initialEnemy[i].x;
						avgy += initialEnemy[i].y;
					}
					avgx = avgx/initialEnemy.length;
					avgy = avgy/initialEnemy.length;
					
					Entity.moveToLocation(rc, new MapLocation(avgx, avgy));
				} else {
					RobotInfo[] closeAllies = rc.senseNearbyRobots(3, rc.getTeam());
					if (closeAllies.length > 2){
						Entity.moveInDirection(rc, rc.getLocation().directionTo(rc.getInitialArchonLocations(rc.getTeam())[0]).opposite());
					}
				}
				
				Clock.yield();
			}
			catch (Exception e){
				
			}
		}
	}
}