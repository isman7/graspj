package eu.brede.graspj.configs.trail;

import java.io.Serializable;

import eu.brede.common.config.EnhancedConfig;
import eu.brede.common.config.MetaData;


public class TrailConfig extends EnhancedConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TrailConfig() {
		super();
		ensureCompliance();
//		put("metaData", new MetaData());
//		put("locationTolerance", new Integer(5));
//		put("locationToleranceNM", new Float(200));
//		put("frameSkipTolerance", new Integer(0));
//		put("maxTrailLength", new Integer(5));
//		put("minTrailLength", new Integer(0));
	}
	
	@Override
	public EnhancedConfig getRequiredDefault() {
		EnhancedConfig requiredConfig = super.getRequiredDefault();
		requiredConfig.setPrefix("TrailConfig.");
		
		requiredConfig.put("metaData", new MetaData());
		requiredConfig.put("locationTolerance", new Integer(5));
		requiredConfig.put("locationToleranceNM", new Float(200));
		requiredConfig.put("frameSkipTolerance", new Integer(0));
		requiredConfig.put("maxTrailLength", new Integer(5));
		requiredConfig.put("minTrailLength", new Integer(0));
		
		return requiredConfig;
	}
}
