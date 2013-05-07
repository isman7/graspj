package eu.brede.graspj.configs;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;

import com.jogamp.common.util.VersionNumber;
import com.jogamp.opencl.CLVersion;

import eu.brede.common.config.ESOMap;
import eu.brede.common.pipeline.Processor;
import eu.brede.common.pipeline.consumers.Consumer;
import eu.brede.common.pipeline.hybrids.ConsumptionPipeline;
import eu.brede.common.pipeline.hybrids.ProductionPipeline;
import eu.brede.common.pipeline.producers.Producer;
import eu.brede.common.util.AssertionTools;
import eu.brede.common.util.StringTools;
import eu.brede.graspj.datatypes.AnalysisItem;
import eu.brede.graspj.datatypes.DefocusingCurve;
import eu.brede.graspj.datatypes.ObjectChoice;
import eu.brede.graspj.datatypes.ProductionWorkflow;
import eu.brede.graspj.datatypes.Workflow;
import eu.brede.graspj.datatypes.WorkflowMap;
import eu.brede.graspj.opencl.programs.CLProgramGaussian2DRenderer;
import eu.brede.graspj.opencl.programs.CLProgramGaussianCC3DRenderer;
import eu.brede.graspj.opencl.programs.CLProgramMLE2DFitter;
import eu.brede.graspj.opencl.programs.CLProgramMLE3DFitter;
import eu.brede.graspj.opencl.programs.CLProgramMLEflex2DFitter;
import eu.brede.graspj.pipeline.consumers.Live3DPreview;
import eu.brede.graspj.pipeline.consumers.LiveRenderer;
import eu.brede.graspj.pipeline.hybrids.ContinousWorkflowOnCopy;
import eu.brede.graspj.pipeline.hybrids.InjectionProducer;
import eu.brede.graspj.pipeline.hybrids.LiveRenderColor;
import eu.brede.graspj.pipeline.hybrids.PipeOnShallowCopy;
import eu.brede.graspj.pipeline.hybrids.WorkflowOnCopy;
import eu.brede.graspj.pipeline.processors.FilterMerger;
import eu.brede.graspj.pipeline.processors.ProductMerger;
import eu.brede.graspj.pipeline.processors.driftcorrector.PackageDriftCorrector;
import eu.brede.graspj.pipeline.processors.filterer.SpotFilterer;
import eu.brede.graspj.pipeline.processors.filterer.SpotFitFilterer;
import eu.brede.graspj.pipeline.processors.filterer.SpotFrameFilterer;
import eu.brede.graspj.pipeline.processors.finder.SpotFinderCL;
import eu.brede.graspj.pipeline.processors.finder.SpotFinderJava;
import eu.brede.graspj.pipeline.processors.fitter.MLEFitter2D;
import eu.brede.graspj.pipeline.processors.fitter.MLEFitter3D;
import eu.brede.graspj.pipeline.processors.fitter.MLEFitterFlex2D;
import eu.brede.graspj.pipeline.processors.renderer.Gaussian2D;
import eu.brede.graspj.pipeline.processors.renderer.GaussianColorCoded3D;
import eu.brede.graspj.pipeline.processors.renderer.GaussianSliced3D;
import eu.brede.graspj.pipeline.processors.renderer.SpotRenderer;
import eu.brede.graspj.pipeline.processors.trailgenerator.TrailGenerator;
import eu.brede.graspj.pipeline.producers.LiveProducer;
import eu.brede.graspj.pipeline.producers.SplitExistingProducer;
import eu.brede.graspj.utils.ReportingExecutorServiceGJ;
import eu.brede.graspj.utils.Utils;

public enum Global {
	INSTANCE;
	
	private ExecutorService executor = new ReportingExecutorServiceGJ();

	public static VersionNumber GraspJ_VERSION = new VersionNumber(1, 1, 3);
	public static double MIN_JAVA_VERSION = 1.7;
	public static CLVersion MIN_CL_VERSION = CLVersion.CL_1_1;
	
	public ExecutorService getExecutor() {
		return executor;
	}
	
