package scoutLure;

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

		Brain brain = new Brain(startingLocation);
		boolean scouty = false;
		boolean scoutx = false;


		while (true){
			try{
				Entity.receiveMessages(rc, brain); 
				if (!brain.haveYScout && !scouty && !scoutx &&
						((brain.minHeight == (Integer) null) || (brain.maxHeight == (Integer) null))){
                	scouty = true;
                	rc.broadcastMessageSignal(4, 0, 80);
                	rc.setIndicatorString(0, "became y scout");
                }
                else if (!brain.haveXScout && !scoutx && !scouty && 
                		((brain.minWidth == (Integer) null) || (brain.maxWidth == (Integer) null))){
                	rc.broadcastMessageSignal(5, 0, 80);
                	rc.setIndicatorString(0, "became x scout");

                	scoutx = true;
                }
				scout(rc, brain, scoutx, scouty);
				MapLocation currentLocation = rc.getLocation();
				
				boolean move = false;
    			boolean stop = false;
    			boolean archonClose = false;
    			RobotInfo[] zombiesWithinRange = rc.senseNearbyRobots(RobotType.SCOUT.sensorRadiusSquared, Team.ZOMBIE);
    			RobotInfo[] enemiesWithinRange = rc.senseNearbyRobots(RobotType.SCOUT.sensorRadiusSquared, enemyTeam);
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
    			
    			Direction moveDir = Direction.SOUTH_EAST;
    			
    			Entity.moveTowards(rc, moveDir);

    			if (rc.isCoreReady() && move && ! stop ){
    				Entity.moveTowards(rc, moveDir);
    			}
    			else if (rc.isCoreReady() && !stop && !archonClose){
    				int fate = rand.nextInt(1000);
    				Direction dirToMove = directions[fate%8];
    				if (rc.canMove(dirToMove)){
    					rc.move(dirToMove);
    				}
    			}
    			if (stop && rc.isInfected()){
    				rc.disintegrate();
    			}
				

				
			}catch (Exception e){
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	private static void scout(RobotController rc, Brain brain, boolean scoutx, boolean scouty) throws GameActionException{
		MapLocation currentLocation = rc.getLocation();
		int power =  Entity.calculatePower(currentLocation, rc.getType().sensorRadiusSquared, brain.startLocation) + 1;
		if (scoutx){
			if ((brain.maxWidth == (Integer) null)){
				for (int i = 0; i < 8; i ++){
					if (!rc.onTheMap(currentLocation.add(Direction.EAST, i))){
						brain.maxWidth = currentLocation.x - 1;
						rc.broadcastMessageSignal(2, brain.maxWidth, power);
					}
				}
				if ((brain.maxWidth == (Integer) null) && rc.canMove(Direction.EAST) && rc.isCoreReady()){
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
				if ((brain.minWidth == (Integer) null) && rc.canMove(Direction.WEST) && rc.isCoreReady()){
					Entity.moveTowards(rc, Direction.WEST);
				}
			}
			else {
				scoutx = false;
			}
		}
		else if (scouty){
			if (brain.maxHeight == (Integer) null){
				for (int i = 0; i < 8; i ++){
					if (!rc.onTheMap(currentLocation.add(Direction.NORTH, i))){
						
						brain.maxHeight = currentLocation.y + 1;
						rc.broadcastMessageSignal(0, brain.maxHeight, power);
					}
				}
				if ((brain.maxHeight == (Integer) null) && rc.canMove(Direction.NORTH) && rc.isCoreReady()){
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
				if ((brain.minHeight == (Integer) null) && rc.canMove(Direction.SOUTH) && rc.isCoreReady()){
					Entity.moveTowards(rc, Direction.SOUTH);
				}
			}
			else{
				scouty = false;
			}
		}
	}
}