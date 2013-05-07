package eu.brede.graspj.gui.configurator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListModel;

import eu.brede.graspj.datatypes.Selection;

public class SelectListBox<T> implements ConfigFormItem<Selection<T>> {
	
	private JList<T> list;
	private Selection<T> selection;
	
	public SelectListBox(Selection<T> selection) {
		this.selection = selection;
		list = new JList<>(new Vector<T>(selection.getOptions()));
		updateListBox();
	}
	
	public void updateSelection() {
		selection.setSelected(list.getSelectedValuesList());
	}
	
	public void updateListBox() {
		setSelectedValues(selection.getSelected());
	}
	
	@Override
	public Selection<T> getValue() {
		updateSelection();
		return selection;
	}

	@Override
	public void setLabelText(String labelStr) {
		//comboBox.setText(labelStr);
	}

	@Override
	public void setToolTipText(String text) {
		list.setToolTipText(text);
	}

	@Override
	public JComponent getGUI() {
		return list;
	}

	@Override
	public void setEnabled(boolean enabled) {
		list.setEnabled(enabled);
	}
	
	private void setSelectedValues(Collection<T> values) {
	    Set<Integer> indexSet = new HashSet<>();
	    for (T value : values) {
	        int index = getIndex(value);
	        if (index >=0) {
	            indexSet.add(index);
	        }
	    }
	    int[] indices = new int[indexSet.size()];
	    Integer[] indexSetArray = indexSet.toArray(new Integer[0]);
	    for(int i=0; i<indexSet.size(); i++) {
	    	indices[i] = indexSetArray[i];
	    }
	    
	    list.setSelectedIndices(indices);
	}

	private int getIndex(Object value) {
	    if (value == null) return -1;
	    ListModel<T> model = list.getModel();
		if (model instanceof DefaultListModel) {
	        return ((DefaultListModel<T>) model).indexOf(value);
	    }
	    for (int i = 0; i < model.getSize(); i++) {
	        if (value.equals(model.getElementAt(i))) return i;
	    }
	    return -1;
	}

	@Override
	public void setValue(Object value) {
		throw new Error("missing implemantation");
	}


}
