package viperinos;

import battlecode.common.*;

public class Message {

	public static int convertMapToSignal(MapLocation loc){
		return (int) (loc.x + 16000 + (loc.y + 16000)*Math.pow(2, 16));
	}
	
	public static MapLocation convertSignalToMap(int signal){
		int x = (int) (signal % Math.pow(2, 16) - 16000);
		int y = (int) (signal / Math.pow(2, 16) - 16000);
		return new MapLocation(x, y);
	}
	
	public static int convertDirectionToSignal(Direction dir){
		int toInt = -1;
		for (int i = 0 ; i < Entity.directions.length ; i++){
			if (dir.equals(Entity.directions[i])){
				toInt = i;
			}
		}
		return toInt;
	}
	
	public static Direction convertSignalToDirection(int signal){
		return Entity.directions[signal];
	}
	
}