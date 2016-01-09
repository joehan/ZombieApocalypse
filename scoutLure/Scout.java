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
        Random rand = new Random(rc.getID());
		MapLocation startingLocation = rc.getLocation();
		int fate = rand.nextInt(1000);
		Optional<MapLocation> guard = Optional.empty();
		boolean baitingZombies = false;
		boolean scoutingZombieDir = false;
		Direction currentDir = directions[fate%8];


		
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
					if (scoutingZombieDir){
						for (RobotInfo zombie : zombiesWithinRange){
							if (zombie.type == RobotType.ZOMBIEDEN){
								scoutingZombieDir = false;
								baitingZombies = true;
							}
						}
						Entity.moveTowards(rc, currentDir);
					}
					else if (baitingZombies){
						Direction dirToMove;
						if (brain.enemyBaseFound){
							dirToMove = robotLocation.directionTo(brain.enemyBase);
						}
						else {
							dirToMove = robotLocation.directionTo(startingLocation).opposite();
						}
						for (RobotInfo enemyInfo : zombiesWithinRange){
							if (robotLocation.distanceSquaredTo(enemyInfo.location) < 8){
								rc.setIndicatorString(0, "enemy nearby, should have moved");
								Entity.moveTowards(rc, dirToMove);
							}
						}
					}
					else if (brain.denLocations.size() == brain.denGuarded.size()){
						//Don't have an available den to go to: just sit and wait
						if (zombiesWithinRange.length > 0 ){
							rc.setIndicatorString(0, "scouting zombie dir");
							scoutingZombieDir = true;
							currentDir = robotLocation.directionTo(zombiesWithinRange[0].location);
							Entity.moveTowards(rc, currentDir);
						}
							
					}else if (!guard.isPresent() && !baitingZombies){
						for (MapLocation denLoc : brain.denLocations){
							if (!brain.denGuarded.contains(denLoc) && robotLocation.distanceSquaredTo(denLoc) < 900){
								guard = Optional.of(denLoc);
								rc.broadcastMessageSignal(8, Entity.convertMapToSignal(denLoc), 900);
								rc.setIndicatorString(0, "going to guard");
								brain.denGuarded.add(denLoc);
							}
						}
					}
					else if (guard.isPresent()){
						for (RobotInfo enemyInfo : zombiesWithinRange){
							if (robotLocation.distanceSquaredTo(enemyInfo.location) < 13){
								//Now we need to start luring to enemy base
								baitingZombies = true;
								rc.broadcastMessageSignal(9, Entity.convertMapToSignal(guard.get()), 900);
								guard = Optional.empty();
								rc.setIndicatorString(0, "now baiting zombies");
								break;
							}
						}
						if (guard.isPresent() && robotLocation.distanceSquaredTo(guard.get()) > 13){
							Entity.moveTowards(rc, robotLocation.directionTo(guard.get()));
							rc.setIndicatorString(0, "should have just moved");
							
						}
					}
				}
				Clock.yield();

					
					
					
					
					
					
					
					/*
					MapLocation currentLocation = rc.getLocation();
					boolean move = false;
					boolean stop = false;
					boolean archonClose = false;
//					RobotInfo[] zombiesWithinRange = rc.senseNearbyRobots(RobotType.SCOUT.sensorRadiusSquared, Team.ZOMBIE);
					//					RobotInfo[] enemiesWithinRange = rc.senseNearbyRobots(RobotType.SCOUT.sensorRadiusSquared, enemyTeam);
					RobotInfo[] alliesWithinRange = rc.senseNearbyRobots(RobotType.SCOUT.sensorRadiusSquared, myTeam);

					for (RobotInfo zombie : zombiesWithinRange){
						if (zombie.location.distanceSquaredTo(currentLocation) < 24){
							move = true;
						}
					}
					for (RobotInfo enemy : enemiesWithinRange){
						if (enemy.location.distanceSquaredTo(currentLocation) < 15){
							move = false;
							stop = true;
						}
					}
					for (RobotInfo ally : alliesWithinRange){
						if (ally.location.distanceSquaredTo(currentLocation) < 48 && ally.type == RobotType.ARCHON){
							archonClose = true;
						}
					}
					if (!brain.enemyBaseFound){
						Entity.moveSemiRandom(rc, currentDir);
					}
					else {
						if (archonClose && zombiesWithinRange.length == 0){

						}
						else{
							Direction moveDir = rc.getLocation().directionTo(brain.enemyBase);
							Entity.moveTowards(rc, moveDir);

							if (rc.isCoreReady() && move && ! stop ){
								Entity.moveTowards(rc, moveDir);
							}
							else if (rc.isCoreReady() && !stop && !archonClose){
								fate = rand.nextInt(1000);
								Direction dirToMove = directions[fate%8];
								if (rc.canMove(dirToMove)){
									rc.move(dirToMove);
								}
							}
							if (stop && rc.isInfected()){
								rc.disintegrate();
							}
						}
					}
				}*/
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