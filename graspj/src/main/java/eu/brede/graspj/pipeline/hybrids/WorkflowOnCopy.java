package eu.brede.graspj.pipeline.hybrids;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import eu.brede.common.config.Configurable;
import eu.brede.common.config.EnhancedConfig;
import eu.brede.common.pipeline.hybrids.AbstractPCPipeline;
import eu.brede.graspj.configs.Global;
import eu.brede.graspj.datatypes.AnalysisItem;
import eu.brede.graspj.datatypes.AnalysisItem.CopyInstructions;
import eu.brede.graspj.pipeline.producers.SplitExistingProducer;

public class WorkflowOnCopy 
	extends AbstractPCPipeline<AnalysisItem,AnalysisItem,AnalysisItem>
	implements Configurable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private EnhancedConfig config;
//	private CopyProducingConsumer cpc;
	private SplitExistingProducer sep;

	public WorkflowOnCopy() {
		super(); 
		sep = new SplitExistingProducer();
		this.getProducers().add(sep);
		
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
		sep.setAnalysisItem(item.copy(instructions));
		Future<Object> pipeFuture = Global.INSTANCE.getExecutor().submit(pipeline);
		try {
			AnalysisItem resultItem = (AnalysisItem) pipeFuture.get();
			if(getQueueOut()!=null) {
				getQueueOut().put(resultItem);				
			}
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	@Override
	public void setProduct(AnalysisItem product) {
		CopyInstructions instructions = getConfig().gett("copyInstructionsProduct");
		super.setProduct(product.copy(instructions));
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
