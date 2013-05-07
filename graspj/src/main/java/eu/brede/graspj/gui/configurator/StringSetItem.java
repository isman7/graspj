package eu.brede.graspj.gui.configurator;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Arrays;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;

public class StringSetItem<T extends Set<String>> implements ConfigFormItem<T> {
	
	JTextField field;
	String separator;
	T stringSet;
	
	public StringSetItem(T stringSet, String separator) {
		this.stringSet = stringSet;
		field = new JTextField();
		this.separator = separator;
		addFocusGainSelectListener();
		field.setText(StringUtils.join(stringSet, separator));
	}
	
	public StringSetItem(T stringSet) {
		this(stringSet,",");
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
	public T getValue() {
		stringSet.clear();
		stringSet.addAll(Arrays.asList(field.getText().split(separator)));
		return stringSet;
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
		throw new Error("missing implemantation");
	}
	
	
	

}
