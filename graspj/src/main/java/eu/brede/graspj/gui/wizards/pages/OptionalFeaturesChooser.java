package eu.brede.graspj.gui.wizards.pages;


import java.util.LinkedHashMap;
import java.util.List;

import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;
import org.jdesktop.swingx.VerticalLayout;

import eu.benito.graspj.configs.daostorm.DAOConfig;
import eu.benito.graspj.configs.daostorm.DAOConfigOptional;
import eu.brede.common.config.EnhancedConfig;
import eu.brede.common.util.GUITools;
import eu.brede.graspj.configs.Global;
import eu.brede.graspj.configs.acquisition.AcquisitionConfig;
import eu.brede.graspj.configs.finding.FindConfig;
import eu.brede.graspj.configs.fit.FitConfig;
import eu.brede.graspj.configs.fit.MLE3DConfig;
import eu.brede.graspj.datatypes.ObjectChoice;
import eu.brede.graspj.datatypes.Option;
import eu.brede.graspj.datatypes.cycle.Cycle;
import eu.brede.graspj.gui.configurator.ConfigForm;
import eu.brede.graspj.gui.wizards.AutoDetect;
import eu.brede.graspj.pipeline.producers.ProductionEngine;
import ij.ImagePlus;

public class OptionalFeaturesChooser extends WizardPage {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ConfigForm cfDaoConfigOpt;
	
	
	public OptionalFeaturesChooser() {
		super("Optional features", "Choose optional features");
		
		this.setLayout(new VerticalLayout());
		EnhancedConfig config = new EnhancedConfig();
		config.setPrefix("wizard.OptionalFeaturesChooser.");
		
		Option tgOption = new Option();
		config.put("trailGeneration", tgOption);
		
//		Choice dcChoice = new Choice();
//		
//		
//		dcChoice.getMap().put("intrinsic", new EnhancedConfig());
//		dcChoice.getMap().put("beads", new EnhancedConfig());
//		
//		dcChoice.setChosen("intrinsic");
		
//		ObjectChoice<String> dcChoice = new ObjectChoice<>("intrinsic", "beads");
		ObjectChoice<String> dcChoice = new ObjectChoice<>("intrinsic");
		dcChoice.setChosen("intrinsic");
		
		Option dcOption = new Option();
		dcOption.setPrefix("wizard.OptionalFeaturesChooser.dcOption.");
		dcOption.put("dcMethod", dcChoice);
		
		
		
		config.put("driftCorrection", dcOption);
		
		
		
		ConfigForm configForm = new ConfigForm(config);
		configForm.setLabelText(Global.getStringfromBundle("ConfigLabels",
				"wizard.OptionalFeaturesChooser"));
		configForm.setName("optionalFeatures");
		this.add(configForm);
//		System.out.println(_namedComponents);
		
		cfDaoConfigOpt = new ConfigForm();
		cfDaoConfigOpt.setName("daoConfigOpt");
		this.add(cfDaoConfigOpt);
		
		
		
	}
	
	
	@Override
	public void rendering(List<WizardPage> path, WizardSettings settings) {
		
		EnhancedConfig displayConfig = (EnhancedConfig) settings
				.get("displayConfig");
		
		ObjectChoice<String> dimensionality = displayConfig
				.gett("dimensionality");
		
		FindConfig findConfig = (FindConfig) settings.get("findConfig");
		DAOConfig DAOConfig = (DAOConfig) settings.get("daoConfig");
		
		switch (dimensionality.getChosen()) {
		case "2D DAOSTORM":
			cfDaoConfigOpt.setConfig(new DAOConfigOptional(DAOConfig, findConfig));
			break;
		}
		
			
		
		GUITools.parentPack(this);
		
//		config.put("findConfig", findConfig);
//		config.put("acqConfig", acqConfig);

		super.rendering(path, settings);
	}

	@Override
	public void updateSettings(WizardSettings settings) {
		// TODO Auto-generated method stub
		super.updateSettings(settings);
	}
	
	
	
}
