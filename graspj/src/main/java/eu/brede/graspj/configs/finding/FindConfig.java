package eu.brede.graspj.configs.finding;

import eu.brede.common.config.EnhancedConfig;
import eu.brede.common.config.MetaData;

public class FindConfig extends EnhancedConfig {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FindConfig() {
		super();
//		ensureCompliance();
//		setProperty("metaData", new MetaData());
//		setProperty("maxSpotsPerFrame", new Integer(1024));
//		setProperty("threshold", new Integer(900));
//		setProperty("boxRadius", new Integer(3));
	}
	
	@Override
	public EnhancedConfig getRequiredDefault() {
		EnhancedConfig requiredConfig = super.getRequiredDefault();
		requiredConfig.setPrefix("FindConfig.");
		
		requiredConfig.put("metaData", new MetaData());
		requiredConfig.put("maxSpotsPerFrame", new Integer(1024));
		requiredConfig.put("threshold", new Integer(900));
		requiredConfig.put("boxRadius", new Integer(3));
        requiredConfig.put("subtractLocalBg", Boolean.FALSE);
		
		return requiredConfig;
	}
	
}
