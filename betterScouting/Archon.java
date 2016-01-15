package betterScouting;

//import scala.util.control.Breaks.TryBlock;
import battlecode.common.*;

public class Archon {
	
	public static void run(RobotController rc, Brain brain){
		
		RobotType typeToBuild = buildNextUnit(brain);
		
		while(true){
			try {
				RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), rc.getType().sensorRadiusSquared);
				if (tryBuildUnitInEmptySpace(rc, brain, typeToBuild, Direction.NORTH)){
					typeToBuild = buildNextUnit(brain);
				}
				if (rc.getTeamParts() < 30){
					rc.broadcastMessageSignal(1, 0, 10000);
				}
				
				
				Clock.yield();
			}
			catch (Exception e){
				//Do something?
				
			}
		}
	}
	
	private static boolean tryBuildUnitInEmptySpace(RobotController rc, Brain brain, RobotType typeToBuild, Direction dirToBuild) throws GameActionException{
        for (int i = 0; i < 8; i++) {
            // If possible, build in this direction
            if (rc.canBuild(dirToBuild, typeToBuild)) {
                rc.build(dirToBuild, typeToBuild);
                return true;
            } else {
                // Rotate the direction to try
                dirToBuild = dirToBuild.rotateLeft();
            }
        }
        return false;
	}
	
	public static RobotType buildNextUnit(Brain brain){
		int buildCount = brain.getBuildCount();
		if(brain.getInitialIteration()){
			RobotType returnRobot = brain.getStartBuildArray()[buildCount];
			buildCount++;
			if(buildCount >= brain.getStartBuildLength()){
				brain.setBuildCount(0);
				brain.setInitialIteration(false);
			}else{
				brain.setBuildCount(buildCount);
			}
			return returnRobot;
		}else{
			RobotType returnRobot = brain.getIterateBuildArray()[buildCount];
			buildCount++;
			if(buildCount >= brain.getIterateBuildLength()){
				brain.setBuildCount(0);
			}
			else{
				brain.setBuildCount(buildCount);
			}
			return returnRobot;
		}
	}
	
	
}