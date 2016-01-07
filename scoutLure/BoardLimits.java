package scoutLure;

public class BoardLimits {
	public int maxHeight, minHeight, maxWidth, minWidth;
//		this.maxHeight = this.minHeight = this.maxWidth = this.minWidth = null;
//		public boolean maxHeightFound, minHeightFound, maxWidthFound, minWidthFound;
	
	public static BoardLimits getNew(){
		BoardLimits ret = new BoardLimits();
		ret.maxHeight = ret.minHeight = ret.maxWidth = ret.minWidth = (Integer) null;
		return ret;
	}
}
