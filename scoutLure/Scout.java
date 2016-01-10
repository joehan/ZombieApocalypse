package scoutLure;

import java.util.Optional;
import java.util.Random;
import scoutLure.Entity;

import battlecode.common.*;

public class Scout {
	
	public static void run(RobotController rc) throws GameActionException{
		Team myTeam = rc.getTeam();
        Team enemyTeam = myTeam.opponent();
        Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
                Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
//        Random rand = new Random(rc.getID());
		MapLocation startingLocation = rc.getLocation();
//		Optional<MapLocation> guard = Optional.empty();
//		boolean baitingZombies = false;
//		boolean scoutingZombieDir = false;
		Optional<Direction> currentDir = Optional.empty();


		
		Brain brain = new Brain(startingLocation);


		while (true){
			try{
				Entity.receiveMessages(rc, brain); 
				RobotInfo[] enemiesWithinRange = rc.senseNearbyRobots(RobotType.SCOUT.sensorRadiusSquared, enemyTeam);
				if (enemiesWithinRange.length > 5 && !brain.enemyBaseFound){
					brain.enemyBaseFound = true;
					brain.enemyBase = enemiesWithinRange[0].location;
					rc.broadcastMessageSignal(6, Entity.convertMapToSignal(brain.enemyBase), 2000);
				}
				RobotInfo[] zombiesWithinRange = rc.senseNearbyRobots(RobotType.SCOUT.sensorRadiusSquared, Team.ZOMBIE);
				for (RobotInfo zombie : zombiesWithinRange){
					if (zombie.type == RobotType.ZOMBIEDEN && !brain.denLocations.contains(zombie.location)){
						MapLocation den = zombie.location;
						brain.denLocations.add(den);
						rc.broadcastMessageSignal(7, Entity.convertMapToSignal(den), 2000);
					}
				}

				
				if (!brain.haveYScout && !brain.scouty && !brain.scoutx &&
						((brain.minHeight == (Integer) null) || (brain.maxHeight == (Integer) null))){
                	brain.scouty = true;
                	rc.broadcastMessageSignal(4, 0, 500);
                	rc.setIndicatorString(0, "became y scout");
                }
                else if (!brain.haveXScout && !brain.scoutx && !brain.scouty && 
                		((brain.minWidth == (Integer) null) || (brain.maxWidth == (Integer) null))){
                	rc.broadcastMessageSignal(5, 0, 500);
                	rc.setIndicatorString(0, "became x scout");
                	brain.scoutx = true;
                }
				if (brain.scouty || brain.scoutx){
					scout(rc, brain);
				}
				else {
					MapLocation robotLocation = rc.getLocation();
					Direction lureDir;
					Optional<RobotInfo> closestArchon = Entity.findClosestArchon(rc);
					if (!brain.enemyBaseFound){
						lureDir = robotLocation.directionTo(startingLocation).opposite();
					}
					else{
						lureDir = robotLocation.directionTo(brain.enemyBase);
					}
					
					if ((!closestArchon.isPresent() || robotLocation.distanceSquaredTo(closestArchon.get().location)> 50)
							&& Entity.findDistanceClosestZombie(rc) < 8){
						Entity.moveAvoidArchons(rc, lureDir);
					}
					else if (!closestArchon.isPresent() || robotLocation.distanceSquaredTo(closestArchon.get().location)> 50){
						//Being chased, but no zombies close enough.  Wait to agro zombies then head to enemy base
					}
					else if (zombiesWithinRange.length > 0){
						if (!currentDir.isPresent()){
							currentDir = Optional.of(robotLocation.directionTo(zombiesWithinRange[0].location));
						}
						Entity.moveTowards(rc, currentDir.get());
					} else {
						//Case where no zombies present, dont move
					}
				}
				Clock.yield();
			}catch (Exception e){
				System.out.println(e.getMessage());
					e.printStackTrace();
				}
			}
		}

		private static void scout(RobotController rc, Brain brain) throws GameActionException{
			MapLocation currentLocation = rc.getLocation();
			int power = currentLocation.distanceSquaredTo(brain.startLocation) + 100;
			if (brain.scoutx){
				if ((brain.maxWidth == (Integer) null)){
					for (int i = 0; i < 8; i ++){
						if (!rc.onTheMap(currentLocation.add(Direction.EAST, i))){
							brain.maxWidth = currentLocation.x - 1;
							rc.broadcastMessageSignal(2, brain.maxWidth, power);
						}
					}
					if ((brain.maxWidth == (Integer) null) && rc.isCoreReady()){
						Entity.moveTowards(rc, Direction.EAST);
					}
				}
				else if (brain.minWidth == (Integer) null){
					for (int i = 0; i < 8; i ++){
					if (!rc.onTheMap(currentLocation.add(Direction.WEST, i))){
						brain.minWidth = currentLocation.x + 1;
						rc.broadcastMessageSignal(3, brain.minWidth, power);
					}
				}
				if ((brain.minWidth == (Integer) null) && rc.isCoreReady()){
					Entity.moveTowards(rc, Direction.WEST);
				}
			}
			else {
				rc.setIndicatorString(0, "not scoutx");
				brain.scoutx = false;
			}
		}
		else if (brain.scouty){
			if (brain.maxHeight == (Integer) null){
				for (int i = 0; i < 8; i ++){
					if (!rc.onTheMap(currentLocation.add(Direction.NORTH, i))){
						
						brain.maxHeight = currentLocation.y + 1;
						rc.broadcastMessageSignal(0, brain.maxHeight, power);
					}
				}
				if ((brain.maxHeight == (Integer) null) && rc.isCoreReady()){
					Entity.moveTowards(rc, Direction.NORTH);
				}
			}
			else if (brain.minHeight == (Integer) null){
				for (int i = 0; i < 8; i ++){
					if (!rc.onTheMap(currentLocation.add(Direction.SOUTH, i))){
						brain.minHeight = currentLocation.y - 1;
						rc.broadcastMessageSignal(1, brain.minHeight, power);
					}
				}
				if ((brain.minHeight == (Integer) null) && rc.isCoreReady()){
					Entity.moveTowards(rc, Direction.SOUTH);
				}
			}
			else{
				rc.setIndicatorString(0, "not scouty");
				brain.scouty = false;
			}
		}
	}
}