	public static WorkflowMap getBuiltinWorkflows(AnalysisItem item) {
		
		ProductionWorkflow itemWf = null;
		if(item==null) {
			itemWf = new ProductionWorkflow();
		}
		else {			
			itemWf = item.getNotes().get("workflow", new ProductionWorkflow());
		}
		
		WorkflowMap map = new WorkflowMap();
		ProductionWorkflow wf;
		
		wf = new ProductionWorkflow();
		wf.getProducers().add(new SplitExistingProducer());
		wf.getProcessors().add(new ProductMerger());
		map.put(Global.getStringfromBundle("MenuLabels",
				"Workflows.basicTemplate"), wf);

		wf = new ProductionWorkflow();
		wf.getProducers().add(new SplitExistingProducer());
		SpotFitFilterer fitFilterer = AssertionTools.defaultIfNull(
				Utils.getComponentIfExistent(SpotFitFilterer.class,
						itemWf.getProcessors()), new SpotFitFilterer());
		fitFilterer.getConfig().ensureCompliance();
		ObjectChoice<String> combinationMode = fitFilterer.getConfig().gett(
				"combinationMode");
		combinationMode.setChosen("override");
		wf.getProcessors().add(fitFilterer);
		SpotFrameFilterer frameFilterer = AssertionTools.defaultIfNull(
				Utils.getComponentIfExistent(SpotFrameFilterer.class,
						itemWf.getProcessors()), new SpotFrameFilterer());
		wf.getProcessors().add(frameFilterer);
		wf.getProcessors().add(new ProductMerger());
		map.put(Global.getStringfromBundle("MenuLabels",
				"Workflows.overrideFitFilter"), wf);
		
		wf = new ProductionWorkflow();
		wf.getProducers().add(new SplitExistingProducer());
		
		SpotRenderer renderer = Utils.getComponentIfExistent(
				SpotRenderer.class, itemWf.getProcessors());
		if (renderer == null) {
			LiveRenderer liveRenderer = Utils.getComponentIfExistent(
					LiveRenderer.class, itemWf.getConsumers());
			if (liveRenderer != null) {
				renderer = liveRenderer.getConfig()
						.<ObjectChoice<SpotRenderer>> gett("renderer")
						.getChosen();
			} else {
				LiveRenderColor liveRenderColor = Utils.getComponentIfExistent(
						LiveRenderColor.class, itemWf.getConsumers());
				if (liveRenderColor != null) {
					liveRenderer = Utils.getComponentIfExistent(
							LiveRenderer.class, liveRenderColor.getConsumers());
					renderer = liveRenderer.getConfig()
							.<ObjectChoice<SpotRenderer>> gett("renderer")
							.getChosen();
				}
			}
			if (renderer == null) {
				renderer = new Gaussian2D();
			}
		}
		renderer.getConfig().put("showImage", true);
		
		wf.getProcessors().add(renderer);
		wf.getProcessors().add(new ProductMerger());
		map.put(Global.getStringfromBundle("MenuLabels",
				"Workflows.staticRendering"), wf);
		
		wf = new ProductionWorkflow();
		wf.getProducers().add(new SplitExistingProducer());
		wf.getProcessors().add(new ProductMerger());
		
		for(Consumer<AnalysisItem> consumer : itemWf.getConsumers()) {
			if((consumer instanceof LiveRenderer)
					|| (consumer instanceof LiveRenderColor)) {
				wf.getConsumers().add(consumer);
			}
		}
		
		map.put(Global.getStringfromBundle("MenuLabels",
				"Workflows.dynamicRendering"), wf);
		
		return map;
	}
	
	public static List<Class<?>> getPipeClasses() {
		List<Class<?>> classList = new ArrayList<Class<?>>(); 
		classList.add(SpotFilterer.class);
		classList.add(SpotFitFilterer.class);
		classList.add(SpotFrameFilterer.class);
		classList.add(PipeOnShallowCopy.class);
		classList.add(LiveRenderColor.class);
		classList.add(FilterMerger.class);
		classList.add(PackageDriftCorrector.class);
//		classList.add(SpotFitter.class);
		classList.add(MLEFitter2D.class);
		classList.add(MLEFitterFlex2D.class);
		classList.add(MLEFitter3D.class);
		classList.add(SpotRenderer.class);
		classList.add(Gaussian2D.class);
		classList.add(GaussianColorCoded3D.class);
		classList.add(GaussianSliced3D.class);
		classList.add(TrailGenerator.class);
		classList.add(ProductionPipeline.class);
		classList.add(ConsumptionPipeline.class);
		classList.add(LiveProducer.class);
		classList.add(SpotFinderCL.class);
		classList.add(SpotFinderJava.class);
		classList.add(ProductMerger.class);
		classList.add(LiveRenderer.class);
		classList.add(Live3DPreview.class);
		classList.add(SplitExistingProducer.class);
		classList.add(InjectionProducer.class);
		classList.add(WorkflowOnCopy.class);
		classList.add(ContinousWorkflowOnCopy.class);
		return classList;
	}
	
