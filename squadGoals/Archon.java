package squadGoals;

import java.util.Random;

import battlecode.common.*;

public class Archon {
	
	private Random rand;
	public void run(RobotController rc, Brain brain) throws GameActionException{	
		rand = new Random(rc.getID());
		brain.initBuildHistory();
		while (true) {
			//			if (rc.isCoreReady()) {
			String print = "";
			RobotType typeToBuild = buildNextUnit(brain);
			for (Integer i : brain.getSquadMembers()){
				print = print + i.toString() + ", ";
			}
			rc.setIndicatorString(0, print);
			brain.thisTurnsSignals = rc.emptySignalQueue();
			//Look for dens
			MapLocation nearbyDen = Entity.searchForDen(rc);

			//Repair a nearby unit, if there are any
			repairUnits(rc);

			//Try to build a unit if you have the parts
			Squad.recruit(rc, brain);
			Squad.listenForRecruits(rc, brain);
			if (rc.isCoreReady()){
				if (rc.hasBuildRequirements(typeToBuild)) {
					tryBuildUnitInEmptySpace(rc, brain, typeToBuild,Direction.NORTH);
					//Otherwise, call out any dens if you see them
				} else if (!(nearbyDen.equals(rc.getLocation()))) {
					rc.setIndicatorString(3, "See den at" + nearbyDen.x + "," + nearbyDen.y);
					Squad.sendMoveCommand(rc, brain, nearbyDen);
					//Otherwise, move
				} else /*if (rc.isCoreReady())*/{
					archonMove(rc);
				}
				//				}
			}
			Clock.yield();

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
		RobotInfo[] adjacentFriendlies = rc.senseNearbyRobots(2, rc.getTeam());
		for (RobotInfo friendly : adjacentFriendlies){
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
	
	public RobotType buildNextUnit(Brain brain){
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