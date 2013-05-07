package eu.brede.graspj.datatypes;

import eu.brede.common.config.EnhancedConfig;

public class Option extends EnhancedConfig {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private boolean selected=false;
	
	public Option() {
		super();
	}
	
//	public Option(EnhancedConfig config) {
//		super(config);
//	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
}