	public static Workflow getEmptyPipeline() {

		
		AnalysisItem templateItem = new AnalysisItem();
		// TODO configure templateItem
//		templateItem.setAcquisition(acqData);
		
		ArrayList<Producer<AnalysisItem>> producers = new ArrayList<Producer<AnalysisItem>>();
		ArrayList<Processor<AnalysisItem>> processors = new ArrayList<Processor<AnalysisItem>>();
		ArrayList<Consumer<AnalysisItem>> consumers = new ArrayList<Consumer<AnalysisItem>>();
		
//		consumers.add(new LiveRenderColor());
		
		ProductionWorkflow pipe = 
			new ProductionWorkflow(producers, processors, consumers, templateItem);
		return pipe;
	}
	
	public static ESOMap getCLProgramCodes() {
		ESOMap codes = new ESOMap(); 
//		long start = System.currentTimeMillis();
		for(Class<?> clazz : getCLProgramCodeClasses()) {
			try {
				codes.put(clazz.getSimpleName(), clazz.newInstance());
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
//		System.out.println("generating all codes took: " + (System.currentTimeMillis()-start) + "ms");
		return codes;
	}
	
	public static List<Class<?>> getCLProgramCodeClasses() {
		List<Class<?>> classList = new ArrayList<Class<?>>(); 
		classList.add(CLProgramMLE2DFitter.class);
		classList.add(CLProgramMLEflex2DFitter.class);
		classList.add(CLProgramMLE3DFitter.class);
		classList.add(CLProgramGaussian2DRenderer.class);
		classList.add(CLProgramGaussianCC3DRenderer.class);
		return classList;
	}
	
	public static DefocusingCurve dfcX() {
		DefocusingCurve dfc = new DefocusingCurve();
		dfc.put("s0", Float.valueOf(0.880539f));
		dfc.put("g", Float.valueOf(-1.441317f));
		dfc.put("d", Float.valueOf(2.491018f));
		dfc.put("A", Float.valueOf(-0.050582f));
		dfc.put("B", Float.valueOf(0.074131724f));
		return dfc;
	}
	
	public static DefocusingCurve dfcY() {
		DefocusingCurve dfc = new DefocusingCurve();
		dfc.put("s0", Float.valueOf(0.9218563f));
		dfc.put("g", Float.valueOf(0.81018f));
		dfc.put("d", Float.valueOf(2.482036f));
		dfc.put("A", Float.valueOf(0.240125f));
		dfc.put("B", Float.valueOf(0.106f));
		return dfc;
	}
	
	public static List<Color> getStdColors() {
		
		//TODO: choose better color set
		
		ArrayList<Color> list = new ArrayList<>();
		list.add(Color.BLACK);
		list.add(Color.GREEN);
		list.add(Color.RED);
		list.add(Color.YELLOW);
		list.add(Color.BLUE);
		list.add(Color.CYAN);
		list.add(Color.ORANGE);
		list.add(Color.MAGENTA);
		list.add(Color.GRAY);
		list.add(Color.WHITE);
		return list;
	}
	
	public static String getStringfromBundle(String bundleName, String key) {
		ResourceBundle bundle = getBundle(bundleName);
		return StringTools.getStringFromBundle(bundle, key);
	}
	
	public static String getStringfromBundle(String bundleName, String key, String defaultString) {
		ResourceBundle bundle = getBundle(bundleName);
		return StringTools.getStringFromBundle(bundle, key, defaultString);
	}
	
	public static ResourceBundle getBundle(String bundleName) {
		return ResourceBundle.getBundle(getResourceURI() + bundleName);
	}
	
	public static String getResourceURI() {
		return "properties/";
	}
}
