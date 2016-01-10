package squadGoals;

import battlecode.common.*;

public class Squad {
	
	public static void recruit(RobotController rc, Brain brain) throws GameActionException {
		rc.broadcastMessageSignal(-16001, -16001, 36);
	}
	
	public static void listenForRecruits(RobotController rc, Brain brain) throws GameActionException {
		Signal[] signals = rc.emptySignalQueue();
		//listen for signals from new bots
		for (Signal signal : signals){
			if (signal.getTeam()==rc.getTeam()){
				brain.addSquadMember(signal.getID());
			}
		}
		rc.setIndicatorString(2, brain.getSquadMembers().length + " members in squad" + rc.getID());
	}
	
	public static void lookForASquad(RobotController rc, Brain brain) throws GameActionException {
		Signal[] signals = rc.emptySignalQueue();
		for (Signal signal: signals){
			//if its a recruiting signal from our team
			if (signal.getTeam()==rc.getTeam() && (signal.getMessage()!=null && signal.getMessage()[0]==-16001)){
				brain.setSquad(signal.getRobotID());
				brain.setLeaderID(signal.getRobotID());
				rc.broadcastSignal(36);
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
		rc.broadcastMessageSignal(loc.x, loc.y, 2*rc.getType().sensorRadiusSquared);
	}
	
	public static void listenForCommands(RobotController rc, Brain brain) throws GameActionException {
		Signal[] signals = rc.emptySignalQueue();
		for (Signal signal : signals){
			//if it's from our leader, and its not a recruiting signal
			if (signal.getID() == brain.getLeaderID() && (signal.getMessage()!=null && signal.getMessage()[0]!=-16001)){
				brain.goalLocation = new MapLocation(signal.getMessage()[0], signal.getMessage()[1]);
			}
		}
	}
}