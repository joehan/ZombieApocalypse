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
	
	public Direction lastDirectionMoved = Direction.NORTH;
	
	public RobotType[] startBuildArray = {RobotType.SOLDIER, RobotType.SOLDIER, RobotType.SOLDIER, RobotType.SOLDIER,
			RobotType.SCOUT};
	public RobotType[] robotsToBuild = {RobotType.SOLDIER, RobotType.VIPER, RobotType.GUARD, RobotType.SCOUT, RobotType.TURRET};
	public Double[] buildDist = {0.7, 0.3, 0.0, 0.0, 0.0};
	
	public int buildCount = 0;
	public boolean initialIteration = true;
	
	public MapLocation leaderLocation = null;
	public int distanceToLeader = 50000;
	public Direction leaderMovingInDirection = null;
	
	public MapLocation[] denLocations = {};
	public MapLocation[] deadDens = {};
	
	public String locListToString(MapLocation[] locs){
		String ret = "";
		for (MapLocation loc : locs){
			ret += "(" + loc.x +", " + loc.y+ "), ";
		}
		return ret;
	}
	
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
	public MapLocation[] enemyInfo = new MapLocation[10000];
	
	public void addEnemyInfo(RobotInfo r){
		if (r.type == RobotType.ARCHON && !inArchonIds(r.ID)){
			archonIds[numArchons] = r.ID;
			++numArchons;
		}
		enemyInfo[r.ID] = r.location;
	}
	
	public String archonsToString(){
		String ret = "";
		for (int archonId : archonIds){
			if (archonId != 0){
				MapLocation loc = enemyInfo[archonId];
				ret += "(" + archonId + " at " + loc.x +", " + loc.y+ "), ";
			}
		}
		return ret;
	}
	
	public RobotInfo[] neutrals = {};
	public void addNeutral(RobotInfo neutral){
		if (!isNeutralKnown(neutral)){
			RobotInfo[] temp = new RobotInfo[neutrals.length+1];
			System.arraycopy(neutrals, 0, temp, 0, neutrals.length);
			temp[neutrals.length] = neutral;
			neutrals = temp;
		}
	}
	public boolean isNeutralKnown(RobotInfo aNeutral){
		for (RobotInfo neutral : neutrals){
			if (neutral != null && neutral.ID == aNeutral.ID){
				return true;
			}
		}
		return false;
	}
	public void removeNeutral(RobotInfo aNeutral){
		for (int i = 0; i < neutrals.length; i++){
			RobotInfo neutral = neutrals[i];
			if (neutral != null && neutral.ID == aNeutral.ID){
				neutrals[i] = null;
			}
		}
	}
	
	private boolean reflection = false;
	
	public enum Axis  {horizontal, vertical, highDiagonal, lowDiagonal, none};
	
	private Axis axis = Axis.none;
	
	private double middleX = 0;
	private double middleY = 0;
	
//	public MapLocation getNearestArchon(){
//		for (int i = numEnemyArchons)
//	}
	
	private int numEnemyArchons = 0;
	
	
	public boolean isArchon(int id){
		return enemyInfo[id] != null;
	}
	
	public void addArchon(MapLocation loc, int id){
		if (enemyInfo[id] == null){
			archonIds[numEnemyArchons] = id;
			numEnemyArchons ++;
		}
		enemyInfo[id] = loc;
	}
	
	/*
	 * This method should be called on the first turn of a robot's existence in order to calculate the
	 * map symmetry.  This method needs to be called before flipAcrossAxis can be called.
	 */
	public void getMapSymmetry(MapLocation[] ourTeam, MapLocation[] enemyTeam) {
		int ourX = 0;
		int ourY = 0;
		int enemyX = 0;
		int enemyY = 0;
		int numArchons = ourTeam.length;
		for (int i = 0; i < numArchons; i ++){
			ourX += ourTeam[i].x;
			ourY += ourTeam[i].y;
			enemyX += enemyTeam[i].x;
			enemyY += enemyTeam[i].y;
		}
		double ourTeamXAverage = ourX*1.0/numArchons;
		double ourTeamYAverage = ourY*1.0/numArchons;
		double enemiesXAverage = enemyX*1.0/numArchons;
		double enemiesYAverage = enemyY*1.0/numArchons;
		if (ourTeamXAverage == enemiesXAverage){
			//Assume horizontal reflection
			reflection = true;
			axis = Axis.horizontal;
			middleY = (ourTeamYAverage + enemiesYAverage)/2;
		} else if (ourTeamYAverage == enemiesYAverage){
			//Assume vertical reflection
			reflection = true;
			axis = Axis.vertical;
			middleX = (ourTeamXAverage + enemiesXAverage)/2;
		} else if (ourTeamXAverage - enemiesXAverage == ourTeamYAverage - enemiesYAverage){
			//Assume reflection: could be rotation
			reflection = true;
			axis = Axis.lowDiagonal;
			middleX = (ourTeamXAverage + enemiesXAverage)/2;
			middleY = (ourTeamYAverage + enemiesYAverage)/2;
		} else if (ourTeamXAverage - enemiesXAverage == enemiesYAverage - ourTeamYAverage){
			//Assume reflection: could be rotation
			reflection = true;
			axis = Axis.highDiagonal;
			middleX = (ourTeamXAverage + enemiesXAverage)/2;
			middleY = (ourTeamYAverage + enemiesYAverage)/2;
		} else {
			//This is definetely a rotation
			reflection = false;
			//Now we need to find out about what
			middleX = (ourTeamXAverage + enemiesXAverage)/2;
			middleY = (ourTeamYAverage + enemiesYAverage)/2;
		}
	}
	
	/*
	 * This method will get the map location that is symmetric about the axis of symmetry.  
	 * This is useful for finding den locations, parts, and neutral robots as they will always
	 * be symetric about this axes.
	 * 
	 * Note that there are some rounding errors and the MapLocation returned may be off by a coordinate
	 * in any direction
	 */
	public MapLocation flipAcrossAxis(MapLocation loc){
		double diffx;
		double diffy;
		if (reflection){
			switch(axis){
				case horizontal:
					diffy = middleY - loc.y;
					return new MapLocation(loc.x, (int) (loc.y + 2*diffy));
				case vertical:
					diffx = middleX - loc.x;
					return new MapLocation((int) (loc.x + 2*diffx), loc.y);
				case highDiagonal:
					diffx = middleX - loc.x;
					diffy = middleY - loc.y;
					return new MapLocation((int) (middleX - diffy), (int) (middleY - diffx));
				case lowDiagonal:
					diffx = middleX - loc.x;
					diffy = middleY - loc.y;
					return new MapLocation((int) (middleX + diffy) , (int) (middleY + diffx));
			default:
				break;
					
			}
		} else {
			diffx = middleX - loc.x;
			diffy = middleY - loc.y;
			return new MapLocation((int) (middleX + diffx), (int) (middleY + diffy));
		}
		return loc;
	}
	
	public boolean inArchonIds(int id){
		for (int archonId : archonIds){
			if (id == archonId){
				return true;
			}
		}
		return false;
	}
	
}