package rovingGangs;

import java.util.Random;

import battlecode.common.*;

public class Archon {
	
	public static void run(RobotController rc) throws GameActionException{
		
		Random rand = new Random(rc.getID()+rc.getRoundNum());
		RobotType typeToBuild = RobotType.SOLDIER;
		while (true) {
			if (rc.isCoreReady()) {
				int makeScout = rand.nextInt(10);
				repairUnits(rc);
				
				if (makeScout > 8) {
					tryBuildUnitInEmptySpace(rc, RobotType.SCOUT, Direction.NORTH);
				} else {
					tryBuildUnitInEmptySpace(rc, typeToBuild, Direction.NORTH);
				}
				Clock.yield();
			}
		}
	}
	
	/*
	 * tryBuildUniitInEmptySpace takes a type of robot to build and a direction to start trying to build in,
	 * and, if the Archon is able, it will build a robot of that type in the nearest possible direction to dirTozBuild
	 */
	private static void tryBuildUnitInEmptySpace(RobotController rc, RobotType typeToBuild, Direction dirToBuild) throws GameActionException{
		if (rc.hasBuildRequirements(typeToBuild)) {
            for (int i = 0; i < 8; i++) {
                // If possible, build in this direction
                if (rc.canBuild(dirToBuild, typeToBuild)) {
                    rc.build(dirToBuild, typeToBuild);
                    break;
                } else {
                    // Rotate the direction to try
                    dirToBuild = dirToBuild.rotateLeft();
                }
            }
		}
	}
	/*
	 * repairUnits looks for damaged, adjacent friendly units, and repairs the non-archon unit it sees
	 */
	private static void repairUnits(RobotController rc) throws GameActionException {
		RobotInfo[] adjacentFriendlies = rc.senseNearbyRobots(2, rc.getTeam());
		for (RobotInfo friendly : adjacentFriendlies){
			if (friendly.health < friendly.type.maxHealth && friendly.type!=RobotType.ARCHON) {
				rc.repair(friendly.location);
				break;
			}
		}
	}
	
	//private static void archonMove()
}