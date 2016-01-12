package squadGoalsWithNeuts;

import java.util.Random;

import battlecode.common.*;

public class Viper {
	
	public void run(RobotController rc, Brain brain){
		
	}
	
	/*
	 * viperAttack looks through the robots from the opposing tema that are in range for uninfected robots, and
	 * attacks the one that is furthest away
	 */
	public boolean viperAttack(RobotController rc, Brain brain){
		boolean attacked = false;
		//Look for nearby enemies
		RobotInfo[] enemiesInRange = rc.senseNearbyRobots(rc.getType().attackRadiusSquared, rc.getTeam().opponent());
		//if there are any enemies
		if (enemiesInRange.length>0){
			int greatestDist = 0;
			RobotInfo enemyToAttack;
			for (RobotInfo enemy : enemiesInRange) {
				
			}
		}
		return attacked;
	}
}