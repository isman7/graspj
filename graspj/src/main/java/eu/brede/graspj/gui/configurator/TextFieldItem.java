package eu.brede.graspj.gui.configurator;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class TextFieldItem implements ConfigFormItem<String> {
	
	JTextField field;
	
	public TextFieldItem(String text) {
		field = new JTextField();
		addFocusGainSelectListener();
		field.setText(text);
	}
	
	private void addFocusGainSelectListener() {
		field.addFocusListener(new FocusAdapter() {
				public void focusGained(FocusEvent e) {
					SwingUtilities.invokeLater( new Runnable() {
	
						@Override
						public void run() {
							field.selectAll();
						}
					});
				}
			});
	}
	
	@Override
	public String getValue() {
		return field.getText();
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
		field.setEnabled(enabled);
	}

	@Override
	public void setValue(Object value) {
		// TODO better cast to String?
		field.setText(value.toString());
	}
	

}
