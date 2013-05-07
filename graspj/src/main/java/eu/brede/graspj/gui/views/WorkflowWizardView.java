package eu.brede.graspj.gui.views;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import org.ciscavate.cjwizard.WizardAdapter;
import org.ciscavate.cjwizard.WizardContainer;
import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;
import org.ciscavate.cjwizard.pagetemplates.PageTemplate;

import eu.brede.common.config.EnhancedConfig;
import eu.brede.common.gui.wizard.CustomPageTemplate;
import eu.brede.common.gui.wizard.LinearPageFactory;
import eu.brede.common.pipeline.Processor;
import eu.brede.common.pipeline.consumers.Consumer;
import eu.brede.common.pipeline.producers.Producer;
import eu.brede.graspj.configs.acquisition.AcquisitionConfig;
import eu.brede.graspj.configs.finding.FindConfig;
import eu.brede.graspj.configs.fit.FitConfig;
import eu.brede.graspj.configs.fit.MLE3DConfig;
import eu.brede.graspj.datatypes.AnalysisItem;
import eu.brede.graspj.datatypes.ObjectChoice;
import eu.brede.graspj.datatypes.Option;
import eu.brede.graspj.datatypes.ProductionWorkflow;
import eu.brede.graspj.datatypes.Workflow;
import eu.brede.graspj.gui.WorkflowEditor;
import eu.brede.graspj.gui.views.ViewManager.SwitchType;
import eu.brede.graspj.gui.wizards.pages.AcquisitionConfiguration;
import eu.brede.graspj.gui.wizards.pages.AnalysisTypeChooser;
import eu.brede.graspj.gui.wizards.pages.MultiSourceChooser;
import eu.brede.graspj.gui.wizards.pages.OptionalFeaturesChooser;
import eu.brede.graspj.pipeline.consumers.Live3DPreview;
import eu.brede.graspj.pipeline.consumers.LiveRenderer;
import eu.brede.graspj.pipeline.hybrids.LiveRenderColor;
import eu.brede.graspj.pipeline.processors.ProductMerger;
import eu.brede.graspj.pipeline.processors.driftcorrector.PackageDriftCorrector;
import eu.brede.graspj.pipeline.processors.filterer.SpotFitFilterer;
import eu.brede.graspj.pipeline.processors.filterer.SpotFrameFilterer;
import eu.brede.graspj.pipeline.processors.finder.SpotFinderJava;
import eu.brede.graspj.pipeline.processors.fitter.MLEFitter2D;
import eu.brede.graspj.pipeline.processors.fitter.MLEFitter3D;
import eu.brede.graspj.pipeline.processors.renderer.Gaussian2D;
import eu.brede.graspj.pipeline.processors.renderer.GaussianColorCoded3D;
import eu.brede.graspj.pipeline.processors.renderer.SpotRenderer;
import eu.brede.graspj.pipeline.processors.trailgenerator.TrailGenerator;
import eu.brede.graspj.pipeline.producers.LiveProducer;
import eu.brede.graspj.pipeline.producers.ProductionEngine;

public class WorkflowWizardView extends View {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private WizardContainer wc;

	public WorkflowWizardView() {
		LinearPageFactory pageFactory = new LinearPageFactory(
				new MultiSourceChooser(), new AnalysisTypeChooser(),
				new AcquisitionConfiguration(), new OptionalFeaturesChooser());
		PageTemplate template = new CustomPageTemplate();
		// template.setBorder(BorderFactory.createLineBorder(Color.PINK));
		// GUITools.makeParentPacking(template);
		wc = new WizardContainer(pageFactory, template);
		pageFactory.setWizardController(wc);
		wc.addWizardListener(new WizardAdapter() {

			// @Override
			// public void onPageChanged(WizardPage newPage, List<WizardPage>
			// path) {
			// // TODO Auto-generated method stub
			//
			// }

			@Override
			public void onFinished(List<WizardPage> path,
					WizardSettings settings) {
				// last pages updateSettings is not called when Finish button is
				// clicked!
				// therefore settings need to be updated manually
				path.get(path.size() - 1).updateSettings(settings);

				Workflow pipeline = createPipeline(settings);
				getManager().switchView(
						new WorkflowView(new WorkflowEditor(pipeline)),
						SwitchType.DISPOSE);
			}

			@Override
			public void onCanceled(List<WizardPage> path,
					WizardSettings settings) {
				getManager().closeCurrent();
			}
		});
		// wc.setBorder(BorderFactory.createLineBorder(Color.GREEN));
		Component[] components = wc.getComponents();
		wc.removeAll();
		wc.add(components[0], BorderLayout.WEST);
		wc.add(components[1], BorderLayout.SOUTH);
		add(wc);
	}

