package betterScouting;


import battlecode.common.*;

public class Soldier {
	
	public void run(RobotController rc, Brain brain){
		
		while (true) {
			try {
//				rc.setIndicatorString(2, String.valueOf(Clock.getBytecodeNum()));

				//First get info, sense enemies, etc.
				RobotInfo[] allies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam());
				RobotInfo[] opponents = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam().opponent());
				RobotInfo[] zombies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, Team.ZOMBIE);
				RobotInfo[] enemies = Entity.concat(opponents, zombies);
				RobotInfo nearestEnemy =  enemies.length > 0 ? Entity.findClosestEnemy(rc, brain, enemies, rc.getLocation()) : null;
				//Then do messaging
				
				brain.thisTurnsSignals = rc.emptySignalQueue();
				Squad.listenForCommands(rc, brain);
				
				//Then attack
				attack(rc, zombies, opponents);
				
				//Then combat move
				if (enemies.length>0){
					combatMove(rc, opponents, enemies, brain, nearestEnemy);
				}
				//Then if not in combat non-combat move
				if (rc.isCoreReady() && brain.leaderMovingInDirection!=null){
					Direction dirToMove = rc.getLocation().directionTo(brain.leaderLocation.add(brain.leaderMovingInDirection));
					Entity.move(rc, brain, dirToMove, false);
				}
				
				//End the turn
				Clock.yield();
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public boolean combatMove(RobotController rc, RobotInfo[] opponents, RobotInfo[] enemies, 
			Brain brain, RobotInfo nearestEnemy) throws GameActionException {
		if (rc.isCoreReady() && nearestEnemy != null){
			//First check if health is low, and if it is retreat
			//TODO change this to a reasonable value
			if (rc.getHealth() < rc.getType().maxHealth/3) {
				//Safe move towards nearest Archon unless fatally infected
				if (rc.getViperInfectedTurns()*2 > rc.getHealth()){
					if (nearestEnemy != null && nearestEnemy.team == rc.getTeam().opponent()){
						Entity.moveToLocation(rc, nearestEnemy.location);
					} else {
						//Move away from nearest archon
						//TODO implement nearest archon and moving away from it
						if (brain.leaderLocation != null){
							Entity.moveInDirection(rc, rc.getLocation().directionTo(brain.leaderLocation));
						} else {
							Entity.moveInDirection(rc, rc.getLocation().directionTo(nearestEnemy.location).opposite());
						}
					}
				} else {
					//Move towards nearest archon
					//TODO implement nearest archon in brain and change direction to this
					Entity.safeMove(rc, enemies, brain, rc.getLocation().directionTo(nearestEnemy.location));
				}
			} else {
				//Now we just want to stay at optimal move range for all enemies
				//AvoidMelee move to optimal attack range
				Entity.moveOptimalAttackRange(rc, brain, enemies, nearestEnemy);
			}
		}
		return false;
	}
	
	
	
	public static RobotType[] orderToAttack = {RobotType.VIPER, RobotType.TURRET, RobotType.TTM,
		RobotType.SOLDIER, RobotType.GUARD, RobotType.ARCHON, RobotType.RANGEDZOMBIE, RobotType.FASTZOMBIE, 
		RobotType.BIGZOMBIE, RobotType.STANDARDZOMBIE, RobotType.SCOUT, RobotType.ZOMBIEDEN};
	
