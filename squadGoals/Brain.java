package squadGoals;

import java.util.HashSet;
import java.util.ArrayList;
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
	
	//Rembering den locations!
	private HashSet<MapLocation> denLocations = new HashSet<MapLocation>();
	
	public MapLocation[] getDenLocations(){
		return denLocations.toArray(new MapLocation[denLocations.size()]);
	}
	public void addDenLocation(MapLocation loc) {
		denLocations.add(loc);
	}
	public void removeDenLocation(MapLocation loc){
		denLocations.remove(loc);
	}
	public Boolean isDenNew(MapLocation den) {
		return !(denLocations.contains(den));
	}
	
	//Remembering what you've built
	public HashMap<RobotType, Integer> buildHistory; 
	
	public void initBuildHistory(){
		buildHistory = new HashMap<RobotType, Integer>();
		buildHistory.put(RobotType.GUARD, 0);
		buildHistory.put(RobotType.SCOUT, 0);
		buildHistory.put(RobotType.SOLDIER, 0);
		buildHistory.put(RobotType.TURRET, 0);
		buildHistory.put(RobotType.VIPER, 0);
	}
	
	public void iterateUnitInBuildHistory(RobotType type){
		buildHistory.put(type, buildHistory.get(type) +1);
	}
	
	private HashSet<MapLocation> archonStarts = new HashSet<MapLocation>();
	
	public MapLocation[] getArchonStarts() {
		return archonStarts.toArray(new MapLocation[denLocations.size()]);
	}
	
	public void addArchonStart(MapLocation loc){
		archonStarts.add(loc);
	}
	
	public MapLocation goalLocation = null;
	
	private int squadNumber = -1;
	
	public void setSquad(int squadNum){
		squadNumber=squadNum;
	}
	
	public Integer getSquadNum(){
		return squadNumber;
	}
	
	private int squadLeaderID = -1;
	
	public void setLeaderID(int id){
		squadLeaderID = id;
	}
	
	public int getLeaderID(){
		return squadLeaderID;
	}
	
	private HashSet<Integer> squadMembers = new HashSet<Integer>();
	
	public void addSquadMember(int memberID){
		squadMembers.add(new Integer(memberID));
	}
	
	public Integer[] getSquadMembers(){
		return squadMembers.toArray(new Integer[squadMembers.size()]);
	}
	
	public Signal[] thisTurnsSignals;
	public Random rand;
}