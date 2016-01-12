package squadGoals;

import java.util.HashSet;

import battlecode.common.*;

public class Squad {
	//Intrasquad codes
	public final static int recruitCode = 1;
	public final static int setGoalLocationCode = 2;
	public final static int clearGoalLocationCode = 3;
	public final static int denCode = 4;
	
	//Intersquad codes
	public final static int intersquadCodeMinimum = 100;
	public final static int helpMeCode = 101;
	public final static int shareDenLocationCode = 102;
	public final static int deadDenCode = 105;
	public final static int foundEnemyCode = 106;
	public final static int enemyTurretCode = 107;
	
	public static void processMessages(RobotController rc, Brain brain){
		Signal[] messages = rc.emptySignalQueue();
		brain.resetMessages();
		for (Signal signal : messages){
			int[] message = signal.getMessage();
			if (signal.getMessage() != null && brain.getSquadNum() == signal.getID()){
				switch(message[0]){
					case recruitCode:
						brain.recruitMessages.add(signal);
						break;
					case setGoalLocationCode:
						brain.setGoalLocation.add(signal);
						break;
					case clearGoalLocationCode:
						brain.clearGoalLocation.add(signal);
						break;
					case denCode:
						brain.den.add(signal);
						break;
				}
			}
			else if (signal.getMessage() != null){
				switch(message[0]){
					case recruitCode:
						brain.recruitMessages.add(signal);
						break;
					case helpMeCode:
						brain.helpMe.add(signal);
						break;
					case shareDenLocationCode:
						brain.shareDenLocation.add(signal);
						break;
					case deadDenCode:
						brain.deadDen.add(signal);
						break;
					case foundEnemyCode:
						brain.foundEnemy.add(signal);
						break;
					case enemyTurretCode:
						brain.enemyTurret.add(signal);
						break;
				}
			} else {
				brain.regularMessage.add(signal);
			}
		}
	}
	
	
	public static void recruit(RobotController rc, Brain brain) throws GameActionException {
		rc.broadcastMessageSignal(recruitCode, brain.getSquadMembers().length, 72);
	}
	
	/*
	 * NOte that this method sets the current goal to be the first message processed out of 
	 * all the squad messages
	 */
	public static boolean processSquadMessages(RobotController rc, Brain brain){
		Signal[] signals = brain.thisTurnsSignals;
		for (Signal signal : signals){
			if (brain.memberInSquad(signal.getID()) && (brain.goalLocation == null)){
				Direction dirToFriend = rc.getLocation().directionTo(signal.getLocation());
				brain.goalLocation = signal.getLocation().add(dirToFriend, 4);
				return true;
			}
		} 
		return false;
	}
	
