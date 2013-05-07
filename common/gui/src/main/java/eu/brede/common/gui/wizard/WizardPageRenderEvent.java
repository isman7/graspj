package eu.brede.common.gui.wizard;

import java.util.List;

import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;

public class WizardPageRenderEvent extends WizardPageEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final List<WizardPage> path;
	private final WizardSettings settings;
	

	public WizardPageRenderEvent(Object source, List<WizardPage> path, WizardSettings settings) {
		super(source);
		this.path = path;
		this.settings = settings;
	}


	public List<WizardPage> getPath() {
		return path;
	}


	public WizardSettings getSettings() {
		return settings;
	}

}
