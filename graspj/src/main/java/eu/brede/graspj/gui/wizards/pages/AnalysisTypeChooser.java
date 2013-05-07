package eu.brede.graspj.gui.wizards.pages;

import java.util.List;

import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;
import org.jdesktop.swingx.VerticalLayout;

import eu.brede.common.config.EnhancedConfig;
import eu.brede.graspj.configs.Global;
import eu.brede.graspj.datatypes.ObjectChoice;
import eu.brede.graspj.datatypes.Option;
import eu.brede.graspj.gui.configurator.ConfigForm;

public class AnalysisTypeChooser extends WizardPage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	boolean framesAvailable = false;

	private ConfigForm configForm;

	private EnhancedConfig config;

	public AnalysisTypeChooser() {
		super("Display Settings", "Choose Display Settings");
		
		
		this.setLayout(new VerticalLayout());
		
		configForm = new ConfigForm();
		configForm.setChildCollapsed(false);
		configForm.setLabelText(Global.getStringfromBundle("ConfigLabels", "wizard.AnalysisTypeChooser"));
		configForm.setName("displayConfig");
		
		this.add(configForm);

	}

	@Override
	public void rendering(List<WizardPage> path, WizardSettings settings) {
		framesAvailable = (boolean) settings.get("framesAvailable");
		initConfig();
		configForm.setConfig(config);
		super.rendering(path, settings);
	}
	
	private void initConfig() {
		config = new EnhancedConfig();
		config.setPrefix("wizard.AnalysisTypeChooser.");
		
		
		ObjectChoice<String> dimChoice = new ObjectChoice<>("2D","3Dastigmatism");
		dimChoice.setChosen("2D");
		
		config.put("dimensionality", dimChoice);
		
		if(framesAvailable) {
			config.put("numColors", 1);
		}
		else {			
//			config.put("frameCycle", new Cycle());
		}
		
		
		config.put("renderAllChannelImage", true);
		
		Option renderIndividualChannels = new Option();
		renderIndividualChannels.put("channelList", "1,2");
		config.put("renderIndividualChannels", renderIndividualChannels);
		
		
		
//		configForm.setConfig(config);
		
	}
	
	

}
