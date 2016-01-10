package scoutLure;

import java.util.HashSet;
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
		Optional<Direction> currentDir = Optional.empty();
		Brain brain = new Brain(startingLocation);
		boolean isBaiting = false;
		int distanceToArchon = 90;

		while (true){
			try{
				rc.setIndicatorString(0, "new Round");
				Entity.receiveMessages(rc, brain); 
				if (rc.getRoundNum() == 50){
					brain.archonLocations = new HashSet<MapLocation>();
				}
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
//					Optional<MapLocation> closestArchon = Entity.findClosestArchon(rc, brain);
					RobotInfo[] allies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam());
					if (!brain.enemyBaseFound){
						lureDir = robotLocation.directionTo(startingLocation).opposite();
					}
					else{
						lureDir = robotLocation.directionTo(brain.enemyBase);
					}
					if ((isBaiting && robotLocation.distanceSquaredTo(startingLocation) > distanceToArchon)
							&& (Entity.findDistanceClosestZombie(rc) < 13 ||
									Entity.canBeAttacked(rc, robotLocation, Team.ZOMBIE))){
						isBaiting = true;
						Entity.moveAvoidArchons(rc, lureDir, brain, distanceToArchon);
					} else if ( isBaiting && robotLocation.distanceSquaredTo(startingLocation) > distanceToArchon){
						//Being chased, but no zombies close enough.  Wait to agro zombies then head to enemy base
						int range = RobotType.SCOUT.sensorRadiusSquared*2;
						if (allies.length > 0 && zombiesWithinRange.length < 5){
							for (RobotInfo zombie : zombiesWithinRange){
								rc.broadcastMessageSignal(12, zombie.ID, range);
							}
						}
					}
					else if (isBaiting){
						if (currentDir.isPresent()){
							Entity.moveTowards(rc, currentDir.get());
						}
					}
					else if (robotLocation.distanceSquaredTo(startingLocation) > distanceToArchon){
						isBaiting = true;
						rc.setIndicatorString(0, "trying to bait zombies");
						Entity.moveAvoidArchons(rc, lureDir, brain, distanceToArchon);
					}
					else if (zombiesWithinRange.length > 0){
						//We need to check all zombies and see if we should head towards them
						if (!currentDir.isPresent()){
							for (RobotInfo zombie : zombiesWithinRange){
								Direction dirToZombie = robotLocation.directionTo(zombie.location);
								boolean allyInDirection = false;
								for (RobotInfo ally : allies){
									if (robotLocation.directionTo(ally.location) == dirToZombie 
											&& ally.type == RobotType.SCOUT && ally.coreDelay < 1){
										allyInDirection = true;
									}
								}
								if (brain.lastLuredDirection[Entity.getSignalFromDirection(dirToZombie)] + 20
										< rc.getRoundNum()  && (rc.canMove(dirToZombie) || 
												rc.canMove(dirToZombie.rotateLeft()) 
												|| rc.canMove(dirToZombie.rotateRight()))
												&& /*!allyInDirection &&*/ !brain.taggedZombies.contains(zombie.ID)){
									rc.broadcastMessageSignal(10, Entity.getSignalFromDirection(dirToZombie), 200);
									rc.setIndicatorString(0, "claiming zombie");
									currentDir = Optional.of(dirToZombie);
								}
							}
						}
						if (currentDir.isPresent()){
							Entity.moveTowards(rc, currentDir.get());
						}
					} else {
						//Case where no zombies present, dont move
						int numNearbyAllies = 0;
						for (RobotInfo ally : allies){
							if (ally.location.distanceSquaredTo(robotLocation)< 3){
								numNearbyAllies ++;
							}
						}
						if (numNearbyAllies > 2){
							Direction randomDir = Entity.directions[rand.nextInt(8)];
							Entity.moveTowards(rc, randomDir);
						}
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
				if (rc.getRoundNum() % 5 == 0){
					rc.broadcastMessageSignal(5, 0, 1000);
				}
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
			if (rc.getRoundNum() % 5 == 0){
				rc.broadcastMessageSignal(4, 0, 1000);
			}
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