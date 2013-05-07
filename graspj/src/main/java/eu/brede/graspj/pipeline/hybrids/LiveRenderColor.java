package eu.brede.graspj.pipeline.hybrids;

import eu.brede.graspj.datatypes.AnalysisItem.CopyInstructions;
import eu.brede.graspj.datatypes.ObjectChoice;
import eu.brede.graspj.datatypes.Option;
import eu.brede.graspj.pipeline.consumers.LiveRenderer;
import eu.brede.graspj.pipeline.processors.ProductMerger;
import eu.brede.graspj.pipeline.processors.filterer.SpotFrameFilterer;
import eu.brede.graspj.pipeline.processors.renderer.Gaussian2D;
import eu.brede.graspj.pipeline.processors.renderer.SpotRenderer;

public class LiveRenderColor extends ContinousWorkflowOnCopy {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
//	private LiveRenderer liveRenderer;

	public LiveRenderColor() {
		this(1,Gaussian2D.class);
	}
	
	public LiveRenderColor(int color, Class<? extends SpotRenderer> rendererClass) {
		super();
		
		CopyInstructions ciProduct = getConfig().gett("copyInstructionsProduct");
		CopyInstructions ciItem = getConfig().gett("copyInstructionsItem");
		
		ciProduct.setSelected(true);
		ciProduct.<Option>gett("newSpotCollection").setSelected(true);
		ciProduct.<Option>gett("newMask").setSelected(true);
		
		ciItem.setSelected(true);
		Option itemNewSpotCollection = ciItem.<Option>gett("newSpotCollection");
		itemNewSpotCollection.setSelected(true);
		itemNewSpotCollection.put("copyOrgData", true);
		
		getConfig().getMetaData().setName(String.valueOf(color));
		
		SpotFrameFilterer filterer = new SpotFrameFilterer();
		
		filterer.getConfig().put("applyMask", true);
		
		Option filterByColorOption = filterer.getConfig().gett("filterByColor");
		Option filterByTypeOption = filterer.getConfig().gett("filterByType");
		
		filterByColorOption.setSelected(true);
		filterByColorOption.put("color", color);
		
		filterByTypeOption.setSelected(true);
		
		this.getProcessors().add(filterer);
//		this.getProcessors().add(new FilterMerger());
		this.getProcessors().add(new ProductMerger());
		
		LiveRenderer liveRenderer = new LiveRenderer();// {
//
//			/**
//			 * 
//			 */
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			protected void use(AnalysisItem item) throws InterruptedException {
//				System.out.println("----------------" + item.getSpots().getSpotCount());
//				setProduct(item);
//				super.use(item);
//			}
//			
//			
//		
//		};
		
//		Option pushActions = liveRenderer.getConfig().gett("pushActions");
//		pushActions.put("groupNames", new GroupNames("live"));
//		pushActions.setSelected(true);
//
//		Option receiveActions = liveRenderer.getConfig().gett("receiveActions");
//		receiveActions.put("groupNames", new GroupNames("live"));
//		receiveActions.setSelected(true);
		
		liveRenderer.getConfig().getMetaData().setName("Channel " + color);
		
		ObjectChoice<SpotRenderer> rendererChoice = 
				liveRenderer.getConfig().gett("renderer");
		for(SpotRenderer renderer : rendererChoice.getChoices()) {
			Option applyMask = renderer.getConfig().gett("applyMask");
			applyMask.setSelected(false);
		}
		rendererChoice.setChosenByClass(rendererClass);
//		getConfig().put("LiveRendererConfig", liveRenderer.getConfig());
		
		
		
		this.getConsumers().add(liveRenderer);
	}

	@Override
	public Integer call() throws Exception {
//		liveRenderer.setController(this.getController());
//		liveRenderer.setStopCondition(stopCondition);
//		this.setQueueOut(liveRenderer.getQueueIn());
//		Global.INSTANCE.getExecutor().submit(liveRenderer);
		return super.call();
	}
	
	
}
