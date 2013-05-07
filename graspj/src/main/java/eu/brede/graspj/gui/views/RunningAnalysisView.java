package eu.brede.graspj.gui.views;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import eu.brede.common.config.EnhancedConfig;
import eu.brede.graspj.configs.Global;
import eu.brede.graspj.datatypes.AnalysisItem;
import eu.brede.graspj.datatypes.ProductionWorkflow;

public class RunningAnalysisView extends ConfigView {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ProductionWorkflow workflow;

	public RunningAnalysisView(ProductionWorkflow workflow) {
		super();
		this.workflow = workflow;
		getViewMenu().addCmdMenuItem("pauseAnalysis");
		getViewMenu().addCmdMenuItem("resumeAnalysis");
		getViewMenu().addCmdMenuItem("stopAnalysis");
//		getViewMenu().addCmdMenuItem("pauseAnalysis");
//		getConfig().setPrefix();
//		configForm.setEnabled(false);
//		refresh();
		
	}
	
	@Override
	public void refresh() {
		EnhancedConfig config = getConfig();
		boolean wasNull = false;
		if(config==null) {
			wasNull = true;
			config = new EnhancedConfig(RunningAnalysisView.class.getSimpleName() + ".config.");
		}
		AnalysisItem product = workflow.getProduct();
		product.lock();
		int spotCount = 0;
		int positiveMaskCount = 0;
		int frameCount = 0;
		if(product!=null) {
			frameCount = product.getSpots().getFrameCount();
			spotCount = product.getSpots().getSpotCount();
			if(product.getMask()!=null) {
				positiveMaskCount = product.getMask().countPositives();
			}
		}
		
		config.put("frameCount", frameCount);
		config.put("spotCount", spotCount);
		config.put("positiveMaskCount", positiveMaskCount);
		product.unlock();
		config.put("handlingReport",workflow.getHandler().getReport());
		if(wasNull) {
			setConfig(config);
		}
		else {			
			super.refresh();
		}
//		setConfig(config);
		configForm.setEnabled(false);
	}

	public RunningAnalysisView start() {
		ExecutorService executor = Global.INSTANCE.getExecutor();
		final Future<Object> futureAnalysisItem = executor.submit(workflow);
		executor.submit(new Runnable() {
			
			@Override
			public void run() {
				
				try {
					AnalysisItem analysisItem = (AnalysisItem) futureAnalysisItem.get();
//					getManager().closeCurrent();
					getManager().switchView(new AnalysisItemView(analysisItem));
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ExecutionException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		return this;
	}

	@Override
	public void runCommand(String cmd) {
		switch(cmd) {
			case "pauseAnalysis":
				workflow.getController().pause();
				break;
			case "resumeAnalysis":
				workflow.getController().resume();
				break;
			case "stopAnalysis":
				workflow.getController().stop();
				workflow.getController().resume();
				break;	
		}
		super.runCommand(cmd);
	}
	
	
	
}
