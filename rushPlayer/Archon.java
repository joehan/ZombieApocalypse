package rushPlayer;

import java.util.Random;

import battlecode.common.*;

public class Archon {
	
	private static Random rand;
	public static void run(RobotController rc) throws GameActionException{	
		rand = new Random(rc.getID());
		RobotType typeToBuild = RobotType.SOLDIER;
		while (true) {
			if (rc.isCoreReady()) {
				//Decide what unit to make
				int makeScout = rand.nextInt(10);
				if (makeScout > 8) {
					typeToBuild=RobotType.SCOUT;
				} else {
					typeToBuild=RobotType.SOLDIER;
				}
				
				//Look for dens
				MapLocation nearbyDen = Entity.searchForDen(rc);
				
				//Repair a neaarby unit, if there aare any
				repairUnits(rc);
				
				//Try to build a unit if you have the parts
				if (rc.hasBuildRequirements(typeToBuild)) {
					tryBuildUnitInEmptySpace(rc, typeToBuild,Direction.NORTH);
				//Otherwise, call out any dens if you see them
				} else if (!(nearbyDen.equals(rc.getLocation()))) {
					Entity.signalMessageLocation(rc, nearbyDen);
				//Otherwise, move
				} else {
					archonMove(rc);
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
	
	private static void archonMove(RobotController rc) throws GameActionException {
		//Look for bad guys
		RobotInfo[] nearbyHostiles = rc.senseHostileRobots(rc.getLocation(),  rc.getType().sensorRadiusSquared);
		//If there are any bad guys, run away
		if (nearbyHostiles.length > 0 ) {
			RobotInfo enemy = nearbyHostiles[0];
			Direction dirToHostile = rc.getLocation().directionTo(enemy.location);
			Entity.moveInDirection(rc, dirToHostile.opposite());
			//Otherwise, run around randomly
		} else {
			Direction randomDir = Entity.directions[rand.nextInt(8)];
			Entity.moveInDirection(rc, randomDir);
		}
	}
}