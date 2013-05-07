package eu.brede.graspj.gui.configurator;

import javax.swing.JComponent;

public interface ConfigFormItem<T> {
	public T getValue();
	public JComponent getGUI();
	public void setLabelText(String labelText);
	public void setToolTipText(String toolTipText);
	public void setEnabled(boolean enabled);
	public void setValue(Object value);
}
