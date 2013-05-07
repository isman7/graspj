package eu.brede.graspj.configs.fit;

import java.io.Serializable;

import eu.brede.common.config.EnhancedConfig;
import eu.brede.common.config.MetaData;

public class FitConfig extends EnhancedConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FitConfig() {
		super();
	}
	
	@Override
	public EnhancedConfig getRequiredDefault() {
		EnhancedConfig requiredConfig = super.getRequiredDefault();
		requiredConfig.setPrefix("FitConfig.");
		
		requiredConfig.put("metaData", new MetaData());
		requiredConfig.put("fitDimension", new Integer(5));
		requiredConfig.put("variablesPerDimension", new Integer(2));
		requiredConfig.put("boxRadius", new Integer(3));
		requiredConfig.put("sigmaPSF", new Float(1.05f));
		requiredConfig.put("iterations", new Integer(5));
		
		return requiredConfig;
	}
	
}
