package viperinos;


import java.util.Random;

import battlecode.common.*;


/*
 * Brain is used by robots to track information about the world around them.
 * For example, it keeps track of a list of the dens that robot has seen, the dimmensions
 * of the map, etc.
 * It is separated out from the individual classes becasue many of these function are universal
 */
public class Brain {
	
	public Random rand;
	public Signal[] thisTurnsSignals;
	
	public Direction lastDirectionMoved;
	
	public RobotType[] startBuildArray = {RobotType.SOLDIER, RobotType.SOLDIER};
	public RobotType[] robotsToBuild = {RobotType.SOLDIER, RobotType.VIPER, RobotType.GUARD, RobotType.SCOUT, RobotType.TURRET};
	public Double[] buildDist = {0.75, 0.25, 0.0, 0.0, 0.0};
	
	public int buildCount = 0;
	public boolean initialIteration = true;
	
	public MapLocation leaderLocation = null;
	public int distanceToLeader = 50000;
	public Direction leaderMovingInDirection = null;
	
	public MapLocation[] denLocations = {};
	public MapLocation[] deadDens = {};
	
	public void addDenLocation(MapLocation den){
		if (!isDenKnown(den)){
			MapLocation[] temp = new MapLocation[denLocations.length + 1];
			System.arraycopy(denLocations, 0, temp, 0, denLocations.length);
			temp[denLocations.length] = den;
			denLocations = temp;
		}
	}
	
	public void addDeadDenLocation(MapLocation den){
		if (!isDenDead(den)){
			MapLocation[] temp = new MapLocation[deadDens.length + 1];
			System.arraycopy(deadDens, 0, temp, 0, deadDens.length);
			temp[deadDens.length] = den;
			deadDens = temp;
		}
	}
	
	public boolean isDenKnown(MapLocation den){
		boolean isKnown = false;
		for (MapLocation knownDen : denLocations){
			if (den.equals(knownDen)){
				isKnown = true;
				break;
			}
		}
		return isKnown;
	}
	
	public boolean isDenDead(MapLocation den){
		boolean isDead = false;
		for (MapLocation deadDen : deadDens){
			if (den.equals(deadDen)){
				isDead = true;
				break;
			}
		}
		return isDead;
	}
	
	public int[] archonIds = new int[8];
	public int numArchons = 0;
	public RobotInfo[] enemyInfo = new RobotInfo[10000];
	public void addEnemyInfo(RobotInfo r){
		if (r.type == RobotType.ARCHON && enemyInfo[r.ID]==null){
			archonIds[numArchons] = r.ID;
			++numArchons;
		}
		enemyInfo[r.ID] = r;
	}
	
}