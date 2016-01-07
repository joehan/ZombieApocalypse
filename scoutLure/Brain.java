package scoutLure;

import java.util.ArrayList;
import java.util.HashSet;

import battlecode.common.*;

/*
 * Brain is used by robots to track information about the world around them.
 * For example, it keeps track of a list of the dens that robot has seen, the dimmensions
 * of the map, etc.
 * It is separated out from the individual classes becasue many of these function are universal
 */
public class Brain {
	
	public HashSet<MapLocation> denLocations; 
	public Integer maxHeight, minHeight, maxWidth, minWidth;
	public boolean haveXScout, haveYScout;
	public MapLocation startLocation;
	
//	this.maxHeight = this.minHeight = this.maxWidth = this.minWidth = (Integer) null;
	public Brain(MapLocation startingLocation){
		denLocations = new HashSet<MapLocation>();
		this.maxHeight = minHeight = maxWidth = minWidth = (Integer) null;
		haveXScout = haveYScout = false;
		startLocation = startingLocation;
		
	}

	
}