	/*
	 * Attack first checks to see if there are any opponents in range.  If there are it attacks them
	 * in the order specified above, attacking the lowest health unit in its attacking range.  It then goes
	 * to check if there are any zombies in range, and attacks the lowest health one in the order specified above.
	 * So, it would attack a 200 hp big zombie before a 40 hp standard zombie because big zombies have
	 * higher priority
	 */
	private boolean attack(RobotController rc, RobotInfo[] zombies, RobotInfo[] enemies) throws GameActionException{
		if (rc.isWeaponReady() && enemies.length>0){
			RobotInfo[] closeEnemies = rc.senseHostileRobots(rc.getLocation(), rc.getType().attackRadiusSquared);
			int enemyLength = closeEnemies.length;
			RobotInfo[] viper = new RobotInfo[enemyLength];
			RobotInfo[] turret = new RobotInfo[enemyLength];
			RobotInfo[] ttm = new RobotInfo[enemyLength];
			RobotInfo[] soldier = new RobotInfo[enemyLength];
			RobotInfo[] guard = new RobotInfo[enemyLength];
			RobotInfo[] archon = new RobotInfo[enemyLength];
			RobotInfo[] scout = new RobotInfo[enemyLength];
			
			int viperIndex = 0;
			int turretIndex = 0;
			int ttmIndex = 0;
			int soldierIndex = 0;
			int guardIndex = 0;
			int archonIndex = 0;
			int scoutIndex = 0;
			
			for (int i = 0; i < closeEnemies.length; i ++){
				RobotInfo opponent = closeEnemies[i];
				switch(opponent.type) {
					case ARCHON:
						archon[archonIndex] = opponent;
						archonIndex++;
						break;
					case VIPER:
						viper[viperIndex] = opponent;
						viperIndex++;
						break;
					case TURRET:
						turret[turretIndex] = opponent;
						turretIndex++;
						break;
					case TTM:
						ttm[ttmIndex] = opponent;
						ttmIndex++;
						break;
					case GUARD:
						guard[guardIndex] = opponent;
						guardIndex++;
						break;
					case SOLDIER:
						soldier[soldierIndex] = opponent;
						soldierIndex++;
						break;
					case SCOUT:
						scout[scoutIndex] = opponent;
						scoutIndex++;
						break;
				}
			}
			
			RobotInfo[][] iterate = {viper, turret, ttm, soldier, 
					guard, archon, scout};
			
			int[] indexes = {viperIndex, turretIndex, ttmIndex, soldierIndex, guardIndex, archonIndex, 
					 scoutIndex};
			double min = 10000;
			MapLocation toAttack = null;
			for (int j = 0; j < 7; j++){
				RobotInfo[] currentList = iterate[j];
				for (int i = 0; i < indexes[j]; i ++){
					RobotInfo current = currentList[i];
					MapLocation loc = current.location;
					if (current.health < min && rc.canAttackLocation(loc)){
						min = current.health;
						toAttack = loc;
					}
				}
				if (toAttack != null){
					rc.attackLocation(toAttack);
					return true;
				}
			}
		}
		
		else if (rc.isWeaponReady() && zombies.length>0){
			int enemyLength = zombies.length;
			RobotInfo[] rangedZombie = new RobotInfo[enemyLength];
			RobotInfo[] fastZombie = new RobotInfo[enemyLength];
			RobotInfo[] bigZombie = new RobotInfo[enemyLength];
			RobotInfo[] standardZombie = new RobotInfo[enemyLength];
			RobotInfo[] zombieDen = new RobotInfo[enemyLength];

			int zombieDenIndex = 0;
			int rangedZombieIndex = 0;
			int fastZombieIndex = 0;
			int bigZombieIndex = 0;
			int standardZombieIndex = 0;
			
			for (int i = 0; i < enemyLength;  i++){
				RobotInfo opponent = zombies[i];
				if (rc.getLocation().distanceSquaredTo(opponent.location) <= 13){
					switch(opponent.type){
						case RANGEDZOMBIE:
							rangedZombie[rangedZombieIndex] = opponent;
							rangedZombieIndex ++;
							break;
						case FASTZOMBIE:
							fastZombie[fastZombieIndex] = opponent;
							fastZombieIndex ++;
							break;
						case BIGZOMBIE:
							bigZombie[bigZombieIndex] = opponent;
							bigZombieIndex ++;
							break;
						case STANDARDZOMBIE:
							standardZombie[standardZombieIndex] = opponent;
							standardZombieIndex ++;
							break;
						case ZOMBIEDEN:
							zombieDen[zombieDenIndex] = opponent;
							zombieDenIndex ++;
							break;
					}
				}
			}
			RobotInfo[][] iterate = { rangedZombie, fastZombie, bigZombie, standardZombie, zombieDen};
			int[] indexes = {rangedZombieIndex, fastZombieIndex, bigZombieIndex, standardZombieIndex, zombieDenIndex};
			
			double min = 100000;

			MapLocation toAttack = null;
			for (int j = 0; j < 5; j++){
				RobotInfo[] currentList = iterate[j];
				for (int i = 0; i < indexes[j]; i ++) {
					RobotInfo current = currentList[i];
					MapLocation loc = current.location;
					if (current.health < min){
						min = current.health;
						toAttack = loc;
					}
				}
				if (toAttack != null){
					rc.attackLocation(toAttack);
					return true;
				}
			}
		}
		return false;
	}
	
	
	
	
	
	
}