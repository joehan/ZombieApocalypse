package squadGoals;

import battlecode.common.*;

public class Squad {
	
	public static int recruitCode = 1;
	public static int setGoalLocationCode = 2;
	public static int clearGoalLocationCode = 3;
	
	public static void recruit(RobotController rc, Brain brain) throws GameActionException {
		rc.broadcastMessageSignal(recruitCode, 0, 15);
	}
	
	public static void listenForRecruits(RobotController rc, Brain brain) throws GameActionException {
		Signal[] signals = brain.thisTurnsSignals;
		//listen for signals from new bots
		for (Signal signal : signals){
			if (signal.getTeam()==rc.getTeam()){
				brain.addSquadMember(signal.getID());
			}
		}
		rc.setIndicatorString(2, brain.getSquadMembers().length + " members in squad" + rc.getID());
	}
	
	public static void lookForASquad(RobotController rc, Brain brain) throws GameActionException {
		Signal[] signals = brain.thisTurnsSignals;
		for (Signal signal: signals){
			//if its a recruiting signal from our team
			if (signal.getTeam()==rc.getTeam() && (signal.getMessage()!=null && signal.getMessage()[0]==recruitCode)){
				brain.setSquad(signal.getRobotID());
				brain.setLeaderID(signal.getRobotID());
				rc.broadcastSignal(15);
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
		rc.broadcastMessageSignal(setGoalLocationCode, Entity.convertMapToSignal(loc), 2*rc.getType().sensorRadiusSquared);
	}
	
	public static void sendClearGoalLocationCommand(RobotController rc, Brain brain) throws GameActionException {
		rc.broadcastMessageSignal(clearGoalLocationCode, 0, 2*rc.getType().sensorRadiusSquared);
	}
	
	
	public static void listenForCommands(RobotController rc, Brain brain) throws GameActionException {
		Signal[] signals = brain.thisTurnsSignals;
		for (Signal signal : signals){
			//if it's from our leader,
			int[] message = signal.getMessage();
			if (signal.getID() == brain.getLeaderID() && (message!=null)){
				if (message[0] == recruitCode){
					continue;
				} else if (message[0] == setGoalLocationCode) {
					brain.goalLocation = Entity.convertSignalToMap(signal.getMessage()[1]);
				} else if (message[0] == clearGoalLocationCode) {
					brain.goalLocation = null;
				}
			}
		}
	}
}