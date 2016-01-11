package squadGoals;

import java.util.Random;

import battlecode.common.*;

public class Archon {
	
	private Random rand;
	public void run(RobotController rc, Brain brain) throws GameActionException{	
		rand = new Random(rc.getID());
		RobotType typeToBuild = RobotType.SOLDIER;
		brain.initBuildHistory();
		while (true) {
			//			if (rc.isCoreReady()) {
			String print = "";
			for (Integer i : brain.getSquadMembers()){
				print = print + i.toString() + ", ";
			}
			rc.setIndicatorString(0, print);
			brain.thisTurnsSignals = rc.emptySignalQueue();
			//Look for dens
			Entity.updateDenLocations(rc, brain);
//			Entity.processSquadMessages(rc, brain);
			/*if (brain.getDenLocations().length >0){
				brain.goalLocation = brain.getDenLocations()[0];
			}*/
			Squad.processSquadMessages(rc, brain);
			if (!(brain.goalLocation == null) && rc.getLocation().distanceSquaredTo(brain.goalLocation) < 3){
				brain.goalLocation = null;
			}
			if (brain.goalLocation != null){
				Squad.sendMoveCommand(rc, brain, brain.goalLocation);
			} else {
				Squad.sendClearGoalLocationCommand(rc, brain);
			}
			//Repair a nearby unit, if there are any
			repairUnits(rc);
			RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), rc.getType().sensorRadiusSquared);

			//Try to build a unit if you have the parts
			Squad.recruit(rc, brain);
			Squad.listenForRecruits(rc, brain);
			if (rc.isCoreReady()){
				if (rc.hasBuildRequirements(typeToBuild)) {
					tryBuildUnitInEmptySpace(rc, brain, typeToBuild,Direction.NORTH);
					//Otherwise, call out any dens if you see them
				} else if (brain.goalLocation!=null && rc.getLocation().distanceSquaredTo(brain.goalLocation) > rc.getType().sensorRadiusSquared){
					Entity.safeMove(rc, brain, enemies, brain.goalLocation, true);
				}
				else {
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
		RobotInfo[] adjacentFriendlies = rc.senseNearbyRobots(rc.getType().attackRadiusSquared, rc.getTeam());
		//Get lowest health enemy
		double lowestHealth = 200;
		MapLocation loc = rc.getLocation();
		for (RobotInfo friendly : adjacentFriendlies){
			if (friendly.health < friendly.type.maxHealth && friendly.type!=RobotType.ARCHON && 
					friendly.health < lowestHealth) {
				lowestHealth = friendly.health;
				loc = friendly.location;
				break;
			}
		}
		if (loc != rc.getLocation()){
			rc.repair(loc);
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