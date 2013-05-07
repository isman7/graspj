package eu.brede.graspj.pipeline.hybrids;

import eu.brede.common.config.Configurable;
import eu.brede.common.config.EnhancedConfig;
import eu.brede.common.pipeline.hybrids.AbstractPCPipeline;
import eu.brede.graspj.configs.Global;
import eu.brede.graspj.datatypes.AnalysisItem;
import eu.brede.graspj.datatypes.AnalysisItem.CopyInstructions;

public class ContinousWorkflowOnCopy 
	extends AbstractPCPipeline<AnalysisItem,AnalysisItem,AnalysisItem>
	implements Configurable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private EnhancedConfig config;
//	private CopyProducingConsumer cpc;
	private InjectionProducer injector;

	public ContinousWorkflowOnCopy() {
		super(); 
		injector = new InjectionProducer();
		this.getProducers().add(injector);
		
		final CopyInstructions copyInstructionsProduct = new CopyInstructions();
		final CopyInstructions copyInstructionsItem = new CopyInstructions();
		
		config = new EnhancedConfig() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public EnhancedConfig getRequiredDefault() {
				EnhancedConfig requiredConfig = super.getRequiredDefault();
				requiredConfig.setPrefix("WorkflowOnCopy.");
				
				requiredConfig.initMetaData();
				requiredConfig.put("copyInstructionsProduct", copyInstructionsProduct);
				requiredConfig.put("copyInstructionsItem", copyInstructionsItem);
				return requiredConfig;
			}
			
		};
		pipeline.setExecutor(Global.INSTANCE.getExecutor());
	}

	@Override
	protected void use(AnalysisItem item) throws InterruptedException {
		CopyInstructions instructions = getConfig().gett("copyInstructionsItem");
		injector.use(item.copy(instructions));
	}
	
	
	
	
	
	@Override
	public Integer call() throws Exception {
		injector.setStopCondition(stopCondition);
		Global.INSTANCE.getExecutor().submit(pipeline);
		return super.call();
	}

	@Override
	public void setProduct(AnalysisItem product) {
		CopyInstructions instructions = getConfig().gett("copyInstructionsProduct");
		super.setProduct(product.copy(instructions));
	}

//	// TODO perhaps move to AnalysisItem
//	protected AnalysisItem copyAnalysisItem(AnalysisItem item, Option instructions) {
//		if(instructions.isSelected()) {
//			item = item.shallowCopy();
//			
//			Option newSpotCollection = instructions.gett("newSpotCollection");
//			Option newMask = instructions.gett("newMask");
//			Option newDrift = instructions.gett("newDrift");
//			
//			if(newSpotCollection.isSelected()) {
//				if(newSpotCollection.gett("copyOrgData")) {
//					item.setSpots(new BufferSpotCollection(item.getSpots()));
//				}
//				else {
//					item.setSpots(new BufferSpotCollection());
//				}
//			}
//			
//			if(newMask.isSelected()) {				
//				if(newMask.gett("copyOrgData")) {
//					item.setMask(new FilterMask(item.getMask()));
//				}
//				else {
//					item.setMask(new FilterMask());
//				}
//			}
//			
//			if(newDrift.isSelected()) {				
//				if(newDrift.gett("copyOrgData")) {
//					item.setDrift(new Drift(item.getDrift()));
//				}
//				else {
//					item.setDrift(new Drift());
//				}
//			}
//		}
//		return item;
//	}
	
	

	@Override
	public EnhancedConfig getConfig() {
		return config;
	}

	@Override
	public void setConfig(EnhancedConfig config) {
		this.config = config;
	}

}
