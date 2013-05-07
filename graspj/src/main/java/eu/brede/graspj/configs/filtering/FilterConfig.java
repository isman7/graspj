package eu.brede.graspj.configs.filtering;

import java.io.Serializable;

import eu.brede.common.config.EnhancedConfig;
import eu.brede.graspj.datatypes.ObjectChoice;


public class FilterConfig extends EnhancedConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public EnhancedConfig getRequiredDefault() {
		EnhancedConfig requiredConfig = super.getRequiredDefault();
		requiredConfig.setPrefix("FilterConfig.");
		
		// TODO improve options of mask application
		requiredConfig.initMetaData();
		requiredConfig.put("applyMask", false);
		ObjectChoice<String> combinationMode = 
				new ObjectChoice<>("override","multiply");
		combinationMode.setChosen("multiply");
		requiredConfig.put("combinationMode", combinationMode);
		
		return requiredConfig;
	}
}
