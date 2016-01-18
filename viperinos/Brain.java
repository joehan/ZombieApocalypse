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
	public RobotType[] mainBuildArray = {RobotType.VIPER, RobotType.SOLDIER, RobotType.SOLDIER, RobotType.SOLDIER, RobotType.SOLDIER};
	public int buildCount = 0;
	public boolean initialIteration = true;
	
	public MapLocation leaderLocation = null;
	public int distanceToLeader = 50000;
	public Direction leaderMovingInDirection = null;
	
	public MapLocation[] denLocations = {};
	public MapLocation[] deadDens = {};
	
	public void addDenLocation(MapLocation den){
		MapLocation[] temp = new MapLocation[denLocations.length + 1];
		System.arraycopy(denLocations, 0, temp, 0, denLocations.length);
		temp[denLocations.length] = den;
		denLocations = temp;
	}
	
	public void addDeadDenLocation(MapLocation den){
		MapLocation[] temp = new MapLocation[deadDens.length + 1];
		System.arraycopy(deadDens, 0, temp, 0, deadDens.length);
		temp[deadDens.length+1] = den;
		deadDens = temp;
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
	
	
}