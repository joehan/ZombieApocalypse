package squadGoals;

import battlecode.common.*;

public class Squad {
	
	public static void recruit(RobotController rc, Brain brain) throws GameActionException {
		rc.broadcastMessageSignal(-1, -1, 4);
	}
	
	public static void listenForRecruits(RobotController rc, Brain brain) throws GameActionException {
		Signal[] signals = rc.emptySignalQueue();
		//listen for signals from new bots
		for (Signal signal : signals){
			if (signal.getTeam()==rc.getTeam()){
				brain.addSquadMember(signal.getID());
			}
		}
		
	}
	
	public static void lookForASquad(RobotController rc, Brain brain) throws GameActionException {
		Signal[] signals = rc.emptySignalQueue();
		for (Signal signal: signals){
			//if its a recruiting signal
			if (signal.getTeam()==rc.getTeam() && (signal.getMessage()!=null && signal.getMessage()[0]==-1)){
				brain.setSquad(signal.getRobotID());
				brain.setLeaderID(signal.getRobotID());
				rc.broadcastSignal(9);
				rc.setIndicatorString(2, "On squad" + brain.getSquadNum());
				break;
			}
		}
	}
	/*
	 * findLeaderLocation returns the location of the squad leader if it is in range. Otherwise, it returns rc.getLocation();
	 */
	public static MapLocation findLeaderLocation(RobotController rc, Brain brain) throws GameActionException {
		RobotInfo[] friends = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam());
		for (RobotInfo friend : friends) {
			if (friend.ID == brain.getLeaderID()){
				return friend.location;
			}
		}
		return rc.getLocation();
	}
	
	public static void sendMoveCommand(RobotController rc, Brain brain, MapLocation loc) throws GameActionException{
		
	}
}