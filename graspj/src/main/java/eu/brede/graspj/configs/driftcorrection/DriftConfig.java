package eu.brede.graspj.configs.driftcorrection;

import java.io.Serializable;

import eu.brede.common.config.EnhancedConfig;
import eu.brede.common.config.MetaData;
import eu.brede.graspj.configs.rendering.RenderConfig;

public class DriftConfig extends EnhancedConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DriftConfig() {
		super();
//		ensureCompliance();
//		setProperty("metaData", new MetaData());
//		setProperty("framesPerRendering", new Integer(1500));
//		RenderConfig renderConfig = new RenderConfig();
//		renderConfig.setProperty("complex", true);
//		setProperty("renderConfig", renderConfig);
//		getMetaData().setName("std");
	}
	
	@Override
	public EnhancedConfig getRequiredDefault() {
		EnhancedConfig requiredConfig = super.getRequiredDefault();
		requiredConfig.setPrefix("DriftConfig.");
		
		requiredConfig.put("metaData", new MetaData());
//		requiredConfig.setProperty("framesPerRendering", new Integer(1500));
		RenderConfig renderConfig = new RenderConfig();
		renderConfig.put("complex", true);
		requiredConfig.put("renderConfig", renderConfig);
		return requiredConfig;
	}
}
