package eu.brede.graspj.gui.configurator;

import javax.swing.JComponent;

public class ComponentItem implements ConfigFormItem<JComponent> {
	
	JComponent component;
	
	public ComponentItem(JComponent component) {
		this.component = component;
	}
	
	@Override
	public JComponent getValue() {
		return component;
	}

	@Override
	public void setLabelText(String labelStr) {
//		label.setText(labelStr);
	}

	@Override
	public void setToolTipText(String text) {
		component.setToolTipText(text);
	}

	@Override
	public JComponent getGUI() {
		return component;
	}

	@Override
	public void setEnabled(boolean enabled) {
		component.setEnabled(enabled);
	}

	@Override
	public void setValue(Object value) {
		this.component = (JComponent) value;
	}
	

}
