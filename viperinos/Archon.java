package viperinos;


import battlecode.common.*;

public class Archon {
	
	public void run(RobotController rc, Brain brain) throws GameActionException{
		RobotType typeToBuild = nextUnitToBuild(brain);
		while (true){
			if (rc.isCoreReady()){
				if (tryToBuild(rc, typeToBuild, Direction.NORTH)){
					typeToBuild = nextUnitToBuild(brain);
				}
			}
		}
	}
	
	
	
	/*
	 * buildNextUnit builds the next unit up in your build order.
	 * First, it iterates through startBuildArray once, and then
	 * it repeatedly builds through mainBuildArray
	 */
	public RobotType nextUnitToBuild(Brain brain){
		RobotType robotToBuild;
		if(brain.initialIteration){
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
}