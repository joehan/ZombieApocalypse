package rovingGangs;

import battlecode.common.*;

/*
 * Entity contains functions that will be used by multiple types of units
 */
public class RobotPlayer {
	
	public static void run(RobotController rc){
		
		while (true) {
			try {
				RobotType type = rc.getType();
			    
				if (type == RobotType.ARCHON) {
					Archon.run(rc);
				} else if (type == RobotType.GUARD){
					Guard.run(rc);
				} else if (type == RobotType.SOLDIER){
					Soldier.run(rc);
				} else if (type == RobotType.SCOUT){
					Scout.run(rc);
				} else if (type == RobotType.TURRET){
					Turret.run(rc);
				} else if (type == RobotType.VIPER){
					Viper.run(rc);
				}
				Clock.yield();
			} catch(Exception e){
				System.out.println(e.getMessage());
	            e.printStackTrace();
			}
		}
	}
	
}