	private Workflow createPipeline(WizardSettings settings) {

		// AcquisitionData acqData = new AcquisitionData(new
		// AcquisitionConfig());
		// // acqData.getConfig().setProperty("file",
		// // new File("C:\\Users\\Norman\\Desktop\\movie_0_0.dax"));
		// acqData.getConfig().append(srcChoice.getMap().get(srcChoice.getChosen()));
		// acqData.getConfig().setProperty("numFrames", Integer.valueOf(512));
		//
		// AnalysisItem finalItem = new AnalysisItem();
		// // TODO configure finalItem
		// finalItem.setAcquisition(acqData);
		// finalItem.setSpots(new BufferSpotCollection((int)1e6,(int)1e4));
		//
		// AnalysisItem templateItem = new AnalysisItem();
		// // TODO configure templateItem
		// templateItem.setAcquisition(acqData);

		ArrayList<Producer<AnalysisItem>> producers = new ArrayList<Producer<AnalysisItem>>();

		ArrayList<Processor<AnalysisItem>> processors = new ArrayList<Processor<AnalysisItem>>();

		ArrayList<Consumer<AnalysisItem>> consumers = new ArrayList<Consumer<AnalysisItem>>();

		LiveProducer liveProducer = new LiveProducer();
		liveProducer.getConfig().put("acquisitionConfig",
				settings.get("acqConfig"));
		liveProducer.updateImageDim();
		producers.add(liveProducer);

		// ProductionEngine engine = null;

		// switch(srcChoice.getChosen()) {
		// case "useStack":
		//
		// break;
		// case "openRaw":
		//
		// break;
		// case "openLoci":
		// break;
		// }

		EnhancedConfig displayConfig = (EnhancedConfig) settings
				.get("displayConfig");

		@SuppressWarnings("unchecked")
		ObjectChoice<ProductionEngine> srcChoice = (ObjectChoice<ProductionEngine>) settings
				.get("srcChoice");

		liveProducer.getConfig().put("productionEngine", srcChoice);

		// Choice dimChoice = (Choice) settings.get("dimChoice");

		// processors.add(new SpotFinder());
		SpotFinderJava finder = new SpotFinderJava();
		finder.setConfig((FindConfig) settings.get("findConfig"));
		processors.add(finder);

		Class<? extends SpotRenderer> rendererClass = null;

		ObjectChoice<String> dimensionality = displayConfig
				.gett("dimensionality");
		
		
		switch (dimensionality.getChosen()) {
		case "2D":
			MLEFitter2D fitter = new MLEFitter2D();
			fitter.setConfig((FitConfig) settings.get("fitConfig"));
			processors.add(fitter);
			rendererClass = Gaussian2D.class;
			break;
		case "3Dastigmatism":
			MLEFitter3D fitter3D = new MLEFitter3D();
			fitter3D.setConfig((MLE3DConfig) settings.get("fitConfig"));
			processors.add(fitter3D);
			rendererClass = GaussianColorCoded3D.class;
			break;
		}

		processors.add(new SpotFitFilterer());

		SpotFrameFilterer spotFrameFilterer = new SpotFrameFilterer();
		spotFrameFilterer.getConfig().put("encodeColor", true);
		processors.add(spotFrameFilterer);

		EnhancedConfig optionalFeatures = (EnhancedConfig) settings
				.get("optionalFeatures");

		Option trailGeneration = optionalFeatures.gett("trailGeneration");
		if (trailGeneration.isSelected()) {
			processors.add(new TrailGenerator());
		}

		Option driftCorrection = optionalFeatures.gett("driftCorrection");
		if (driftCorrection.isSelected()) {
			// Choice dcMethod = driftCorrection.gett("dcMethod");
			// switch(dcMethod.getChosen()) {
			// case "intrinsic":
			// processors.add(new PackageDriftCorrector());
			// break;
			// case "beads":
			// break;
			// }
			ObjectChoice<String> dcMethod = driftCorrection.gett("dcMethod");
			switch (dcMethod.getChosen()) {
			case "intrinsic":
				processors.add(new PackageDriftCorrector());
				break;
			case "beads":
				break;
			}
		}

		processors.add(new ProductMerger());

		if (displayConfig.gett("renderAllChannelImage")) {
			LiveRenderer liveRenderer = new LiveRenderer();
			liveRenderer.getConfig().getMetaData().setName("All Channels");
			ObjectChoice<SpotRenderer> rendererChoice = liveRenderer
					.getConfig().gett("renderer");
			rendererChoice.setChosenByClass(rendererClass);
			consumers.add(liveRenderer);
		}

		Option renderIndividualChannels = displayConfig
				.gett("renderIndividualChannels");
		if (renderIndividualChannels.isSelected()) {
			String channelList = renderIndividualChannels
					.getString("channelList");
			String[] channels = channelList.split(",");
			for (String channel : channels) {
				// LiveRenderer liveRenderer = new LiveRenderer();
				// LiveRenderer liveRenderer = new
				// LiveRenderer(Integer.valueOf(channel));
				LiveRenderColor liveRenderer = new LiveRenderColor(
						Integer.valueOf(channel), rendererClass);
				// ObjectChoice<SpotRenderer> choice =
				// liveRenderer.getConfig().gett("renderer");
				// Option applyMask =
				// choice.getChosen().getConfig().gett("applyMask");
				// Option specificChannel = applyMask.gett("specificChannel");
				// specificChannel.setSelected(true);
				// specificChannel.setProperty("channelNr",
				// Integer.valueOf(channel));
				// liveRenderer.getConfig().getMetaData().setName("Channel " +
				// channel);
				consumers.add(liveRenderer);
				// consumers.add(new
				// LiveRenderColor(Integer.valueOf(channel),rendererClass));
			}
		}

		if (displayConfig.get("3dMulticolorPreview", false)) {
			consumers.add(new Live3DPreview());
		}

		// consumers.add(new LiveRenderColor());

		// TODO: auto detect Acqui data and modify AnalysisItem (below)

		ProductionWorkflow workflow = new ProductionWorkflow(producers,
				processors, consumers, new AnalysisItem());

		workflow.getProduct().setAcquisitionConfig(
				liveProducer.getConfig().<AcquisitionConfig> gett(
						"acquisitionConfig"));
		// return pipe;

		// AcquisitionConfig acqConfig =
		// workflow.getConfig().gett("acquisitionConfig");
		// workflow.getConfig()
		// acqConfig.put("frameCycle", displayConfig.<Cycle>gett("frameCycle"));

		return workflow;
	}

	@Override
	public void runCommand(String cmd) {
		// TODO Auto-generated method stub

	}

}
