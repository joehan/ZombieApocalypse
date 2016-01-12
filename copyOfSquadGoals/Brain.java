package copyOfSquadGoals;

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
	
	
	//Remember where we've seen parts
	private HashSet<MapLocation> partLocations = new HashSet<MapLocation>();
	
	public MapLocation[] getPartLocations(){
		return  partLocations.toArray(new MapLocation[partLocations.size()]);
	}
	
	public void addPartLocation(MapLocation loc) {
		partLocations.add(loc);
	}
	
	public void removePartLocation(MapLocation loc){
		partLocations.remove(loc);
	}
	//Rembering den locations!
	private HashSet<MapLocation> denLocations = new HashSet<MapLocation>();
	private HashSet<MapLocation> deadDenLocations = new HashSet<MapLocation>();
	
	public MapLocation[] getDenLocations(){
		return denLocations.toArray(new MapLocation[denLocations.size()]);
	}
	
	public MapLocation[] getDeadDenLocations(){
		return deadDenLocations.toArray(new MapLocation[deadDenLocations.size()]);
	}
	
	public void addDenLocation(MapLocation loc) {
		denLocations.add(loc);
	}
	public void removeDenLocation(MapLocation loc){
		denLocations.remove(loc);
		deadDenLocations.add(loc);
	}
	
	public Boolean isDenDead(MapLocation den){
		return (deadDenLocations.contains(den));
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
	
	private RobotType[] startBuildArray = {RobotType.SCOUT, RobotType.SOLDIER};
	private RobotType[] iterateBuildArray = {RobotType.SOLDIER, RobotType.SOLDIER, RobotType.SOLDIER};
	private int buildCount = 0;
	private Boolean initialIteration = true;
	
//	public RobotType buildNextUnit(RobotController rc){
//		if(initialIteration){
//			RobotType returnRobot = startBuildArray[buildCount];
//			buildCount++;
//			rc.setIndicatorString(1, "" + startBuildArray.length);
//			if(buildCount >= startBuildArray.length){
//				buildCount = 0;
//				initialIteration = false;
//			}
//			return returnRobot;
//		}else{
//			RobotType returnRobot = iterateBuildArray[buildCount];
//			buildCount++;
//			if(buildCount >= iterateBuildArray.length){
//				buildCount = 0;
//			}
//			return returnRobot;
//		}
//	}
	
	public int getBuildCount() {
		return buildCount;
	}
	public void setBuildCount(int buildCount) {
		this.buildCount = buildCount;
	}
	public Boolean getInitialIteration() {
		return initialIteration;
	}
	public void setInitialIteration(Boolean initialIteration) {
		this.initialIteration = initialIteration;
	}
	public RobotType[] getStartBuildArray() {
		return startBuildArray;
	}
	public RobotType[] getIterateBuildArray() {
		return iterateBuildArray;
	}
	public int getStartBuildLength() {
		return startBuildArray.length;
	}
	public int getIterateBuildLength() {
		return iterateBuildArray.length;
	}
	
	private MapLocation startingLocation;
	
	public void setStartingLocation(MapLocation loc){
		this.startingLocation = loc;
	}
	
	public MapLocation getStartingLocation(){
		return this.startingLocation;
	}

	private HashSet<MapLocation> archonStarts = new HashSet<MapLocation>();
	
	public MapLocation[] getArchonStarts() {
		return archonStarts.toArray(new MapLocation[denLocations.size()]);
	}
	
	public void addArchonStart(MapLocation loc){
		archonStarts.add(loc);
	}
	
	public MapLocation goalLocation = null;
	public MapLocation leadersLastKnownLocation = null;
	
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
	
	public void removeSquadMember(int memberID){
		squadMembers.remove(memberID);
	}
	
	public boolean memberInSquad(int memberID){
		return squadMembers.contains(memberID);
	}
	
	public Integer[] getSquadMembers(){
		return squadMembers.toArray(new Integer[squadMembers.size()]);
	}
	
	public Signal[] thisTurnsSignals;
	public Random rand;
}