package scoutLure;

import java.util.Random;
import scoutLure.Entity.*;

import battlecode.common.*;

public class Scout {
	
	public static void run(RobotController rc) throws GameActionException{
		BoardLimits boardLimits = BoardLimits.getNew();
		ScoutJob scoutJobs = ScoutJob.getDefault();
		boolean scouty = false;
		boolean scoutx = false;

		while (true){
			try{
				Entity.receiveMessages(rc, boardLimits, scoutJobs); 
				if (!scoutJobs.haveYScout && (boardLimits.minHeight == (Integer) null || boardLimits.maxHeight == (Integer) null)){
                	scouty = true;
                	rc.broadcastMessageSignal(4, 0, 80);
                	rc.setIndicatorString(0, "became y scout");
                }
                else if (!scoutJobs.haveXScout && (boardLimits.minWidth == (Integer) null || boardLimits.maxWidth == (Integer) null)){
                	rc.broadcastMessageSignal(5, 0, 80);
                	rc.setIndicatorString(0, "became x scout");

                	scoutx = true;
                }
				
				
				Random rand = new Random(rc.getID()+rc.getRoundNum());
				RobotType typeToBuild = RobotType.SCOUT;

				if (rc.isCoreReady()) {
					repairUnits(rc);
					tryBuildUnitInEmptySpace(rc, typeToBuild, Direction.NORTH);
					Clock.yield();
				}
			}catch (Exception e){
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}
}