package viperinos;

import battlecode.common.*;

public class Squad {
	
	public static int movingDirectionCode = 1;
	
	public static int shareDenLocationCode = 101;
	public static int shareDeadDenCode = 102;
	public static int shareArchonInfoCode = 103;
	
	public static void sendDirectionToMove(RobotController rc, Brain brain, Direction dir) throws GameActionException {
		rc.broadcastMessageSignal(movingDirectionCode, Message.convertDirectionToSignal(dir), 2 * rc.getType().sensorRadiusSquared);
	}
	
	public static void listenForCommands(RobotController rc, Brain brain) {
		brain.distanceToLeader = 50000;
		for (Signal signal : brain.thisTurnsSignals){
			if (signal.getTeam()==rc.getTeam() && signal.getMessage()!=null){
				int[] message = signal.getMessage();
				if (message[0] == movingDirectionCode){
					int distanceToMessager = rc.getLocation().distanceSquaredTo(signal.getLocation());
					if (distanceToMessager < brain.distanceToLeader){
						brain.leaderLocation = signal.getLocation();
						brain.distanceToLeader = distanceToMessager;
						brain.leaderMovingInDirection = Message.convertSignalToDirection(message[1]);
					}
				}
				
			}
		}
	}
	
	public static void shareDenLocation(RobotController rc, MapLocation den, int distance) throws GameActionException{
		rc.broadcastMessageSignal(shareDenLocationCode, Message.convertMapToSignal(den), distance);
	}
	
	public static void shareDeadDen(RobotController rc, MapLocation den, int distance) throws GameActionException{
		rc.broadcastMessageSignal(shareDeadDenCode, Message.convertMapToSignal(den), distance);
	}
	
	public static void shareArchonInfo(RobotController rc, RobotInfo archon, int distance) throws GameActionException{
		rc.broadcastMessageSignal(Message.convertIDToSignal(archon.ID, shareArchonInfoCode), Message.convertMapToSignal(archon.location), distance);
	}
	
	public static void listenForInformation(RobotController rc, Brain brain){
		for (Signal signal : brain.thisTurnsSignals){
			if (signal.getTeam()==rc.getTeam() && signal.getMessage()!=null){
				int[] message = signal.getMessage();
				if (message[0]==shareDenLocationCode){
					MapLocation den = Message.convertSignalToMap(message[1]);
					brain.addDenLocation(den);
				} else if (message[0] == shareDeadDenCode){
					MapLocation den = Message.convertSignalToMap(message[1]);
					brain.addDeadDenLocation(den);
				} else if (Message.getCodeFromSignal(message[0]) == shareArchonInfoCode){
					int id = Message.convertSignalToID(message[0]);
					MapLocation loc = Message.convertSignalToMap(message[1]);
					RobotInfo r = new RobotInfo(id, rc.getTeam().opponent(), RobotType.ARCHON, loc, 0.,0.,0.,RobotType.ARCHON.maxHealth,RobotType.ARCHON.maxHealth,0,0);
					brain.addEnemyInfo(r);
				}
			}
		}
	}
	
}