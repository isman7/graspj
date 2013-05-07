package eu.brede.graspj.pipeline.hybrids;

import java.io.Serializable;

import eu.brede.common.config.Configurable;
import eu.brede.common.config.EnhancedConfig;
import eu.brede.common.config.MetaData;
import eu.brede.common.pipeline.NeedsProduct;
import eu.brede.common.pipeline.hybrids.ConsumptionPipeline;
import eu.brede.common.pipeline.hybrids.ProductionPipeline;
import eu.brede.graspj.datatypes.AnalysisItem;
import eu.brede.graspj.datatypes.Drift;
import eu.brede.graspj.datatypes.filtermask.FilterMask;
import eu.brede.graspj.datatypes.spotcollection.BufferSpotCollection;

public class PipeOnShallowCopy extends ConsumptionPipeline<AnalysisItem,AnalysisItem>
	implements NeedsProduct<AnalysisItem>, Configurable, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private EnhancedConfig config;

	public PipeOnShallowCopy() {
		super(new ShallowCopier(), 
				new ProductionPipeline<AnalysisItem,AnalysisItem>());
//		config = new EnhancedConfig();
//		config.put("metaData", new MetaData());
//		config.put("newSpotCollection", Boolean.valueOf(false));
//		config.put("newMask", Boolean.valueOf(false));
//		config.put("newDrift", Boolean.valueOf(false));
		
		config = new EnhancedConfig() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public EnhancedConfig getRequiredDefault() {
				EnhancedConfig requiredConfig = super.getRequiredDefault();
				requiredConfig.setPrefix("PipeOnShallowCopy.");
				
				requiredConfig.put("metaData", new MetaData());
				requiredConfig.put("newSpotCollection", Boolean.valueOf(false));
				requiredConfig.put("newMask", Boolean.valueOf(false));
				requiredConfig.put("newDrift", Boolean.valueOf(false));
				return requiredConfig;
			}
			
		};
		
		config.ensureCompliance();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setProduct(AnalysisItem product) {
		if(pipeline instanceof NeedsProduct<?>) {
			AnalysisItem shallow = product.shallowCopy();
			if(getConfig().getBoolean("newSpotCollection")) {
				shallow.setSpots(new BufferSpotCollection());
			}
			if(getConfig().getBoolean("newMask")) {
				shallow.setMask(new FilterMask());
			}
			if(getConfig().getBoolean("newDrift")) {
				shallow.setDrift(new Drift());
			}
			((NeedsProduct<AnalysisItem>)pipeline).setProduct(shallow);
		}
	}

	@Override
	public EnhancedConfig getConfig() {
		return config;
	}

	@Override
	public void setConfig(EnhancedConfig config) {
		this.config = config;
	}

}
