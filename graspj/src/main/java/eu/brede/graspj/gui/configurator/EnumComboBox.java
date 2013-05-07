package eu.brede.graspj.gui.configurator;

import javax.swing.JComboBox;
import javax.swing.JComponent;

public class EnumComboBox implements ConfigFormItem<Object> {
	
	JComboBox comboBox;
	public EnumComboBox(Enum<?> value) {
		comboBox = new JComboBox(value.getClass().getEnumConstants());
		comboBox.setSelectedItem(value);
	}
	
	@Override
	public Object getValue() {
		return comboBox.getSelectedItem();
	}

	@Override
	public void setLabelText(String labelStr) {
		//comboBox.setText(labelStr);
	}

	@Override
	public void setToolTipText(String text) {
		comboBox.setToolTipText(text);
	}

	@Override
	public JComponent getGUI() {
		return comboBox;
	}

	@Override
	public void setEnabled(boolean enabled) {
		comboBox.setEnabled(enabled);
	}

	@Override
	public void setValue(Object value) {
		comboBox.setSelectedItem(value);
	}
	

}
