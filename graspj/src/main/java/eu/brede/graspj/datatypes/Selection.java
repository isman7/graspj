package eu.brede.graspj.datatypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class Selection<T> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Collection<T> options;
	private Collection<T> selected;
	
	public Selection(Collection<T> options, Collection<T> selected) {
		super();
		this.options = options;
		this.selected = selected;
	}
	
	public Selection(Collection<T> options) {
		this(options,new ArrayList<T>());
	}

	public Collection<T> getOptions() {
		return options;
	}
	public void setOptions(Collection<T> options) {
		this.options = options;
	}
	public Collection<T> getSelected() {
		return selected;
	}
	public void setSelected(Collection<T> selection) {
		this.selected = selection;
	}	
}
