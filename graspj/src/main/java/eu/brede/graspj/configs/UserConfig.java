package eu.brede.graspj.configs;

import java.io.Serializable;

import eu.brede.common.config.EnhancedConfig;
import eu.brede.graspj.datatypes.WorkflowMap;
import eu.brede.graspj.opencl.utils.CLSystemGJ;


public class UserConfig extends EnhancedConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UserConfig() {
		super();
//		ensureCompliance();
	}
	
	@Override
	public EnhancedConfig getRequiredDefault() {
		EnhancedConfig requiredConfig = super.getRequiredDefault();
		requiredConfig.setPrefix("UserConfig.");
		
//		requiredConfig.initMetaData();
		requiredConfig.put("clSystem", CLSystemGJ.createWithSimpleGuess());
		requiredConfig.put("userWorkflowsOnExistent", new WorkflowMap());
		
		return requiredConfig;
	}

}