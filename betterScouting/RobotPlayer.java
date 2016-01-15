package betterScouting;


import battlecode.common.*;

/*
 * Entity contains functions that will be used by multiple types of units
 */
public class RobotPlayer {
	
	public static void run(RobotController rc){
		
		Brain brain = new Brain();
		
		RobotType type = rc.getType();
	    try {
			if (type == RobotType.ARCHON) {
				Archon archon = new Archon();
				archon.run(rc, brain);
			} else if (type == RobotType.GUARD){
				Guard guard = new Guard();
				Guard.run(rc, brain);
			} else if (type == RobotType.SOLDIER){
				Soldier soldier = new Soldier();
				soldier.run(rc, brain);
			} else if (type == RobotType.SCOUT){
				Scout scout = new Scout();
				scout.run(rc, brain);
			} else if (type == RobotType.TURRET || type == RobotType.TTM){
				Turret turret = new Turret();
				turret.run(rc, brain);
			} else if (type == RobotType.VIPER){
				Viper viper = new Viper();
				viper.run(rc, brain);
			}
			Clock.yield();
		} catch(Exception e){
			System.out.println(e.getMessage());
	        e.printStackTrace();
		}
	}
	
}