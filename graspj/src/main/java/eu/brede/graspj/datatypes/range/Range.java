package eu.brede.graspj.datatypes.range;

import java.io.Serializable;

public class Range<T> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private T min = null;
	private T max = null;

	public Range(T min, T max) {
//		this.type = (Class<T>) start.getClass();
		this.min = min;
		this.max = max;
	}
	
	

	public T getMin() {
		return min;
	}



	public T getMax() {
		return max;
	}
	
//	public Class<T> getType() {
//		return type;
//	}
}
