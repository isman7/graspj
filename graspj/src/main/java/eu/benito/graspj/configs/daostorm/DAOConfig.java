package eu.benito.graspj.configs.daostorm;

import java.io.Serializable;

import eu.brede.common.config.EnhancedConfig;
import eu.brede.common.config.MetaData;

public class DAOConfig extends EnhancedConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DAOConfig() {
		super();
	}
	
	@Override
	public EnhancedConfig getRequiredDefault() {
		EnhancedConfig requiredConfig = super.getRequiredDefault();
		requiredConfig.setPrefix("DaoConfig.");
		
		requiredConfig.put("metaData", new MetaData());
		requiredConfig.put("iterations", new Integer(3));
		
	
		
		return requiredConfig;
	}
	
}
