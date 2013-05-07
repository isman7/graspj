package eu.brede.graspj.gui.configurator;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.Format;
import java.text.ParseException;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.SwingUtilities;

import org.apache.commons.beanutils.converters.NumberConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.brede.graspj.utils.Utils;

public class FormattedFieldItem implements ConfigFormItem<Object> {
	
	JFormattedTextField field;
	final static Logger logger = LoggerFactory.getLogger(FormattedFieldItem.class);
	private final Object value;
	
	public FormattedFieldItem(Object value) {
		this.value = value;
		field = new JFormattedTextField(value);
		addFocusGainSelectListener();
		field.setValue(value);
	}
	
	public FormattedFieldItem(Object value, Format format) {
		this.value = value;
		field = new JFormattedTextField(format);
		addFocusGainSelectListener();
		field.setValue(value);
	}
	
	public FormattedFieldItem(Object value, AbstractFormatter formatter) {
		this.value = value;
		field = new JFormattedTextField(formatter);
		addFocusGainSelectListener();
		field.setValue(value);
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
	public Object getValue() {
		try {
			field.commitEdit();
		}
		catch (ParseException e) {
			logger.error(e.getMessage(), e);
			Utils.showError(e);
			e.printStackTrace();
		}
		
		if(value instanceof Number) {			
			NumberConverter nc = new NumberConverter(true, field.getValue()) {
				
				@Override
				protected Class<?> getDefaultType() {
					return value.getClass();
				}
			};
			
			return value.getClass().cast(nc.convert(value.getClass(), field.getValue()));
		}
		else {
			return field.getValue();
		}
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
		field.setValue(value);
	}
	

}
