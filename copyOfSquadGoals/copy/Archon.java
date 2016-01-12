package copyOfSquadGoals.copy;


import battlecode.common.*;

public class Archon {
	
	public void run(RobotController rc, Brain brain) throws GameActionException{	
		brain.initBuildHistory();
		Direction currentDirection = Entity.directions[brain.rand.nextInt(8)];
		while (true) {

			RobotType typeToBuild = buildNextUnit(brain);
			brain.thisTurnsSignals = rc.emptySignalQueue();
			Squad.listenForIntersquadCommunication(rc, brain);
			//Look for dens
			RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), rc.getType().sensorRadiusSquared);

			Entity.updateDenLocations(rc, brain);
			boolean memberRequestedHelp = Squad.processSquadMessages(rc, brain);
			if (Entity.inDanger(enemies, rc.getLocation(), true)){
				Squad.sendMoveCommand(rc, brain, rc.getLocation());
			} else if (memberRequestedHelp) {
				Squad.sendMoveCommand(rc, brain, brain.goalLocation);
//				if (brain.goalLocation!=null) {
//					rc.setIndicatorString(1, "Friend is Goal: " + brain.goalLocation.x + ", " + brain.goalLocation.y);
//				}
			} else if (brain.getDenLocations().length >0){
				brain.goalLocation = Entity.getNearestDen(rc, brain);
				Squad.sendAttackDenCommand(rc, brain, brain.goalLocation);
//				if (brain.goalLocation!=null) {
//					rc.setIndicatorString(1, "Den is Goal: " + brain.goalLocation.x + ", " + brain.goalLocation.y);
//				}
			} else {
				Squad.sendClearGoalLocationCommand(rc, brain);
			}
			if (!(brain.goalLocation == null) && rc.getLocation().distanceSquaredTo(brain.goalLocation) < 3){
				brain.goalLocation = null;
			}
			//Repair a nearby unit, if there are any
			repairUnits(rc);
			RobotInfo[] neutrals = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, Team.NEUTRAL);
			//Recruit new squad members
			Squad.recruit(rc, brain);
			Squad.listenForRecruits(rc, brain);
			
			//If you see another archon, share some info with him
			//shareInfo(rc,  brain);
			
			//Look for nearby parts
			Entity.findPartsInRange(rc, brain, 35);
			if (rc.isCoreReady()){
				boolean inDanger = Entity.inDanger(enemies, rc.getLocation(), true);
				boolean moved = false;
				if (inDanger){
					Squad.sendHelpMessage(rc, brain, 10*rc.getType().sensorRadiusSquared);
					moved = Entity.safeMove(rc, brain, enemies, Direction.NONE, true);
					if (!moved){
						Entity.moveTowards(rc, Entity.awayFromEnemies(rc, enemies, brain).opposite());
					}
				} 
				else if ( enemies.length > 0 && rc.getLocation().distanceSquaredTo(Entity.findClosestEnemy(
						rc, brain, enemies, rc.getLocation()).location) <= 24 ){
					Squad.sendMoveCommand(rc, brain, rc.getLocation());
				} else if (enemies.length > 0 && rc.getLocation().distanceSquaredTo(Entity.findClosestEnemy(
					rc, brain, enemies, rc.getLocation()).location) <= 24 ){
					moved = Entity.safeMove(rc, brain, enemies, Direction.NONE, true);
					if (!moved){
						Entity.moveTowards(rc, Entity.awayFromEnemies(rc, enemies, brain).opposite());
					}
				} else if (activateAdjacentNeutralRobots(rc,brain, neutrals)){
					rc.setIndicatorString(0, "Activated Robot");
				} else if (rc.hasBuildRequirements(typeToBuild)) {
					tryBuildUnitInEmptySpace(rc, brain, typeToBuild,Direction.NORTH);
				} else if (brain.goalLocation!=null && rc.getLocation().distanceSquaredTo(brain.goalLocation) > rc.getType().sensorRadiusSquared){
					Entity.moveTowards(rc, rc.getLocation().directionTo(brain.goalLocation));
//					Entity.safeMove(rc, brain, enemies, brain.goalLocation, false);
					brain.removePartLocation(rc.getLocation());
				}
				else if (brain.goalLocation != null){
					Entity.safeMove(rc, brain, enemies, rc.getLocation().directionTo(brain.goalLocation), true);
				}
				else {
					moved = archonMove(rc, brain, currentDirection, neutrals);
					if (!moved){
						currentDirection = Entity.directions[brain.rand.nextInt(8)];
					}
					brain.removePartLocation(rc.getLocation());
				}
			}
			
			if (brain.goalLocation==null) {
				rc.setIndicatorString(1, "No goal yet");
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
	
	private boolean archonMove(RobotController rc, Brain brain, Direction currentDirection, RobotInfo[] neutrals) throws GameActionException {
		//Look for bad guys
		RobotInfo[] nearbyHostiles = rc.senseHostileRobots(rc.getLocation(),  rc.getType().sensorRadiusSquared);
		//If there are any bad guys, run away
		if (nearbyHostiles.length > 0 ) {
			RobotInfo enemy = nearbyHostiles[0];
			Direction dirToHostile = rc.getLocation().directionTo(enemy.location);
			Entity.safeMove(rc, brain, nearbyHostiles, dirToHostile.opposite(), true);
			//Otherwise, run around randomly
			return true;
		} else if (neutrals.length>0){
			Entity.moveToLocation(rc, neutrals[0].location);
			return true;
		}else if (brain.getPartLocations().length >0) {
			Entity.moveToLocation(rc, brain.getPartLocations()[0]);
			return true;
		} else {
			if (rc.canMove(currentDirection)){
				rc.move(currentDirection);
				return true;
			} else {
				return false;
			}
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
	
	private boolean activateAdjacentNeutralRobots(RobotController rc, Brain brain, RobotInfo[] neutrals) throws GameActionException {
		for (RobotInfo neutral : neutrals){
			if (rc.getLocation().isAdjacentTo(neutral.location)){
				rc.activate(neutral.location);
				return true;
			}
		}
		return false;
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