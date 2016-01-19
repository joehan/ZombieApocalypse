package viperinos;

import java.util.Random;


import battlecode.common.*;

public class Scout {
	
		
		public void run(RobotController rc, Brain brain){
			brain.getMapSymmetry(rc.getInitialArchonLocations(rc.getTeam()), 
					rc.getInitialArchonLocations(rc.getTeam().opponent()));
			
//			MapLocation toGo = brain.flipAcrossAxis(rc.getLocation());
			Random rand = new Random(rc.getID());
			Direction randomDir = Entity.directions[rand.nextInt(8)];
			Direction currentDir = randomDir;
			
			while (true){
				try {
					RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), rc.getType().sensorRadiusSquared);
					Signal[] signals = rc.emptySignalQueue();
					Entity.searchForDen(rc, brain);

					for (Signal signal : signals){
						if (brain.isArchon(signal.getID()) && 
								brain.enemyInfo[signal.getID()] != signal.getLocation()){
							rc.setIndicatorString(1, "saw an archon signal");
							//TODO Fix this fucking shit
							RobotInfo r = new RobotInfo(signal.getID(), rc.getTeam().opponent(), RobotType.ARCHON, signal.getLocation(), 0.,0.,0.,RobotType.ARCHON.maxHealth,RobotType.ARCHON.maxHealth,0,0);
							brain.addEnemyInfo(r);
							Squad.shareArchonInfo(rc, signal.getLocation(), signal.getID(), 5000);
						}
					}
					ZombieSpawnSchedule spawn = rc.getZombieSpawnSchedule();
					
					boolean move = false;
					int randomNum = rand.nextInt(8);
					int i = 0;
					while (!move && i < 8 && rc.isCoreReady()){
						move = Entity.safeMoveOneDirectionRanged(rc, enemies, brain, currentDir);
						if (!move){
							currentDir = Entity.directions[(randomNum + i)%8];
						}
						i ++;
					}
					if (!move){
						//scout is trapped
						Entity.moveRandomDirection(rc, brain);
					}
//					boolean val = Entity.moveInDirection(rc, rc.getLocation().directionTo(toGo));
					Entity.addArchonsToBrain(rc, enemies, brain);
					Clock.yield();
				}
				catch (Exception e){
					
				}
			}
		}
}