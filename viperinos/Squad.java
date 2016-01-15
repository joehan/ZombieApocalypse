package viperinos;

import battlecode.common.*;

public class Squad {
	
	public static int movingDirectionCode = 1;
	
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
	
}