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
	
	public RobotType[] startBuildArray = {};
	public RobotType[] mainBuildArray = {RobotType.SOLDIER, RobotType.SOLDIER, RobotType.SOLDIER};
	public int buildCount = 0;
	public boolean initialIteration = true;
	
	public MapLocation leaderLocation = null;
	public int distanceToLeader = 50000;
	public Direction leaderMovingInDirection = null;
	
}