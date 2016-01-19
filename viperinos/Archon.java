package viperinos;


import battlecode.common.*;

public class Archon {
	
	public void run(RobotController rc, Brain brain) throws GameActionException{
		brain.lastDirectionMoved = Entity.directions[brain.rand.nextInt(8)];
		RobotType typeToBuild = nextUnitToBuild(brain, new RobotInfo[0]);
		if (rc.getTeamParts() > 250){
			tryToBuild(rc, RobotType.VIPER, Direction.NORTH);
		}

		while (true){

			brain.thisTurnsSignals = rc.emptySignalQueue();
			
			RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam().opponent());
			RobotInfo[] zombies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, Team.ZOMBIE);
			RobotInfo[] allies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam());
			RobotInfo[] neutrals = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, Team.NEUTRAL);
			RobotInfo closestEnemy = Entity.findClosestHostile(rc, enemies, zombies);
			
			Entity.trackDens(rc, brain, zombies);
			Entity.trackArchons(rc, brain, enemies);
			Entity.trackNeutrals(rc, brain, neutrals);
			
			Squad.listenForInformation(rc, brain);
			
			repair(rc);
			
			if (rc.isCoreReady()){
				if (Entity.fleeEnemies(rc,brain,enemies,zombies, closestEnemy)){
						rc.setIndicatorString(1, "fleeing zombie");
						Squad.sendDirectionToMove(rc, brain, brain.lastDirectionMoved);
				} else if (activateNeutrals(rc,brain,brain.neutrals)){
					rc.setIndicatorString(1, "activating neutrals");
					Squad.sendDirectionToMove(rc, brain, brain.lastDirectionMoved);
				} else if (tryToBuild(rc, typeToBuild, Direction.NORTH)){
					rc.setIndicatorString(1, "building robot");
					typeToBuild = nextUnitToBuild(brain, allies);
				} else if (grabParts(rc, brain)){
					rc.setIndicatorString(1, "grabbing parts");
					Squad.sendDirectionToMove(rc, brain, brain.lastDirectionMoved);
				} else if (huntDens(rc,brain)){
					rc.setIndicatorString(1, "hunting dens");
					Squad.sendDirectionToMove(rc, brain, brain.lastDirectionMoved);
				} else if (chaseArchons(rc,brain, allies)){
					Squad.sendDirectionToMove(rc, brain, brain.lastDirectionMoved);
				} else {
					rc.setIndicatorString(1, "just chillin");
					Entity.move(rc, brain, brain.lastDirectionMoved, false);
					Squad.sendDirectionToMove(rc, brain, brain.lastDirectionMoved);
				}
			}
			rc.setIndicatorString(0, "Dens at : " + brain.locListToString(brain.denLocations));
			rc.setIndicatorString(1, "Archons : " + brain.archonsToString());
			Clock.yield();
		}
	}
	
	
	
	/*
	 * nextUnitToBuild builds the next unit up in your build order.
	 * First, it iterates through startBuildArray once, and then
	 * it repeatedly builds through mainBuildArray
	 */
	public RobotType nextUnitToBuild(Brain brain, RobotInfo[] allies){
		RobotType robotToBuild;
		if(brain.initialIteration && brain.startBuildArray.length>0){
			robotToBuild = brain.startBuildArray[brain.buildCount];
			brain.buildCount++;
			if(brain.buildCount >= brain.startBuildArray.length){
				brain.initialIteration =false;
			}
		} else if (brain.buildCount % 15 == 0){
			robotToBuild = RobotType.SCOUT;
			brain.buildCount++;
		} else {
			if (allies.length == 0) {
				robotToBuild = RobotType.SOLDIER;
			} else {
				Double[] currentDist = calculateCurrentDist(allies);
				int maxIndex = 0;
				Double maxDiff = 0.0;
				for(int i = 0; i < 5; i++){
					double diff = brain.buildDist[i] - currentDist[i];
					if(diff > maxDiff){
						maxDiff = diff;
						maxIndex = i;
					}
				}
				robotToBuild = brain.robotsToBuild[maxIndex];
			}
			brain.buildCount++;
		}
		return robotToBuild;
	}
	
	public Double[] calculateCurrentDist(RobotInfo[] allies){
		int totalAllies = allies.length;
		double soldierCount = 0.0;
		double viperCount = 0.0;
		double guardCount = 0.0;
		double scoutCount = 0.0;
		double turretCount = 0.0;
		for(RobotInfo ally : allies){
			switch (ally.type) {
			case GUARD:
				guardCount++;
				break;
			case SCOUT:
				scoutCount++;
				break;
			case SOLDIER:
				soldierCount++;
				break;
			case TTM:
			case TURRET:
				turretCount++;
				break;
			case VIPER:
				viperCount++;
				break;
			default:
				break;
			}
		}
		Double[] returnArray = {soldierCount/totalAllies, viperCount/totalAllies, guardCount/totalAllies, scoutCount/totalAllies, turretCount/totalAllies};
		return returnArray;
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
	
	
	/*
	 * repairUnits looks for damaged, adjacent friendly units, and repairs the non-archon unit it sees
	 */
	private void repair(RobotController rc) throws GameActionException {
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
	
	/*
	 * grabParts looks for locations with parts within sensor range, and bugs toward the closest location.
	 * It returns true if the robot moved, and fasle otherwise
	 */
	public boolean grabParts(RobotController rc, Brain brain) throws GameActionException{
		boolean moved = false;
		MapLocation[] parts = rc.sensePartLocations(rc.getType().sensorRadiusSquared);
		MapLocation closestPart = null;
		int minDistance = 5000;
		for (MapLocation loc : parts){
			int distanceTo = rc.getLocation().distanceSquaredTo(loc);
			if (distanceTo < minDistance && rc.senseRubble(loc) < GameConstants.RUBBLE_OBSTRUCTION_THRESH){
				distanceTo = minDistance;
				closestPart = loc;
			}
		}
		if (closestPart != null){
			Entity.move(rc, brain, rc.getLocation().directionTo(closestPart), true);
			moved = true;
		}
		return moved;
	}
	
	/*
	 * activateNeutrals takes a list of nearby neutral robots, and activates any that are in range. Otherwise, it moves toward the closest neutral.
	 * It returns true if the robot moved or activated a robot, and false otherwise
	 */
	public boolean activateNeutrals(RobotController rc, Brain brain, RobotInfo[] neutrals) throws GameActionException{
		boolean activated = false;
		RobotInfo closestNeutral = null;
		int distanceToClosest = 5000;
		for (RobotInfo neutral:neutrals){
			if(neutral!=null){
				int distanceTo = rc.getLocation().distanceSquaredTo(neutral.location);
				if ( distanceTo <= 2){
					rc.activate(neutral.location);
					activated = true;
					break;
				} else if (distanceTo < distanceToClosest){
					distanceToClosest = distanceTo;
					closestNeutral = neutral;
				}
			}
		}
		if (!activated && closestNeutral != null && distanceToClosest <= 100){
			Entity.move(rc, brain, rc.getLocation().directionTo(closestNeutral.location), true);
			activated = true;
		}
		return activated;
	}
	
	/*
	 * huntDens moves toward the closest non dead den
	 */
	public boolean huntDens(RobotController rc, Brain brain) throws GameActionException{
		boolean moved = false;
		MapLocation closestDen = null;
		int minDistance = 50000;
		for (MapLocation den : brain.denLocations){
			if (!brain.isDenDead(den)){
				int distanceTo = rc.getLocation().distanceSquaredTo(den);
				if (distanceTo < minDistance){
					closestDen = den;
					minDistance = distanceTo;
				}
			}
		}
		if (closestDen!=null && minDistance > 20){
			Entity.moveLimited(rc, brain, rc.getLocation().directionTo(closestDen));
			moved = true;
		} else if (minDistance <= 20){
			moved=true;
		}
		return moved;
	}
	
	/*
	 * 
	 */
	public boolean chaseArchons(RobotController rc, Brain brain, RobotInfo[] allies) throws GameActionException{
		boolean moved = false;
		if (allies.length > 8){
			for (int id : brain.archonIds){
				if (id != 0 ){
					MapLocation lastKnownArchonInfo = brain.enemyInfo[id];
					if (lastKnownArchonInfo != null){
						Entity.moveLimited(rc, brain, rc.getLocation().directionTo(lastKnownArchonInfo));
						if (rc.getLocation().equals(lastKnownArchonInfo)){
							brain.enemyInfo[id] = null;
						}
						moved = true;
						break;
					}
				}
			}
		}
		return moved;
	}
}