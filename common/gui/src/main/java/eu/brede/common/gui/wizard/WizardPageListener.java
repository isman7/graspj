package eu.brede.common.gui.wizard;

import java.util.EventListener;

public interface WizardPageListener extends EventListener {
	public abstract void pageRendered(WizardPageRenderEvent e);
}
