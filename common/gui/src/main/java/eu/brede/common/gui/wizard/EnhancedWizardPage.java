package eu.brede.common.gui.wizard;

import java.util.List;

import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;

public class EnhancedWizardPage extends WizardPage {

	 /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	 
	public EnhancedWizardPage(String title, String description) {
			super(title, description);
	}
	 
    public void addWizardPageListener(WizardPageListener listener) {
        listenerList.add(WizardPageListener.class, listener);
    }

    public void removeWizardPageListener(WizardPageListener listener) {
        listenerList.remove(WizardPageListener.class, listener);
    }

    public WizardPageListener[] getWizardPageListeners() {
        return listenerList.getListeners(WizardPageListener.class);
    }

    protected void fireWizardPageRenderUpdate(WizardPageRenderEvent e) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==WizardPageListener.class) {
                ((WizardPageListener)listeners[i+1]).pageRendered(e);
            }
        }
    }

	@Override
	public void rendering(List<WizardPage> path, WizardSettings settings) {
		super.rendering(path, settings);
		fireWizardPageRenderUpdate(new WizardPageRenderEvent(this, path, settings));
	}
	
}
