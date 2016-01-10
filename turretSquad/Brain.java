package turretSquad;

import java.util.HashSet;
import java.util.HashMap;
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
	
	
}