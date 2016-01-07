package scoutBait;

import battlecode.common.*;

import java.util.Random;

public class RobotPlayer {
	
//	private static MapLocation flipLocation(MapLocation original){
////		original.add(Direction.NORTH, Ma)
//	}
	
	private static void moveTowards(RobotController rc, Direction dir){
		if (rc.isCoreReady()){
			try {
				if (rc.canMove(dir)){
					rc.move(dir);

				}
				else if (rc.canMove(dir.rotateLeft())){
					rc.move(dir.rotateLeft());
				}
				else if (rc.canMove(dir.rotateRight())){
					rc.move(dir.rotateRight());
				}
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();

			}
		}
	}

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) {
        // You can instantiate variables here.
        Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
                Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
        RobotType[] robotTypes = {RobotType.SCOUT, RobotType.SOLDIER, RobotType.SOLDIER, RobotType.SOLDIER,
                RobotType.GUARD, RobotType.GUARD, RobotType.VIPER, RobotType.TURRET};
        Random rand = new Random(rc.getID());
        int myAttackRange = 0;
        Team myTeam = rc.getTeam();
        Team enemyTeam = myTeam.opponent();

        if (rc.getType() == RobotType.ARCHON) {
        	int maxHeight, minHeight, maxWidth, minWidth;
        	maxHeight = minHeight = maxWidth = minWidth = 0;
        	boolean maxHeightFound, minHeightFound, maxWidthFound, minWidthFound;
    		maxHeightFound = minHeightFound = maxWidthFound = minWidthFound = false;
            try {
                // Any code here gets executed exactly once at the beginning of the game.
            	
            } catch (Exception e) {
                // Throwing an uncaught exception makes the robot die, so we need to catch exceptions.
                // Caught exceptions will result in a bytecode penalty.
                System.out.println(e.getMessage());
                e.printStackTrace();
            }

            while (true) {
                // This is a loop to prevent the run() method from returning. Because of the Clock.yield()
                // at the end of it, the loop will iterate once per game round.
                try {
                    int fate = rand.nextInt(1000);
                    
                    Signal[] signals = rc.emptySignalQueue();
                    for (Signal signal: signals){
                    	if (signal.getTeam() == myTeam){
                    		int[] messages = signal.getMessage();
                    		switch(messages[0]) {
                    		case 0:
                    			if (!maxHeightFound){
                    				maxHeightFound = true;
                    				maxHeight = messages[1];
                    			}
                    			break;
                    		
                    		case 1:
                    			if (!minHeightFound){
                    				minHeightFound = true;
                    				minHeight = messages[1];
                    			}
                    			break;
                    		case 2:
                    			if (!maxWidthFound){
                    				maxWidthFound = true;
                    				maxWidth = messages[1];
                    			}
                    			break;
                    		case 3:
                    			if (!minWidthFound){
                    				minWidthFound = true;
                    				minWidth = messages[1];
                    			}
                    			break;
                    		}
                    		
                    	}
                    }
                    if (rc.getRoundNum() % 10 == 0){
                    	System.out.println(maxHeightFound);
                    	System.out.println(minHeightFound);
                    	System.out.println(maxWidthFound);
                    	System.out.println(minWidthFound);

                    	if (maxHeightFound){
                    		rc.broadcastMessageSignal(0, maxHeight, 80);
                    	}
                    	if (minHeightFound){
                    		rc.broadcastMessageSignal(1, minHeight, 80);
                    		rc.setIndicatorString(0, "sent message about min Height on round:" + rc.getRoundNum());

                    	}
                    	if (maxWidthFound){
                    		rc.broadcastMessageSignal(2, maxWidth, 80);
                    	}
                    	if (minWidthFound){
                    		rc.broadcastMessageSignal(3, minWidth, 80);
                    	}
                    }
                    /*if (signals.length > 0) {
                        // Set an indicator string that can be viewed in the client
                        rc.setIndicatorString(0, "I received a signal this turn!");
                    } else {
                        rc.setIndicatorString(0, "I don't any signal buddies");
                    }*/
                    if (rc.isCoreReady()) {
                            RobotType typeToBuild = RobotType.SCOUT;
                            // Check for sufficient parts
                            if (rc.hasBuildRequirements(typeToBuild) && fate < 10 || rc.getRoundNum() == 1) {
                                // Choose a random direction to try to build in
                                Direction dirToBuild = directions[rand.nextInt(8)];
                                for (int i = 0; i < 8; i++) {
                                    // If possible, build in this direction
                                    if (rc.canBuild(dirToBuild, typeToBuild)) {
                                        rc.build(dirToBuild, typeToBuild);
                                        break;
                                    } else {
                                        // Rotate the direction to try
                                        dirToBuild = dirToBuild.rotateLeft();
                                    }
                                }
                            }
                        }
//                    }

                    Clock.yield();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        else if (rc.getType() == RobotType.SCOUT ){
        	int maxHeight, minHeight, maxWidth, minWidth;
    		boolean maxHeightFound, minHeightFound, maxWidthFound, minWidthFound;
    		maxHeightFound = minHeightFound = maxWidthFound = minWidthFound = false;
    		boolean scoutx = false;
    		boolean scouty = false;
    		boolean haveYScout = false;
    		boolean haveXScout = false;
    		MapLocation startingLocation;
//    		Team myTeam = rc.getTeam();
//            Team enemyTeam = myTeam.opponent();

        	try {
                // Any code here gets executed exactly once at the beginning of the game.
        		MapLocation[] denLocations = new MapLocation[100];
        		startingLocation = rc.getLocation();
        		Signal[] signals = rc.emptySignalQueue();
            } catch (Exception e) {
                // Throwing an uncaught exception makes the robot die, so we need to catch exceptions.
                // Caught exceptions will result in a bytecode penalty.
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        	while (true){
        		try{
        			Signal[] signals = rc.emptySignalQueue();
                    for (Signal signal: signals){
                    	if (signal.getTeam() == myTeam){
                    		int[] messages = signal.getMessage();
                    		switch(messages[0]) {
                    		case 0:
                    			if (!maxHeightFound){
                    				maxHeightFound = true;
                    				maxHeight = messages[1];
                    			}
                    			break;
                    		case 1:
                    			if (!minHeightFound){
                    				minHeightFound = true;
                    				minHeight = messages[1];
                    			}
                    			break;
                    		case 2:
                    			if (!maxWidthFound){
                    				maxWidthFound = true;
                    				maxWidth = messages[1];
                    			}
                    			break;
                    		case 3:
                    			if (!minWidthFound){
                    				minWidthFound = true;
                    				minWidth = messages[1];
                    			}
                    			break;
                    		case 4:
                    			haveYScout = true;
                    			rc.setIndicatorString(0, "saw another yScout");
                    			break;
                    		case 5:
                    			haveXScout = true;
                    			rc.setIndicatorString(0, "saw another xScout");
                    			break;
                    		}
                    		
                    	}
                    }
                    if (!haveYScout && (!minHeightFound || !maxHeightFound)){
                    	scouty = true;
                    	rc.broadcastMessageSignal(4, 0, 80);
                    	rc.setIndicatorString(0, "became y scout");
                    }
                    else if (!haveXScout && (!minWidthFound || !maxWidthFound)){
                    	rc.broadcastMessageSignal(5, 0, 80);
                    	rc.setIndicatorString(0, "became x scout");

                    	scoutx = true;
                    }
        			MapLocation currentLocation = rc.getLocation();
        			if (scoutx){
        				if (!maxWidthFound){
            				for (int i = 0; i < 8; i ++){
            					if (!rc.onTheMap(currentLocation.add(Direction.EAST, i))){
            						maxWidth = currentLocation.x - 1;
            						rc.broadcastMessageSignal(2, maxWidth, 5000);

            						maxWidthFound = true;
            					}
            				}
            				if (!maxWidthFound && rc.canMove(Direction.EAST) && rc.isCoreReady()){
            					moveTowards(rc, Direction.EAST);
            				}
            			}
            			else if (!minWidthFound){
            				for (int i = 0; i < 8; i ++){
            					if (!rc.onTheMap(currentLocation.add(Direction.WEST, i))){
            						minWidth = currentLocation.x + 1;
            						rc.broadcastMessageSignal(3, minWidth, 5000);
            						
            						minWidthFound = true;
            					}
            				}
            				if (!minWidthFound && rc.canMove(Direction.WEST) && rc.isCoreReady()){
            					moveTowards(rc, Direction.WEST);
            				}
            			}
            			else {
            				scoutx = false;
            			}
        			}
        			else if (scouty){
        				if (!maxHeightFound){
            				for (int i = 0; i < 8; i ++){
            					if (!rc.onTheMap(currentLocation.add(Direction.NORTH, i))){
            						
            						maxHeight = currentLocation.y + 1;
            						rc.broadcastMessageSignal(0, maxHeight, 5000);
            						maxHeightFound = true;
            					}
            				}
            				if (!maxHeightFound && rc.canMove(Direction.NORTH) && rc.isCoreReady()){
            					moveTowards(rc, Direction.NORTH);
            				}
            			}
            			else if (!minHeightFound){
            				for (int i = 0; i < 8; i ++){
            					if (!rc.onTheMap(currentLocation.add(Direction.SOUTH, i))){
            						minHeight = currentLocation.y - 1;
            						rc.broadcastMessageSignal(1, minHeight, 5000);
            						minHeightFound = true;
            					}
            				}
            				if (!minHeightFound && rc.canMove(Direction.SOUTH) && rc.isCoreReady()){
            					moveTowards(rc, Direction.SOUTH);
            				}
            			}
            			else{
            				scouty = false;
            			}
        			}
        			
        			
        			
        			boolean move = false;
        			boolean stop = false;
        			boolean archonClose = false;
        			boolean zombiesNextTo = false;
        			RobotInfo[] zombiesWithinRange = rc.senseNearbyRobots(RobotType.SCOUT.sensorRadiusSquared, Team.ZOMBIE);
        			RobotInfo[] enemiesWithinRange = rc.senseNearbyRobots(RobotType.SCOUT.sensorRadiusSquared, enemyTeam);
        			RobotInfo[] alliesWithinRange = rc.senseNearbyRobots(RobotType.SCOUT.sensorRadiusSquared, myTeam);

        			for (RobotInfo zombie : zombiesWithinRange){
        				if (zombie.location.distanceSquaredTo(currentLocation) < 24){
        					move = true;
        				}
        				if (zombie.location.distanceSquaredTo(currentLocation) < 3){
        					zombiesNextTo = true;
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
        			/*startingLocation.add(Direction.NORTH, )
        			MapLocation enemyLocation = MapLocation(maxWidth - startingLocation.x - minWidth,
        					maxHeight - startingLocation.y - minHeight);
        					*/
        			
        			moveTowards(rc, moveDir);

        			if (rc.isCoreReady() && move && ! stop ){
        				moveTowards(rc, moveDir);
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
            		Clock.yield();
            	} catch (Exception e){
            		// Throwing an uncaught exception makes the robot die, so we need to catch exceptions.
            		// Caught exceptions will result in a bytecode penalty.
            		System.out.println(e.getMessage());
            		e.printStackTrace();
            	}
        		
        	}
        }
        else {
        	while (true){
        		try{
        			
        			if (rc.canMove(Direction.SOUTH_EAST) && rc.isCoreReady()){
        				rc.move(Direction.SOUTH_EAST);
        			}
        			Clock.yield();
        		} catch (Exception e){
        			// Throwing an uncaught exception makes the robot die, so we need to catch exceptions.
        			// Caught exceptions will result in a bytecode penalty.
        			System.out.println(e.getMessage());
        			e.printStackTrace();
        		}
        	}
        }
        
    }
}
