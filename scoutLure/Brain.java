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
	public boolean scoutx;
	public boolean scouty;
	public boolean enemyBaseFound;
	public MapLocation enemyBase;
	public HashSet<MapLocation> denGuarded;
	public final Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
             Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	//an array with one slot for each direction. the number stored in each slot of the array
	//is the last time we received an allied signal saying it was going to chase a zombie in
	//the corresponding direction.
	public int[] lastLuredDirection;
	public HashSet<MapLocation> archonLocations = new HashSet<MapLocation>();
	public HashSet<Integer> taggedZombies = new HashSet<Integer>();
	
	
//	this.maxHeight = this.minHeight = this.maxWidth = this.minWidth = (Integer) null;
	public Brain(MapLocation startingLocation){
		denLocations = new HashSet<MapLocation>();
		this.maxHeight = minHeight = maxWidth = minWidth = (Integer) null;
		haveXScout = haveYScout = false;
		startLocation = startingLocation;
		scoutx = scouty = false;
		enemyBase = null;
		denGuarded = new HashSet<MapLocation>();
		lastLuredDirection = new int[8];
	}
	
	public MapLocation averageArchonLocation(){
		int averageX = 0;
		int averageY = 0;
		int numArchons = 0;
		for (MapLocation archon : archonLocations){
			averageX += archon.x;
			averageY += archon.y;
			numArchons ++;
		}
		MapLocation clumpLocation = new MapLocation(averageX/numArchons, averageY/numArchons);
		return clumpLocation;
	}
	
	/*
	 * This gets the location on the map flipped about the 45 degree angle line from the bottom left
	 * to the top right corner.
	 */
	public MapLocation flipLocation(MapLocation loc){
		if (!(this.maxHeight == null) && !(this.minHeight == null) && !(this.maxWidth == null) && !(this.minWidth == null)){
			return new MapLocation(minWidth + maxWidth - loc.x, minHeight + maxHeight - loc.y);
		}
		else {
			return loc;
		}
	}
	

	public void tryFindEnemyBase(){
		MapLocation newLoc = flipLocation(startLocation);
		if (newLoc != startLocation){
			enemyBase = newLoc;
			enemyBaseFound = true;
		}
	}

	
}