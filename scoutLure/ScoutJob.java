package scoutLure;

public class ScoutJob {
	public boolean haveXScout, haveYScout;
	
	public static ScoutJob getDefault(){
		ScoutJob job = new ScoutJob();
		job.haveYScout = job.haveXScout = false;
		return job;
	}
}
