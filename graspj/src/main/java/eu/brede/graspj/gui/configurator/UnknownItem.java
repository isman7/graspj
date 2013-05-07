package eu.brede.graspj.gui.configurator;

import javax.swing.JComponent;
import javax.swing.JTextField;

public class UnknownItem implements ConfigFormItem<Object> {
	
	JTextField field;
	Object value;
	
	public UnknownItem(Object value) {
		field = new JTextField();
		field.setEditable(false);
		field.setEnabled(false);
		if(value!=null) {
			field.setText(value.toString());
		}
		else {
			field.setText("null");
		}
		this.value = value;
	}
	
	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public void setLabelText(String labelStr) {
//		label.setText(labelStr);
	}

	@Override
	public void setToolTipText(String text) {
		field.setToolTipText(text);
	}

	@Override
	public JComponent getGUI() {
		return field;
	}

	@Override
	public void setEnabled(boolean enabled) {
//		field.setEnabled(enabled);
	}

	@Override
	public void setValue(Object value) {
		if(value!=null) {
			field.setText(value.toString());
		}
		else {
			field.setText("null");
		}
	}
	

}
