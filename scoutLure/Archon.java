package scoutLure;

import java.util.Random;

import scoutLure.Entity.*;

import battlecode.common.*;

public class Archon {
	
	public static void run(RobotController rc) throws GameActionException{
		Brain brain = new Brain(rc.getLocation());

		while (true){
			try{
				Entity.receiveMessages(rc, brain);
				if (rc.getRoundNum() % 10 == 0){
					sendHeartbeat(rc, brain);
				}

//				Random rand = new Random(rc.getID()+rc.getRoundNum());
				RobotType typeToBuild = RobotType.SCOUT;

				if (rc.isCoreReady()) {
					repairUnits(rc);
					tryBuildUnitInEmptySpace(rc, typeToBuild, Direction.NORTH);
					Clock.yield();
				}
			}catch (Exception e){
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	
	
	private static void sendHeartbeat(RobotController rc, Brain brain) throws GameActionException{
		if (!(brain.maxHeight == (Integer) null)){
    		rc.broadcastMessageSignal(0, brain.maxHeight, 80);
    	}
    	if (!(brain.minHeight == (Integer) null)){
    		rc.broadcastMessageSignal(1, brain.minHeight, 80);
    		rc.setIndicatorString(0, "sent message about min Height on round:" + rc.getRoundNum());

    	}
    	if (!(brain.maxWidth == (Integer) null)){
    		rc.broadcastMessageSignal(2, brain.maxWidth, 80);
    	}
    	if (!(brain.minWidth == (Integer) null)){
    		rc.broadcastMessageSignal(3, brain.minWidth, 80);
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
}