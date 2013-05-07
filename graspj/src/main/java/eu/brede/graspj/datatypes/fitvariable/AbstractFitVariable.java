package eu.brede.graspj.datatypes.fitvariable;

import java.util.ArrayList;
import java.util.Collection;


public abstract class AbstractFitVariable implements FloatFitVariable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Collection<String> toStringCollection() {
		ArrayList<String> outputStringCollection = new  ArrayList<String>();
		outputStringCollection.add(String.valueOf(value()));
		outputStringCollection.add(String.valueOf(uncertainty()));
		return outputStringCollection;
	}
}
