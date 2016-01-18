package betterScouting;

import battlecode.common.*;

/*
 * Entity contains functions that will be used by multiple types of units
 */
public class Entity {
	
	public static boolean inDanger(RobotInfo[] enemies, MapLocation loc, boolean ranged){
		if (ranged){
			for (RobotInfo enemy : enemies){
				if (enemy.location.distanceSquaredTo(loc) <= enemy.type.attackRadiusSquared){
					return true;
				}
			}
		} else {
			for (RobotInfo enemy : enemies){
				if (enemy.location.distanceSquaredTo(loc) < 3){
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean safeMoveOneDirection(RobotController rc, RobotInfo[] enemies,
			Brain brain, Direction dir) throws GameActionException{
		if (rc.isCoreReady()){
			MapLocation robotLocation = rc.getLocation();
			Direction currentDir = dir;
			MapLocation newLoc = robotLocation.add(currentDir);
			if (!inDanger(enemies, newLoc, false) && rc.canMove(currentDir)){
				rc.move(currentDir);
				return true;
			}
		}
		return false;
	}
	
	
	public static boolean moveAvoidMelee(RobotController rc, Brain brain, RobotInfo[] enemies, 
			RobotInfo nearestEnemy) throws GameActionException{
		rc.setIndicatorString(0, String.valueOf(Clock.getBytecodeNum()));
		Direction[] directions = directionsToTry(Direction.NORTH);
		RobotInfo[] meleeEnemies = new RobotInfo[enemies.length];
		int meleeEnemyLength = 0;
		RobotInfo[] rangedEnemies = new RobotInfo[enemies.length];
		int enemyLength = enemies.length;
		for (int i = 0; i < enemies.length; i ++){
			RobotInfo enemy = enemies[i];
			if (enemy.type.attackRadiusSquared == 2){
				meleeEnemies[meleeEnemyLength++] = enemy;
			} else {
				rangedEnemies[i - meleeEnemyLength] = enemy;
			}
		}
		rc.setIndicatorString(1, String.valueOf(Clock.getBytecodeNum()));

		int[] closestMeleeEnemy = new int[9];
		boolean[] rangeEnemyInRange = new boolean[9];
		for (int i = 0; i < 9; i ++){
			Direction direction;
			MapLocation newLoc;
			if (i==8){
				direction = Direction.NONE;
				newLoc = rc.getLocation();
			}else {
				direction = directions[i];
				newLoc = rc.getLocation().add(direction);
			}
			int lowest = 0;
			if (rc.canMove(direction) && 
					rc.senseRubble(rc.getLocation().add(direction)) < GameConstants.RUBBLE_SLOW_THRESH|| direction == Direction.NONE){
				lowest = 100;
				for (int j = 0; j < meleeEnemyLength; j ++){
					int distance = newLoc.distanceSquaredTo(meleeEnemies[j].location);
					if (distance < lowest){
						lowest = distance;
					}
				}
				closestMeleeEnemy[i] = lowest;
				int upperBound = enemyLength - meleeEnemyLength;
				for (int j = 0; j < upperBound; j ++){
					int distance = newLoc.distanceSquaredTo(rangedEnemies[j].location);
					if (distance <= 13){
						rangeEnemyInRange[i] = true;
						break;
					}
				}
			}
			closestMeleeEnemy[i] = lowest;
		}
		int maxIndex = 0;
		Direction toMove = Direction.NONE;
		for (int i = 0; i < 9; i++){
			int closest = closestMeleeEnemy[i];
			Direction dir = (i == 8) ? Direction.NONE : directions[i];
			if (closest > maxIndex && (closest <= 13 || rangeEnemyInRange[i]) 
					){
				maxIndex = closest;
				toMove = dir;
			}
		}
		if (toMove != Direction.NONE){
			rc.move(toMove);
			return true;
		}
		
		rc.setIndicatorString(2, String.valueOf(Clock.getBytecodeNum()));

		for (int i = 0; i < 9; i ++){
			Direction direction;
			MapLocation newLoc;
			if (i==8){
				direction = Direction.NONE;
				newLoc = rc.getLocation();
			}else {
				direction = directions[i];
				newLoc = rc.getLocation().add(direction);
			}
			int lowest = 0;
			if (rc.canMove(direction) &&
					rc.senseRubble(rc.getLocation().add(direction)) >= GameConstants.RUBBLE_SLOW_THRESH|| direction == Direction.NONE){
				lowest = 100;
				for (int j = 0; j < meleeEnemyLength; j ++){
					int distance = newLoc.distanceSquaredTo(meleeEnemies[j].location);
					if (distance < lowest){
						lowest = distance;
					}
				}
				closestMeleeEnemy[i] = lowest;
				int upperBound = enemyLength - meleeEnemyLength;
				for (int j = 0; j < upperBound; j ++){
					int distance = newLoc.distanceSquaredTo(rangedEnemies[j].location);
					if (distance <= 13){
						rangeEnemyInRange[i] = true;
						break;
					}
				}
			}
			closestMeleeEnemy[i] = lowest;
		}
		for (int i = 0; i < 9; i++){
			int closest = closestMeleeEnemy[i];
			Direction dir = (i == 8) ? Direction.NONE : directions[i];
			if (closest > maxIndex && (closest <= 13 || rangeEnemyInRange[i])){
				maxIndex = closest;
				toMove = dir;
			}
		}
		if (toMove != Direction.NONE){
			rc.move(toMove);
			return true;
		}

		return false;
	}
	
	public static Direction[] directionsToTry(Direction dir){
		Direction[] ret = {dir, dir.rotateRight(), dir.rotateLeft(), dir.rotateRight().rotateRight(), dir.rotateLeft().rotateLeft(),
			dir.rotateRight().rotateRight().rotateRight(), dir.rotateLeft().rotateLeft().rotateLeft(),
			dir.opposite()};
		return ret;
	}
	
	public static boolean safeMove(RobotController rc, RobotInfo[] enemies, Brain brain, Direction dir
			) throws GameActionException{
		Direction[] dirToTry = directionsToTry(dir);
		for (int i = 0; i < 8; i++){
			Direction currentDir = dirToTry[i];
			MapLocation newLoc = rc.getLocation().add(currentDir);
			if (rc.canMove(currentDir) && rc.senseRubble(newLoc) < GameConstants.RUBBLE_SLOW_THRESH &&
					!currentDir.isDiagonal()){
				boolean valid = true;
				for (int j = enemies.length; --j >= 0;){
					RobotInfo currentEnemy = enemies[j];
					if (newLoc.distanceSquaredTo(currentEnemy.location) <= currentEnemy.type.attackRadiusSquared){
						valid = false;
						break;
					}
				}
				if (valid){
					rc.move(currentDir);
					return true;
				}
			}
		}
		for (int i = 0; i < 8 ; i++){
			Direction currentDir = dirToTry[i];
			MapLocation newLoc = rc.getLocation().add(currentDir);
			if (rc.canMove(currentDir) && rc.senseRubble(newLoc) < GameConstants.RUBBLE_SLOW_THRESH){
				boolean valid = true;
				for (int j = enemies.length; --j >= 0;){
					RobotInfo currentEnemy = enemies[j];
					if (newLoc.distanceSquaredTo(currentEnemy.location) <= currentEnemy.type.attackRadiusSquared){
						valid = false;
						break;
					}
				}
				if (valid){
					rc.move(currentDir);
					return true;
				}
			}
		}
		for (int i = 0; i < 8 ; i++){
			Direction currentDir = dirToTry[i];
			MapLocation newLoc = rc.getLocation().add(currentDir);
			if (rc.canMove(currentDir)){
				boolean valid = true;
				for (int j = enemies.length; --j >= 0;){
					RobotInfo currentEnemy = enemies[j];
					if (newLoc.distanceSquaredTo(currentEnemy.location) <= currentEnemy.type.attackRadiusSquared){
						valid = false;
						break;
					}
				}
				if (valid){
					rc.move(currentDir);
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean fatallyInfected(int health, int turnsViperInfection){
		return turnsViperInfection*2 > health;
	}
	
	public static RobotInfo[] concat(RobotInfo[] a, RobotInfo[] b) {
		   int aLen = a.length;
		   int bLen = b.length;
		   RobotInfo[] c= new RobotInfo[aLen+bLen];
		   System.arraycopy(a, 0, c, 0, aLen);
		   System.arraycopy(b, 0, c, aLen, bLen);
		   return c;
		}
	
	
	public static RobotType[] orderToAttack = {RobotType.VIPER, RobotType.TURRET, RobotType.TTM,
		RobotType.SOLDIER, RobotType.GUARD, RobotType.ARCHON, RobotType.RANGEDZOMBIE, RobotType.FASTZOMBIE, 
		RobotType.BIGZOMBIE, RobotType.STANDARDZOMBIE, RobotType.SCOUT, RobotType.ZOMBIEDEN};
	
	/*
	 * attackHostiles looks for hostile enemies within the current units attack range
	 * and attacks the weakest of them
	 * It returns true if the robot attacked, and false otherwise
	 */
	public static Boolean attackHostiles(RobotController rc, RobotInfo[] enemiesInAttackRange) throws GameActionException {
//		Boolean attacked = false;
		if (enemiesInAttackRange.length > 0){
			if (rc.isWeaponReady()){
				RobotInfo weakestSoFar=null;
				double healthOfWeakest=1;
				for (int i = 0; i < orderToAttack.length; i ++){
					for (RobotInfo enemy : enemiesInAttackRange){
						double currentHealth = enemy.health/enemy.maxHealth;
						if ((weakestSoFar==null || currentHealth < healthOfWeakest) && enemy.type == orderToAttack[i]
								&& rc.canAttackLocation(enemy.location)){
							weakestSoFar=enemy;
							healthOfWeakest = currentHealth;
						}
					}
					if (!(weakestSoFar == null)){
						rc.attackLocation(weakestSoFar.location);
						return true;
					}
				}
//				attacked = true;
			}
		}
		return false;
	}
	
	
	public static boolean basicAttack(RobotController rc, MapLocation enemyLoc) throws GameActionException{
		if (rc.canAttackLocation(enemyLoc) && rc.isWeaponReady()){
			rc.attackLocation(enemyLoc);
			return true;
		}
		return false;
	}
	
	/*
	 * Searches all enemies in enemies and returns the one that is the closest.
	 */
	public static RobotInfo findClosestEnemy(RobotController rc, Brain brain, RobotInfo[] enemies, MapLocation loc){
		int distance = 100;
		RobotInfo closestEnemy = enemies[0];
		for (RobotInfo enemy : enemies){
			int distToEnemy = loc.distanceSquaredTo(enemy.location);
			if (distToEnemy < distance){
				distance = distToEnemy;
				closestEnemy = enemy;
			}
		}
		return closestEnemy;
	}
	
	/*
	 * This method will look at all enemies in enemies, check if there are any archons, and if so
	 * add them to the archon store in brain.
	 */
	public static void addArchonsToBrain(RobotController rc, RobotInfo[] enemies, Brain brain){
		int enemiesLength = enemies.length;
		for (int i = 0; i < enemiesLength; i ++){
			if (enemies[i].type == RobotType.ARCHON){
				brain.addArchon(enemies[i]);
				rc.setIndicatorString(0, "found archon");
			}
		}
	}
	
	public static boolean moveInDirectionClearRubble(RobotController rc, Direction dir) throws GameActionException{
		if (rc.isCoreReady()){
			if (rc.canMove(dir)){
				rc.move(dir);
				return true;
			}
			else if (rc.canMove(dir.rotateLeft())){
				rc.move(dir.rotateLeft());
				return true;
			}
			else if (rc.canMove(dir.rotateRight())){
				rc.move(dir.rotateRight());
				return true;
			} else if (rc.senseRubble(rc.getLocation().add(dir)) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH){
				rc.clearRubble(dir);
				return true;
			} else if (rc.senseRubble(rc.getLocation().add(dir.rotateLeft())) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH){
				rc.clearRubble(dir.rotateLeft());
				return true;
			} else if (rc.senseRubble(rc.getLocation().add(dir.rotateRight())) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH){
				rc.clearRubble(dir.rotateRight());
				return true;
			} 
		}
		return false;
	}
	
	public static boolean moveToLocation(RobotController rc, MapLocation loc) throws GameActionException{
		return moveInDirection(rc, rc.getLocation().directionTo(loc));
	}
	
	/*
	 * This method will try to move in the general direction of dir.  If it cant move in that direction
	 * it will try to move in a diagonal direction, then perpendicular direction.
	 */
	public static boolean moveInDirection(RobotController rc, Direction dir) throws GameActionException{
		if (rc.isCoreReady()){
			if (rc.canMove(dir)){
				rc.move(dir);
				return true;
			}
			else if (rc.canMove(dir.rotateLeft())){
				rc.move(dir.rotateLeft());
				return true;
			}
			else if (rc.canMove(dir.rotateRight())){
				rc.move(dir.rotateRight());
				return true;
			}
			else if (rc.canMove(dir.rotateLeft().rotateLeft())){
				rc.move(dir.rotateLeft().rotateLeft());
				return true;
			}
			else if (rc.canMove(dir.rotateRight().rotateRight())){
				rc.move(dir.rotateRight().rotateRight());
				return true;
			}
		}
		return false;
	}
	
}