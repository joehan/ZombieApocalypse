package betterScouting;


import battlecode.common.*;

public class Archon {
	
	public void run(RobotController rc, Brain brain){
		
		RobotType typeToBuild = nextUnitToBuild(brain);
		
		while(true){
			try {
				RobotInfo[] opponents = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam().opponent());
				RobotInfo[] zombies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, Team.ZOMBIE);
				RobotInfo[] allies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam());
				RobotInfo[] enemies = Entity.concat(opponents, zombies);
				RobotInfo closestEnemy = (enemies.length > 0) ? Entity.findClosestEnemy(rc, brain, enemies, rc.getLocation()) : null;
				
				repair(rc, allies);
				
				if (rc.isCoreReady()){
					if (tryToBuild(rc, typeToBuild, Direction.NORTH)){
						typeToBuild = nextUnitToBuild(brain);
					} else if (Entity.fleeEnemies(rc,brain,enemies,zombies, closestEnemy)){
						Squad.sendDirectionToMove(rc, brain, brain.lastDirectionMoved);
					} else {
						Entity.move(rc, brain, brain.lastDirectionMoved, false);
						Squad.sendDirectionToMove(rc, brain, brain.lastDirectionMoved);
					}
				}
				Clock.yield();
			}
			catch (Exception e){
				//Do something?
				e.printStackTrace();
				
			}
		}
	}
	
	public RobotType nextUnitToBuild(Brain brain){
		RobotType robotToBuild;
		if(brain.initialIteration && brain.startBuildArray.length>0){
			robotToBuild = brain.startBuildArray[brain.buildCount];
			brain.buildCount++;
			if(brain.buildCount >= brain.startBuildArray.length){
				brain.buildCount = 0;
				brain.initialIteration =false;
			}
		}else{
			robotToBuild = brain.iterateBuildArray[brain.buildCount];
			brain.buildCount++;
			if(brain.buildCount >= brain.iterateBuildArray.length){
				brain.buildCount = 0;
			}
		}
		return robotToBuild;
	}
	
	/*
	 * tryToBuild takes a RobotType, and attempts to build it in the given direction
	 * If it cannot be built in that direction, it rotates the direction left and tries again
	 * Returns true if a unit was built,
	 * and false otherwise
	 */
	public boolean tryToBuild(RobotController rc, RobotType type, Direction dir) throws GameActionException {
		
		if (!rc.hasBuildRequirements(type)){
			return false;
		}
		boolean built = false;
		for (int i = 0; i < 8; i++) {
            if (rc.canBuild(dir, type)) {
                rc.build(dir,type);
                built = true;
                break;
            } else {
                dir = dir.rotateLeft();
            }
        }
		return built;
	}
	
	
	/*
	 * Repair looks through the array of nearby allies, and repairs the first injured robot it sees
	 */
	public void repair(RobotController rc, RobotInfo[] allies) throws GameActionException{
		for (RobotInfo ally: allies){
			if (ally.health < ally.maxHealth && ally.type !=RobotType.ARCHON && ally.location.distanceSquaredTo(rc.getLocation()) <= rc.getType().attackRadiusSquared){
				rc.repair(ally.location);
				break;
			}
		}
	}
	
	
}