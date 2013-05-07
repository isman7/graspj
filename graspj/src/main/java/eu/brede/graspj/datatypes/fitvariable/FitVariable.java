package eu.brede.graspj.datatypes.fitvariable;

import java.io.Serializable;

public class FitVariable<T> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private T value;
	private T uncertainty;

	public FitVariable(T value, T uncertainty) {
		this.value = value;
		this.uncertainty = uncertainty;
	}

	public T getValue() {
		return value;
	}

	public T getUncertainty() {
		return uncertainty;
	}

}
