package eu.brede.graspj.gui.wizards.pages;


import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;

import eu.brede.common.config.EnhancedConfig;
import eu.brede.graspj.configs.Global;
import eu.brede.graspj.datatypes.ObjectChoice;
import eu.brede.graspj.datatypes.Option;
import eu.brede.graspj.gui.configurator.ConfigForm;

public class OptionalFeaturesChooser extends WizardPage {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public OptionalFeaturesChooser() {
		super("Optional features", "Choose optional features");
		
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
		
		// DAOSTORM added to the Workflow Wizard. 
		/*Option daoOption = new Option();
		config.put("daoStorm", daoOption);*/
		
		
		ConfigForm configForm = new ConfigForm(config);
		configForm.setLabelText(Global.getStringfromBundle("ConfigLabels",
				"wizard.OptionalFeaturesChooser"));
		configForm.setName("optionalFeatures");
		this.add(configForm);
//		System.out.println(_namedComponents);
	}

	@Override
	public void updateSettings(WizardSettings settings) {
		// TODO Auto-generated method stub
		super.updateSettings(settings);
	}
	
	
	
}
