package eu.brede.graspj.gui.wizards.pages;

import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;

import eu.brede.graspj.configs.Global;
import eu.brede.graspj.datatypes.ObjectChoice;
import eu.brede.graspj.gui.configurator.ObjectChoiceForm;
import eu.brede.graspj.pipeline.producers.LiveProducer;
import eu.brede.graspj.pipeline.producers.ProductionEngine;

public class MultiSourceChooser extends WizardPage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MultiSourceChooser() {
		super("Data source", "Choose Data Source");

		ObjectChoiceForm<ProductionEngine> choiceForm = new ObjectChoiceForm<>(
				LiveProducer.getEngineChoice());

		choiceForm.setName("srcChoice");
		choiceForm.setLabelText(Global.getStringfromBundle("ConfigLabels",
				"wizard.MultiSourceChooser"));

		this.add(choiceForm);
	}

	@Override
	public void updateSettings(WizardSettings settings) {
		super.updateSettings(settings);
		@SuppressWarnings("unchecked")
		ObjectChoice<ProductionEngine> srcChoice = (ObjectChoice<ProductionEngine>) settings
				.get("srcChoice");
		
		boolean framesAvailable=false;
		if(srcChoice.getChosen().framesAvailable() > 0) {
			framesAvailable=true;
		}
		
		settings.put("framesAvailable",framesAvailable);
	}

}
