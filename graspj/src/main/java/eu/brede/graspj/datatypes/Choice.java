package eu.brede.graspj.datatypes;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Set;

import eu.brede.common.config.EnhancedConfig;

public class Choice implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private LinkedHashMap<String,EnhancedConfig> optionMap; 
	private String chosen;
	
	public Choice() {
		optionMap = new LinkedHashMap<>();
	}
	
	public Set<String> getOptions() {
		return optionMap.keySet();
	}
	
	public String getChosen() {
		return chosen;
	}
	
	public boolean setChosen(String choice) {
		if(optionMap.containsKey(choice)) {
			chosen = choice;
			return true;
		}
		else {
			return false;
		}
	}
	
	public LinkedHashMap<String,EnhancedConfig> getMap() {
		return optionMap;
	}
}
