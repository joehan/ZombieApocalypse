package viperinos;


import battlecode.common.*;

public class Archon {
	
	public void run(RobotController rc, Brain brain) throws GameActionException{
		RobotType typeToBuild = nextUnitToBuild(brain);
		brain.lastDirectionMoved = Entity.directions[brain.rand.nextInt(8)];
		while (true){
			RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam().opponent());
			RobotInfo[] zombies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, Team.ZOMBIE);
			RobotInfo[] allies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam());
			RobotInfo closestEnemy = Entity.findClosestHostile(rc, enemies, zombies);
			
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
	}
	
	
	
	/*
	 * buildNextUnit builds the next unit up in your build order.
	 * First, it iterates through startBuildArray once, and then
	 * it repeatedly builds through mainBuildArray
	 */
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
			robotToBuild = brain.mainBuildArray[brain.buildCount];
			brain.buildCount++;
			if(brain.buildCount >= brain.mainBuildArray.length){
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
			if (ally.health < ally.maxHealth){
				rc.repair(ally.location);
				break;
			}
		}
	}
	
	
}