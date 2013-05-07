package eu.brede.graspj.gui.configurator;

import javax.swing.JCheckBox;
import javax.swing.JComponent;

public class CheckBoxItem implements ConfigFormItem<Boolean> {
	
	JCheckBox checkBox;
	
	public CheckBoxItem(Boolean value) {
		checkBox = new JCheckBox();
		checkBox.setSelected(value);	
		checkBox.setText(null);
	}
	
	@Override
	public Boolean getValue() {
		return checkBox.isSelected();
	}

	@Override
	public void setLabelText(String labelStr) {
//		checkBox.setText(labelStr);
	}

	@Override
	public void setToolTipText(String text) {
		checkBox.setToolTipText(text);
	}

	@Override
	public JComponent getGUI() {
		return checkBox;
	}

	@Override
	public void setEnabled(boolean enabled) {
		checkBox.setEnabled(enabled);
	}

	@Override
	public void setValue(Object value) {
		if(value.equals(true)) {
			checkBox.setSelected(true);
		}
		else {
			checkBox.setSelected(false);
		}
	}
	

}
