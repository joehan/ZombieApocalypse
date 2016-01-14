package betterScouting;

import battlecode.common.MapLocation;
import battlecode.common.RobotType;

public class Brain {

	
	private RobotType[] startBuildArray = {RobotType.SCOUT, RobotType.GUARD};
	private RobotType[] iterateBuildArray = {RobotType.GUARD, RobotType.GUARD, RobotType.GUARD};
	private int buildCount = 0;
	private Boolean initialIteration = true;
	
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
	
	private boolean reflection = false;
	
	public enum Axis  {horizontal, vertical, highDiagonal, lowDiagonal, none};
	
	private Axis axis = Axis.none;
	
	private double middleX = 0;
	private double middleY = 0;
	
	
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
	
	
}
