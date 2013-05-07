package eu.brede.graspj.datatypes;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.brede.common.config.Configurable;
import eu.brede.common.config.EnhancedConfig;
import eu.brede.common.config.MetaData;
import eu.brede.common.pipeline.Handler;
import eu.brede.common.pipeline.Processor;
import eu.brede.common.pipeline.consumers.Consumer;
import eu.brede.common.pipeline.hybrids.ProductionPipeline;
import eu.brede.common.pipeline.producers.Producer;
import eu.brede.graspj.configs.Global;

public class ProductionWorkflow extends ProductionPipeline<AnalysisItem,AnalysisItem> 
	implements Workflow, Configurable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private EnhancedConfig config;

	private transient ReportingHandler handler;
	final static Logger logger = LoggerFactory.getLogger(ProductionWorkflow.class);
		
	public ProductionWorkflow() {
		this(	new ArrayList<Producer<AnalysisItem>>(),
				new ArrayList<Processor<AnalysisItem>>(),
				new ArrayList<Consumer<AnalysisItem>>(),
				new AnalysisItem());
	}
	
	public ProductionWorkflow(AnalysisItem product) {
		this();
		this.product = product;
	}
	
	public ProductionWorkflow(ProductionPipeline<AnalysisItem,AnalysisItem> pipe) {
		this(	pipe.getProducers(),
				pipe.getProcessors(),
				pipe.getConsumers(),
				pipe.getProduct());
	}
	
	public ProductionWorkflow(List<Producer<AnalysisItem>> producers, List<Processor<AnalysisItem>> processors,
			List<Consumer<AnalysisItem>> consumers, AnalysisItem product) {
		super(producers, processors, consumers,product);
		
//		final AnalysisItem currentProduct = getProduct();
		
		config = new EnhancedConfig() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public EnhancedConfig getRequiredDefault() {
				EnhancedConfig requiredConfig = super.getRequiredDefault();
				requiredConfig.setPrefix("ProductionWorkflowConfig.");
				
				requiredConfig.put("metaData", new MetaData());
//				AnalysisItem currentProduct = ProductionWorkflow.super.getProduct();
//				AcquisitionConfig acqConfig = null;
//				if(currentProduct!=null) {
//					acqConfig = currentProduct.getAcquisitionConfig();
//				}
//				else {
//					acqConfig = new AcquisitionConfig();
//				}
//				requiredConfig.setProperty("acquisitionConfig", acqConfig);
				
				return requiredConfig;
			}
			
		};
		
		config.ensureCompliance();
		setExecutor(Global.INSTANCE.getExecutor());
	}

	@Override
	public EnhancedConfig getConfig() {
		return config;
	}

	@Override
	public void setConfig(EnhancedConfig config) {
		this.config = config;
		// ensure compliance? Probably not!
	}

	@Override
	public AnalysisItem getProduct() {
		AnalysisItem product = super.getProduct();
		if(product==null) {
			setProduct(new AnalysisItem());
		}
		return super.getProduct();
	}

	@Override
	protected void initComponents() {
		// TODO: sloppy fix of product init problem
		Producer<AnalysisItem> producer = getProducers().get(0);
		
		try {
			Method initProduct = producer.getClass().getMethod("initProduct", AnalysisItem.class);
			initProduct.invoke(producer, getProduct());
		}
		catch (Exception e) {
//			logger.error(e.getMessage(), e);
//			Utils.errorIJgeneric(e);
		}
		
		
//		getProduct().setAcquisitionConfig(
//				getConfig().<AcquisitionConfig>gett("acquisitionConfig"));
		
		super.initComponents();
	}

	@Override
	protected Handler<AnalysisItem> initHandler(
			List<Processor<AnalysisItem>> processors) {
		handler = new ReportingHandler(processors);
		return handler;
	}
	
	public ReportingHandler getHandler() {
		return handler;
	}

}
