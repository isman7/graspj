package eu.brede.graspj.gui.configurator;

import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JComponent;

public abstract class ObjectComboBox<T> implements ConfigFormItem<T> {
	
	JComboBox<T> comboBox;
	Vector<T> availableObjects;
	
	public ObjectComboBox(T selectedObject) {
		availableObjects = getAvailableObjects();
		comboBox = new JComboBox<>(availableObjects);
		comboBox.setSelectedItem(selectedObject);
	}
	
	public ObjectComboBox() {
		availableObjects = getAvailableObjects();
		comboBox = new JComboBox<>(availableObjects);
	}
	
	@Override
	public T getValue() {
		return (T) comboBox.getSelectedItem();
	}
	
	@Override
	public void setValue(Object value) {
		comboBox.setSelectedItem(value);
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
	
	protected abstract Vector<T> getAvailableObjects();

}
