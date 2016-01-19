package betterScouting;


import battlecode.common.*;

/*
 * Entity contains functions that will be used by multiple types of units
 */
public class Entity {
	
	public static boolean canSenseArchon(RobotController rc, RobotInfo[] allies){
		int alliesLength = 0;
		for (int i = 0; i < alliesLength; i++){
			if (allies[i].type == RobotType.ARCHON){
				return true;
			}
		}
		return false;
	}
	
	public static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
        Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	
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
	
	
	public static boolean moveOptimalAttackRange(RobotController rc, Brain brain, RobotInfo[] enemies, RobotInfo enemy)
			throws GameActionException{
		if (rc.isCoreReady()){
			int maxAttackRange = rc.getType().attackRadiusSquared;
			MapLocation robotLocation = rc.getLocation();
//			RobotInfo enemy = Entity.findClosestEnemy(rc, brain, enemies, robotLocation);
			MapLocation enemyLoc = enemy.location;
			Direction dirToEnemy = robotLocation.directionTo(enemyLoc);
			Direction[] dirToTri = directionsToTry(dirToEnemy.opposite());
			int currentDistance = robotLocation.distanceSquaredTo(enemyLoc);
			if (robotLocation.distanceSquaredTo(enemyLoc) < 8 || (robotLocation.distanceSquaredTo(enemyLoc) > 13)){
				for (Direction dir : dirToTri){
					if (rc.senseRubble(robotLocation.add(dir)) < GameConstants.RUBBLE_SLOW_THRESH){
						int moveDistToEnemy = robotLocation.add(dir).distanceSquaredTo(enemyLoc);
						if (moveDistToEnemy <= maxAttackRange && 
								(moveDistToEnemy >= currentDistance || currentDistance > maxAttackRange)){
							boolean moved = Entity.safeMoveOneDirection(rc, enemies, brain, dir);
							if (moved){
								return true;
							}
						}
					}
				}
				for (Direction dir : dirToTri){
					if (rc.senseRubble(robotLocation.add(dir)) >= GameConstants.RUBBLE_SLOW_THRESH){
						int moveDistToEnemy = robotLocation.add(dir).distanceSquaredTo(enemyLoc);
						if (moveDistToEnemy <= maxAttackRange && 
								(moveDistToEnemy >= currentDistance || currentDistance > maxAttackRange)){
							boolean moved = Entity.safeMoveOneDirection(rc, enemies, brain, dir);
							if (moved){
								return true;
							}
						}
					}
				}
			}
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
	
	public static boolean fleeEnemies(RobotController rc, Brain brain, RobotInfo[] enemies, RobotInfo[] zombies, RobotInfo closestEnemy) throws GameActionException{
		boolean moved = false;
		if (enemies.length > 0 || zombies.length > 0){
			move(rc, brain, closestEnemy.location.directionTo(rc.getLocation()), false);
			moved = true;
		}
		return moved;
	}
	
	/*
	 *move attempts to move in dir.
	 *if Bug is try, it uses bug movement. OTherwise, it uses closest direction movement.
	 *Returns true if the robot moved, and false otherwise
	 */
	public static boolean move(RobotController rc, Brain brain, Direction dir, boolean bug) throws GameActionException{
		Direction[] directionsToTry;
		if(bug){
			Direction[] bugDirections = {dir, dir.rotateLeft(), dir.rotateLeft().rotateLeft(),
					dir.opposite().rotateRight(), dir.opposite(), dir.opposite().rotateLeft(),
					dir.rotateRight().rotateRight(), dir.rotateRight()};
			directionsToTry = bugDirections;
		} else {
			Direction[] normalDirections = {dir, dir.rotateLeft(), dir.rotateRight(),
					dir.rotateLeft().rotateLeft(), dir.rotateRight().rotateRight(),
					dir.opposite().rotateRight(), dir.opposite().rotateLeft(),
					dir.opposite()};
			directionsToTry = normalDirections;
		}
		for (Direction d : directionsToTry){
			if (rc.canMove(d)){
				rc.move(d);
				brain.lastDirectionMoved = d;
				return true;
			}
		}
		return false;
	}
	
	
}