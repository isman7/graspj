package eu.brede.graspj.configs.filtering;

import java.io.Serializable;

import eu.brede.common.config.EnhancedConfig;
import eu.brede.graspj.datatypes.Option;


public class FrameFilterConfig extends FilterConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FrameFilterConfig() {
		super();
		ensureCompliance();
//		Option filterByColorOption = new Option();
//		filterByColorOption.setProperty("color", new Integer(1));
//		filterByColorOption.setSelected(false);
//		
//		Option filterByTypeOption = new Option();
//		filterByTypeOption.setProperty("isActivation", false);
//		filterByTypeOption.setSelected(false);
//		
//		setProperty("metaData", new MetaData()); // new Range(min,max)
//		setProperty("filterByColor", filterByColorOption);
//		setProperty("filterByType", filterByTypeOption);
//		setProperty("encodeColor", false);
		
	}
	
	@Override
	public EnhancedConfig getRequiredDefault() {
		EnhancedConfig requiredConfig = super.getRequiredDefault();
		requiredConfig.setPrefix("FrameFilterConfig.");
		
		Option filterByColorOption = new Option();
		filterByColorOption.put("color", new Integer(1));
		filterByColorOption.setSelected(false);
		
		Option filterByTypeOption = new Option();
		filterByTypeOption.put("isActivation", false);
		filterByTypeOption.setSelected(false);
		
		requiredConfig.put("filterByColor", filterByColorOption);
		requiredConfig.put("filterByType", filterByTypeOption);
		requiredConfig.put("encodeColor", false);
		
		return requiredConfig;
	}
	
}
