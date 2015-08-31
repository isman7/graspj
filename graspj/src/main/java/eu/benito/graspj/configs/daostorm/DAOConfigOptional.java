package eu.benito.graspj.configs.daostorm;

import java.io.Serializable;
import java.util.Map.Entry;

import eu.brede.common.config.EnhancedConfig;
import eu.brede.common.config.MetaData;
import eu.brede.graspj.configs.finding.FindConfig;
import eu.brede.graspj.configs.fit.FitConfig;

public class DAOConfigOptional extends EnhancedConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DAOConfig daoConfig;
	private FindConfig findConfig;
		
	public DAOConfigOptional(DAOConfig daoConfig, FindConfig findConfig) {
		super(false);
		this.daoConfig = daoConfig;
		this.findConfig = findConfig;
		super.ensureCompliance();
		
	}
	
	public DAOConfigOptional() {
		super(false);
		this.daoConfig = new DAOConfig();
		this.findConfig = new FindConfig();
		super.ensureCompliance();
		
	}
	
	@Override
	public EnhancedConfig getRequiredDefault() {
		EnhancedConfig requiredConfig = super.getRequiredDefault();
		requiredConfig.setPrefix("DAOConfigOpt.");
		requiredConfig.put("iterations", this.daoConfig.get("iterations"));
		for (int i=0; i<this.daoConfig.getInt("iterations"); i++){
			requiredConfig.put("threshold"+i, this.findConfig.get("threshold"));
		}
		
		
		
		return requiredConfig;
	}
	
}
