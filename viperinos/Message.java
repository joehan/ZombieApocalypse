package viperinos;

import battlecode.common.*;

public class Message {

	public static int convertMapToSignal(MapLocation loc){
		return (int) (loc.x + (loc.y)*Math.pow(2, 10));
	}
	
	public static MapLocation convertSignalToMap(int signal){
		int x = (int) (signal % Math.pow(2, 10));
		int y = (int) (signal / Math.pow(2, 10));
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
	
	public static int convertIDToSignal(int ID, int code){
		return (int) (ID*Math.pow(2, 10) + code);
	}
	
	public static int convertSignalToID(int signal){
		int id = (int) (signal/Math.pow(2, 10));
		return id;
	}
	
	public static int getCodeFromSignal(int signal){
		return (int) (signal % Math.pow(2, 10));
	}
	
	
}