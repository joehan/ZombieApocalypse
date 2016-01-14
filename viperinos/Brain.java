package viperinos;

import java.util.HashSet;
import java.util.HashMap;
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
	
	public RobotType[] startBuildArray = {RobotType.SCOUT};
	public RobotType[] mainBuildArray = {RobotType.SOLDIER};
	public int buildCount = 0;
	public boolean initialIteration = true;
}