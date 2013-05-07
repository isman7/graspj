package eu.brede.graspj.datatypes.cycle;

public class CycleWithOffset extends Cycle {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;	
	
	private int offset;
	
	public CycleWithOffset(Cycle cycle, int offset) {
		super(cycle);
		this.offset = offset;
	}

	@Override
	public int getCycleEntryNr(int frameNr) {
		return (frameNr+offset) % this.size();
	}
	
	public int getOffset() {
		return offset;
	}
}
