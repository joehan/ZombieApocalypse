package squadGoalsWithNeuts;

import java.util.HashSet;

import battlecode.common.*;

public class Squad {
	//Intrasquad codes
	public static int recruitCode = 1;
	public static int setGoalLocationCode = 2;
	public static int clearGoalLocationCode = 3;
	
	//Intersquad codes
	public static int intersquadCodeMinimum = 100;
	public static int helpMeCode = 101;
	public static int shareDenLocationCode = 102;
	public static int shareNeutLocationCode = 103;
	
	public static void recruit(RobotController rc, Brain brain) throws GameActionException {
		rc.broadcastMessageSignal(recruitCode, brain.getSquadMembers().length, 72);
	}
	
	/*
	 * NOte that this method sets the current goal to be the last message processed out of 
	 * all the squad messages
	 */
	public static void processSquadMessages(RobotController rc, Brain brain){
		Signal[] signals = brain.thisTurnsSignals;
		for (Signal signal : signals){
			if (brain.memberInSquad(signal.getID()) && (brain.goalLocation == null)){
				Direction dirToFriend = rc.getLocation().directionTo(signal.getLocation());
				brain.goalLocation = signal.getLocation().add(dirToFriend, 4);
			}
		}
	}
	
	public static void listenForRecruits(RobotController rc, Brain brain) throws GameActionException {
		HashSet<Integer> potentialSquadmates = new HashSet<Integer>();
		Signal[] signals = brain.thisTurnsSignals;
		//listen for signals from new bots
		//Need to for a squad join
		for (Signal signal : signals){
			if (signal.getTeam()==rc.getTeam() && potentialSquadmates.contains(signal.getRobotID())
					&& (signal.getMessage() == null)){
				brain.addSquadMember(signal.getID());
			} else if (signal.getTeam() == rc.getTeam() && !potentialSquadmates.contains(signal.getRobotID())
					&& (signal.getMessage() == null)){
				potentialSquadmates.add(signal.getRobotID());
			}
		}
		rc.setIndicatorString(2, brain.getSquadMembers().length + " members in squad" + rc.getID());
	}
	
	public static void lookForASquad(RobotController rc, Brain brain, RobotInfo[] allies) throws GameActionException {
		Signal[] signals = brain.thisTurnsSignals;

		int closestArchonDistance = 100;
		for (RobotInfo ally: allies){
			if (ally.type == RobotType.ARCHON && 
					ally.location.distanceSquaredTo(rc.getLocation()) < closestArchonDistance){
				closestArchonDistance = ally.location.distanceSquaredTo(rc.getLocation());
			}
			else if (ally.type == RobotType.ARCHON && 
					ally.location.distanceSquaredTo(rc.getLocation()) == closestArchonDistance){
				//Two archons at the same distance
				return;
			}
		}
		if (closestArchonDistance < 100){
			for (Signal signal: signals){
				//if its a recruiting signal from our team
				if (signal.getTeam()==rc.getTeam() && (!(signal.getMessage() == null) && signal.getMessage()[0]==recruitCode)){
					brain.setSquad(signal.getRobotID());
					brain.setLeaderID(signal.getRobotID());
					brain.leadersLastKnownLocation = signal.getLocation();
					rc.broadcastSignal(closestArchonDistance + 1);
					rc.broadcastSignal(closestArchonDistance + 1);
					rc.setIndicatorString(2, "On squad" + brain.getSquadNum());
					break;
				}
			}
		}
	}
	/*
	 * findLeaderLocation updates the leadersLastKnownLocation to his current position, if he is in sight range
	 */
	public static void findLeaderLocation(RobotController rc, Brain brain) throws GameActionException {
		for (Signal friend : brain.thisTurnsSignals) {
			if (friend.getID() == brain.getLeaderID()){
				brain.leadersLastKnownLocation = friend.getLocation();
			}
		}
	}
	
	public static void sendMoveCommand(RobotController rc, Brain brain, MapLocation loc) throws GameActionException{
		rc.broadcastMessageSignal(setGoalLocationCode, Entity.convertMapToSignal(loc), 2*rc.getType().sensorRadiusSquared);
	}
	
	public static void sendClearGoalLocationCommand(RobotController rc, Brain brain) throws GameActionException {
		rc.broadcastMessageSignal(clearGoalLocationCode, 0, 2*rc.getType().sensorRadiusSquared);
	}
	
	/*
	 * SendHelpMessage asks any nearby archons for help
	 */
	public static void sendHelpMessage(RobotController rc, Brain brain) throws GameActionException {
		rc.broadcastMessageSignal(helpMeCode, 0, 2*rc.getType().sensorRadiusSquared);
	}
	
	/*
	 * ShareDenLocation shares the location of a den with any nearby archons
	 */
	public static void shareDenLocation(RobotController rc, Brain brain, MapLocation den, int distance
			) throws GameActionException {
		rc.broadcastMessageSignal(shareDenLocationCode, Entity.convertMapToSignal(den) , distance);
	}
	
	/*
	 * ShareNeutLocation shares locations of neutral bot with nearby archons
	 */
	public static void shareNeutLocation(RobotController rc, Brain brain, MapLocation loc, int distance) throws GameActionException{
		rc.broadcastMessageSignal(shareNeutLocationCode, Entity.convertMapToSignal(loc), distance);
	}
	
	public static void listenForCommands(RobotController rc, Brain brain) throws GameActionException {
		Signal[] signals = brain.thisTurnsSignals;
		for (Signal signal : signals){
			//if it's from our leader,
			int[] message = signal.getMessage();
			if (signal.getID() == brain.getLeaderID() && (message!=null)){
				brain.leadersLastKnownLocation = signal.getLocation();
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
	
	public static void listenForIntersquadCommunication(RobotController rc, Brain brain) throws GameActionException {
		Signal[] signals = brain.thisTurnsSignals;
		for (Signal signal: signals){
			int[] message = signal.getMessage();
			//if
			if (signal.getTeam()==rc.getTeam() &&  !(message == null) && message[0]>intersquadCodeMinimum){
				if (message[0] == helpMeCode){
					MapLocation friend = signal.getLocation();
					brain.goalLocation = friend;
				} else if (message[0] == shareDenLocationCode){
					MapLocation den = Entity.convertSignalToMap(message[1]);
					brain.addDenLocation(den);
				} else if (message[0] == shareNeutLocationCode){
					MapLocation neut = Entity.convertSignalToMap(message[1]);
					brain.addNeutBot(neut);
				}
			}
		}
	}
}