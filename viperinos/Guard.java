package viperinos;

import battlecode.common.*;

public class Guard {
	
	public void run(RobotController rc, Brain brain){
		while (true){
			try {
				brain.thisTurnsSignals = rc.emptySignalQueue();
				
				RobotInfo[] allies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam());
				RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam().opponent());
				RobotInfo[] zombies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, Team.ZOMBIE);
				RobotInfo nearestHostile = Entity.findClosestHostile(rc, enemies, zombies);
				
				if (brain.leaderLocation != null && rc.getLocation().distanceSquaredTo(brain.leaderLocation) < 5){
					if (!Entity.canSenseArchon(rc, allies)){
						brain.leaderLocation = null;
						brain.distanceToLeader = 50000;
						brain.leaderMovingInDirection = null;
					}
				}
				Squad.listenForCommands(rc, brain);
				boolean moved = false;
				if (attack(rc,brain,nearestHostile)){
					rc.setIndicatorString(0, "I am attacking");
				} else if (rc.isCoreReady() && brain.leaderMovingInDirection!=null && enemies.length == 0){
					Direction dirToMove = rc.getLocation().directionTo(brain.leaderLocation.add(brain.leaderMovingInDirection, 4));
					moved = Entity.move(rc, brain, dirToMove, false);
					rc.setIndicatorString(0, "I am going " + dirToMove.toString());
				}
				if (!moved && rc.isCoreReady()){
					Entity.digInDirection(rc, brain, Direction.NORTH);
				}
				
				Clock.yield();
			}
			catch (Exception e){
				
			}
		}
	}
	
	public boolean attack(RobotController rc, Brain brain, RobotInfo closestRobot) throws GameActionException{
		boolean attacked = false;
		if (closestRobot != null){
			if (rc.canAttackLocation(closestRobot.location) && rc.isWeaponReady()){
				rc.attackLocation(closestRobot.location);
				attacked = true;
			} else if (rc.isCoreReady()){
				Entity.move(rc, brain, rc.getLocation().directionTo(closestRobot.location), false);
				attacked = true;
			}
		}
		return attacked;
	}
}