	public static void listenForRecruits(RobotController rc, Brain brain) throws GameActionException {
		HashSet<Integer> potentialSquadmates = new HashSet<Integer>();
//		Signal[] signals = brain.thisTurnsSignals;
		//listen for signals from new bots
		//Need to for a squad join
		for (Signal signal : brain.regularMessage){
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
//		Signal[] signals = brain.thisTurnsSignals;

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
			for (Signal signal: brain.recruitMessages){
				//if its a recruiting signal from our team
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
	/*
	 * findLeaderLocation updates the leadersLastKnownLocation to his current position, if he is in sight range
	 */
	public static void findLeaderLocation(RobotController rc, Brain brain) throws GameActionException {
		for (Signal friend : brain.recruitMessages) {
			if (friend.getID() == brain.getLeaderID()){
				brain.leadersLastKnownLocation = friend.getLocation();
			}
		}
	}
	
	public static int messageRange(RobotController rc){
		if (rc.getRoundNum() % 80 == 0) {
			return 31*rc.getType().sensorRadiusSquared;
		} else {
			return 2*rc.getType().sensorRadiusSquared;
		}
	}
	public static void sendMoveCommand(RobotController rc, Brain brain, MapLocation loc) throws GameActionException{
		rc.broadcastMessageSignal(setGoalLocationCode, Entity.convertMapToSignal(loc), messageRange(rc));
	}
	
	public static void sendClearGoalLocationCommand(RobotController rc, Brain brain) throws GameActionException {
		rc.broadcastMessageSignal(clearGoalLocationCode, 0, messageRange(rc));
	}
	
	public static void sendAttackDenCommand(RobotController rc, Brain brain, MapLocation den) throws GameActionException{
		rc.broadcastMessageSignal(denCode, Entity.convertMapToSignal(den), messageRange(rc));
	}
	
	public static void sendDeadDenCommand(RobotController rc, Brain brain, MapLocation den) throws GameActionException {
		rc.broadcastMessageSignal(deadDenCode, Entity.convertMapToSignal(den), 2*rc.getLocation().distanceSquaredTo(brain.getStartingLocation()));
	}
	
	public static void sendEnemyFoundCommand(RobotController rc, Brain brain, MapLocation enemy) throws GameActionException{
		rc.broadcastMessageSignal(foundEnemyCode, Entity.convertMapToSignal(enemy), 2*rc.getLocation().distanceSquaredTo(brain.getStartingLocation()));
	}
	
	public static void sendEnemyTurretCommand(RobotController rc, Brain brain, MapLocation enemy) throws GameActionException{
		rc.broadcastMessageSignal(enemyTurretCode, Entity.convertMapToSignal(enemy), 
				2*rc.getLocation().distanceSquaredTo(brain.getStartingLocation()));
	}
	
	/*
	 * SendHelpMessage asks any nearby archons for help
	 */
	public static void sendHelpMessage(RobotController rc, Brain brain, int distance) throws GameActionException {
		rc.broadcastMessageSignal(helpMeCode, 0, distance);
	}
	
	/*
	 * ShareDenLocation shares the location of a den with any nearby archons
	 */
	public static void shareDenLocation(RobotController rc, Brain brain, MapLocation den, int distance
			) throws GameActionException {
		rc.broadcastMessageSignal(shareDenLocationCode, Entity.convertMapToSignal(den) , distance);
	}
	
	public static void listenForCommands(RobotController rc, Brain brain) throws GameActionException {
//		Signal[] signals = brain.thisTurnsSignals;
		for (Signal signal : brain.setGoalLocation){
			//if it's from our leader,
			int[] message = signal.getMessage();
			brain.goalLocation = Entity.convertSignalToMap(signal.getMessage()[1]);
		}
		for (Signal signal : brain.clearGoalLocation){
			int[] message = signal.getMessage();				
			brain.goalLocation = null;
		}
		for (Signal signal : brain.den){
			int[] message = signal.getMessage();
			MapLocation den =Entity.convertSignalToMap(message[1]);
			if (!(brain.isDenDead(den))){
				brain.addDenLocation(den);
				brain.goalLocation = den;
			} else {
				rc.setIndicatorString(0, "Ignoring den message at " + den.x + ", " + den.y);
			}
		}
		for (Signal signal : brain.deadDen){
			int[] message = signal.getMessage();
			brain.removeDenLocation(Entity.convertSignalToMap(message[1]));
		}
	}
	
	public static void listenForIntersquadCommunication(RobotController rc, Brain brain) throws GameActionException {
//		Signal[] signals = brain.thisTurnsSignals;
		for (Signal signal : brain.helpMe){
			MapLocation friend = signal.getLocation();
			brain.goalLocation = friend;
		}
		for (Signal signal : brain.shareDenLocation){
			MapLocation den = Entity.convertSignalToMap(signal.getMessage()[1]);
			brain.addDenLocation(den);
		}
		for (Signal signal : brain.deadDen){
			brain.removeDenLocation(Entity.convertSignalToMap(signal.getMessage()[1]));
		}
		for (Signal signal : brain.foundEnemy){
			brain.addEnemyLocation(Entity.convertSignalToMap(signal.getMessage()[1]));
		}
		for (Signal signal : brain.enemyTurret){
			brain.setEnemyTurret();
			break;
		}
		
		/*for (Signal signal: signals){
			int[] message = signal.getMessage();
			//if
			if (signal.getTeam()==rc.getTeam() &&  !(message == null) && message[0]>intersquadCodeMinimum){
				if (message[0] == helpMeCode){
					MapLocation friend = signal.getLocation();
					brain.goalLocation = friend;
				} else if (message[0] == shareDenLocationCode){
					MapLocation den = Entity.convertSignalToMap(message[1]);
					brain.addDenLocation(den);
				}
			}
		}*/
	}
}