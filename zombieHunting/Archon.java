package zombieHunting;

import java.util.Random;

import battlecode.common.*;

public class Archon {
	
	private Random rand;
	public void run(RobotController rc, Brain brain) throws GameActionException{	
		rand = new Random(rc.getID());
		RobotType typeToBuild = RobotType.SOLDIER;
		brain.initBuildHistory();
		while (true) {
			if (rc.isCoreReady()) {
				if (rc.getRoundNum()==0){
					rc.broadcastMessageSignal(-1, -1, 3600);
				} else {
					
					//Look for dens
					MapLocation nearbyDen = Entity.searchForDen(rc);
					
					//Repair a neaarby unit, if there aare any
					repairUnits(rc);
					
					//Decide what to build
					int soldiersBuilt = brain.buildHistory.get(RobotType.SOLDIER);
					int scoutsBuilt = brain.buildHistory.get(RobotType.SCOUT);
					if (5*scoutsBuilt > soldiersBuilt){
						typeToBuild = RobotType.SOLDIER;
					} else {
						typeToBuild = RobotType.SCOUT;
					}
					
					//Try to build a unit if you have the parts
					if (rc.hasBuildRequirements(typeToBuild)) {
						tryBuildUnitInEmptySpace(rc, brain, typeToBuild,Direction.NORTH);
					//Otherwise, call out any dens if you see them
					} else if (!(nearbyDen.equals(rc.getLocation()))) {
						rc.setIndicatorString(3, "See den at" + nearbyDen.x + "," + nearbyDen.y);
						Entity.signalMessageLocation(rc, nearbyDen);
					//Otherwise, move
					} else {
						archonMove(rc);
					}
				}
				Clock.yield();
			}
		}
	}
	
	/*
	 * tryBuildUniitInEmptySpace takes a type of robot to build and a direction to start trying to build in,
	 * and, if the Archon is able, it will build a robot of that type in the nearest possible direction to dirTozBuild
	 */
	private void tryBuildUnitInEmptySpace(RobotController rc, Brain brain, RobotType typeToBuild, Direction dirToBuild) throws GameActionException{
        for (int i = 0; i < 8; i++) {
            // If possible, build in this direction
            if (rc.canBuild(dirToBuild, typeToBuild)) {
                rc.build(dirToBuild, typeToBuild);
                brain.iterateUnitInBuildHistory(typeToBuild);
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
	private void repairUnits(RobotController rc) throws GameActionException {
		RobotInfo[] inRangeFriendlies = rc.senseNearbyRobots(rc.getType().attackRadiusSquared, rc.getTeam());
		for (RobotInfo friendly : inRangeFriendlies){
			if (friendly.health < friendly.type.maxHealth && friendly.type!=RobotType.ARCHON) {
				rc.repair(friendly.location);
				break;
			}
		}
	}
	
	private void archonMove(RobotController rc) throws GameActionException {
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
	
	private void listenForArchonStarts(RobotController rc, Brain brain) throws GameActionException {
		Signal[] signals = rc.emptySignalQueue();
		for (Signal signal: signals){
			if (signal.getTeam()==rc.getTeam()) {
				MapLocation loc = signal.getLocation();
				brain.addArchonStart(loc);
			}
		}
	}
	
	private MapLocation groupArchons(RobotController rc, Brain brain) throws GameActionException {
		MapLocation[] archonStarts = brain.getArchonStarts();
		MapLocation center = Entity.findAverageOfLocations(archonStarts);
//		Direction dirTo = center.directionTo(rc.getLocation());
//		MapLocation mySpot = center.add(dirTo, 2);
		return center;
	}
}