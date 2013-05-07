package eu.brede.common.gui.wizard;

import java.util.Arrays;
import java.util.List;

import org.ciscavate.cjwizard.PageFactory;
import org.ciscavate.cjwizard.WizardController;
import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;

public class LinearPageFactory implements PageFactory {
	
	List<WizardPage> outline;
	WizardController wizardController;
	
	public LinearPageFactory(List<WizardPage> outline) {
		super();
		this.outline = outline;
		for(WizardPage page : outline) {
			if(page instanceof EnhancedWizardPage) {
				((EnhancedWizardPage) page).addWizardPageListener(new WizardPageAdapter() {

					@Override
					public void pageRendered(WizardPageRenderEvent e) {
						super.pageRendered(e);
						if(e.getPath().size()>=LinearPageFactory.this.outline.size()-1) {
							
						}
					}
					
				});
			}
		}
	}

	public LinearPageFactory(WizardPage... outline) {
		this(Arrays.asList(outline));
	}
	
	@Override
	public WizardPage createPage(List<WizardPage> path, WizardSettings settings) {
		if(wizardController != null) {
			if(path.size()>=outline.size()-1) {
				wizardController.setFinishEnabled(true);
				wizardController.setNextEnabled(false);
			}
			else {
				wizardController.setFinishEnabled(false);
				wizardController.setNextEnabled(true);
			}
			
			if(path.size()==0) {
				wizardController.setPrevEnabled(false);
			}
			else {
				wizardController.setPrevEnabled(true);
			}
		}
		return outline.get(path.size());
	}

	public WizardController getWizardController() {
		return wizardController;
	}

	public void setWizardController(WizardController wizardController) {
		this.wizardController = wizardController;
	}
	

}
