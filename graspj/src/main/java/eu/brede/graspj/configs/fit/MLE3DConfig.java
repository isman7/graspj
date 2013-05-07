package eu.brede.graspj.configs.fit;

import java.io.Serializable;

import eu.brede.common.config.EnhancedConfig;
import eu.brede.graspj.configs.Global;

public class MLE3DConfig extends FitConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MLE3DConfig() {
		super();
		ensureCompliance();
	}
	
	@Override
	public EnhancedConfig getRequiredDefault() {
		EnhancedConfig requiredConfig = super.getRequiredDefault();
		requiredConfig.setPrefix("MLE3DConfig.");
		
		requiredConfig.put("dfCurveX", Global.dfcX());
		requiredConfig.put("dfCurveY", Global.dfcY());
		
		return requiredConfig;
	}
}
