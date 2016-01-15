package squadGoalsWithNeuts;

import java.util.Random;

import battlecode.common.*;

public class Archon {
	
	private Random rand;
	private boolean lookingForNeut = false;
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
			if (rc.senseNearbyRobots(35, Team.NEUTRAL).length>0){
				for(RobotInfo neut: rc.senseNearbyRobots(35, Team.NEUTRAL)){
					if(rc.getLocation().isAdjacentTo(neut.location)){
						rc.activate(neut.location);
					}
				}
				Entity.moveTowardLocation(rc, rc.senseNearbyRobots(35, Team.NEUTRAL)[0].location);
			}
			//Look for dens
			Entity.updateDenLocations(rc, brain);
			if (brain.getDenLocations().length >0){
				brain.goalLocation = brain.getDenLocations()[0];
			}
			//Look for NeutBots
			Entity.updateNeutLocations(rc, brain);
			if (brain.getNeutBots().length >0 && brain.goalLocation == null){
				brain.goalLocation = brain.getNeutBots()[0];
				lookingForNeut = true;
			}
			if (lookingForNeut == true && rc.getLocation().isAdjacentTo(brain.goalLocation)){
				rc.activate(brain.goalLocation);
				brain.goalLocation = null;
				lookingForNeut = false;
			}
			Squad.processSquadMessages(rc, brain);
			Squad.listenForIntersquadCommunication(rc, brain);
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
			
			//If you see another archon, share some info with him
			shareInfo(rc,  brain);
			
			//Look for nearby parts
			Entity.findPartsInRange(rc, brain, 35);
			if (rc.isCoreReady()){
				boolean inDanger = Entity.inDanger(enemies, rc.getLocation(), false);
				boolean moved = false;
				if (inDanger){
					moved = Entity.safeMove(rc, brain, enemies, Direction.NONE, false);
					if (!moved){
						Entity.moveRandomDirection(rc, brain);
					}
				}
				else if (rc.hasBuildRequirements(typeToBuild)) {
					tryBuildUnitInEmptySpace(rc, brain, typeToBuild,Direction.NORTH);
					//Otherwise, call out any dens if you see them
				} else if (brain.goalLocation!=null && rc.getLocation().distanceSquaredTo(brain.goalLocation) > rc.getType().sensorRadiusSquared){
					Entity.safeMove(rc, brain, enemies, brain.goalLocation, true);
					brain.removePartLocation(rc.getLocation());
				}
				else {
					archonMove(rc, brain);
					brain.removePartLocation(rc.getLocation());
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
	
	private void archonMove(RobotController rc, Brain brain) throws GameActionException {
		//Look for bad guys
		RobotInfo[] nearbyHostiles = rc.senseHostileRobots(rc.getLocation(),  rc.getType().sensorRadiusSquared);
		//If there are any bad guys, run away
		if (nearbyHostiles.length > 0 ) {
			RobotInfo enemy = nearbyHostiles[0];
			Direction dirToHostile = rc.getLocation().directionTo(enemy.location);
			Entity.safeMove(rc, brain, nearbyHostiles, dirToHostile.opposite(), true);
			//Otherwise, run around randomly
		} else if (brain.getPartLocations().length >0) {
			Entity.moveToLocation(rc, brain.getPartLocations()[0]);
		} else {
			Direction directionToMove = Entity.directions[rand.nextInt(8)];
			Entity.moveInDirection(rc, directionToMove);
		}
	}
	
	private void shareInfo(RobotController rc, Brain brain) throws GameActionException {
		RobotInfo[] friends = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam());
		for (RobotInfo friend : friends){
			if (friend.type == RobotType.ARCHON){
				MapLocation[] dens = brain.getDenLocations();
				//Share the location of a random den
				if (dens.length>0){
					Squad.shareDenLocation(rc, brain, dens[brain.rand.nextInt(dens.length)], 2*rc.getType().sensorRadiusSquared);
				}
			}
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