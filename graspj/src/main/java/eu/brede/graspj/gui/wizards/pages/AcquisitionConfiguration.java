package eu.brede.graspj.gui.wizards.pages;

import ij.ImagePlus;

import java.util.LinkedHashMap;
import java.util.List;

import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;
import org.jdesktop.swingx.VerticalLayout;

import eu.benito.graspj.configs.daostorm.DAOConfig;
import eu.brede.common.config.EnhancedConfig;
import eu.brede.common.util.GUITools;
import eu.brede.graspj.configs.acquisition.AcquisitionConfig;
import eu.brede.graspj.configs.finding.FindConfig;
import eu.brede.graspj.configs.fit.FitConfig;
import eu.brede.graspj.configs.fit.MLE3DConfig;
import eu.brede.graspj.datatypes.ObjectChoice;
import eu.brede.graspj.datatypes.cycle.Cycle;
import eu.brede.graspj.gui.configurator.ConfigForm;
import eu.brede.graspj.gui.wizards.AutoDetect;
import eu.brede.graspj.pipeline.producers.ProductionEngine;

public class AcquisitionConfiguration extends WizardPage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ConfigForm cfAcqConfig;
	private ConfigForm cfFindConfig;
	private ConfigForm cfFitConfig;
	private ConfigForm cfDaoConfig;

	public AcquisitionConfiguration() {
		super("Acquisition and Finding Parameters", "Choose Acquisition and Finding Parameters");

		this.setLayout(new VerticalLayout());

		cfAcqConfig = new ConfigForm();
		cfAcqConfig.setName("acqConfig");
		
		cfFindConfig = new ConfigForm();
		cfFindConfig.setName("findConfig");
		
		cfFitConfig = new ConfigForm();
		cfFitConfig.setName("fitConfig");
		
		cfDaoConfig = new ConfigForm();
		cfDaoConfig.setName("daoConfig");
		
		
		this.add(cfAcqConfig);
		this.add(cfFindConfig);
		this.add(cfFitConfig);
		this.add(cfDaoConfig);
	}

	@Override
	public void rendering(List<WizardPage> path, WizardSettings settings) {
		
		AcquisitionConfig acqConfig = new AcquisitionConfig();
		
		EnhancedConfig displayConfig = (EnhancedConfig) settings
				.get("displayConfig");
		
		ObjectChoice<String> dimensionality = displayConfig
				.gett("dimensionality");
		switch (dimensionality.getChosen()) {
		case "2D DAOSTORM":
			acqConfig.put("framesPerPackage", 1);
			break;
		}
		
		FindConfig findConfig = new FindConfig();
		
		if((boolean) settings.get("framesAvailable")) {
//			IJ.log("frames are available");
			EnhancedConfig analysisTypeConfig = (EnhancedConfig) settings.get("displayConfig");
			
			@SuppressWarnings("unchecked")
			ObjectChoice<ProductionEngine> srcChoice = (ObjectChoice<ProductionEngine>) settings
			.get("srcChoice");
			
			ProductionEngine engine = srcChoice.getChosen();
			acqConfig.setDimensions(engine.getImageDim());
			
			// double is (mean-min) for each imp
			LinkedHashMap<ImagePlus, Double> map = new LinkedHashMap<>();
			
			int numFrames = Math.min(50, engine.framesAvailable());
//			IJ.log("numFrames: " + numFrames);
			if(numFrames >= 10) {				
				for (int i = 0; i < numFrames; i++) {
//					IJ.log("i: " + i);
					ImagePlus imp = engine.nextImage();
					Double mean = imp.getStatistics().mean;
//				Double max = imp.getStatistics().max;
//				Double min = imp.getStatistics().min;
					map.put(imp, mean);
//			imp.show();
				}
				// engine needs reset to start from beginning when analysis starts
				engine.reset();
				
				int numColors = analysisTypeConfig.gett("numColors");
				
				int maxNcycle = 5;
				Cycle cycle = AutoDetect.detectCycle(map, numColors, maxNcycle);
				acqConfig.put("frameCycle", cycle);
				
				//ObjectChoice<ProductionEngine> choosenIn = (ObjectChoice<ProductionEngine>) settings.get("srcChoice");
				
				//System.out.println('.' + choosenIn.getChoices().toString() + '.');
						
				int maxNthreshold = 5;
				int threshold = AutoDetect.detectThreshold(map.keySet(), cycle,
						maxNthreshold);
				
				
				findConfig.put("threshold", threshold);
			}
		}
		
		switch (dimensionality.getChosen()) {
		case "2D DAOSTORM":
			cfDaoConfig.setConfig(new DAOConfig());
			cfFitConfig.setConfig(new FitConfig());
			break;
		case "2D":
			cfFitConfig.setConfig(new FitConfig());
			this.remove(cfDaoConfig);
			break;
		case "3Dastigmatism":
			cfFitConfig.setConfig(new MLE3DConfig());
			this.remove(cfDaoConfig);
			break;
		}
		
		
		cfAcqConfig.setConfig(acqConfig);
		cfFindConfig.setConfig(findConfig);
		
		GUITools.parentPack(this);
		
//		config.put("findConfig", findConfig);
//		config.put("acqConfig", acqConfig);

		super.rendering(path, settings);
	}

	// @Override
	// public void updateSettings(WizardSettings settings) {
	// super.updateSettings(settings);
	// settings.put("colorChoice", srcChoice);
	// }

}
