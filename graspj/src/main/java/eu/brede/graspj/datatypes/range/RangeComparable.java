package eu.brede.graspj.datatypes.range;


public class RangeComparable<T extends Comparable<T>> extends Range<T> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public RangeComparable(T min, T max) {
		super(min,max);
	}


	public boolean contains(T value) {
		return
			getMin().compareTo(value) <= 0 &&
			getMax().compareTo(value) >= 0;
	}
	
//	public Class<T> getType() {
//		return type;
//	}
}
