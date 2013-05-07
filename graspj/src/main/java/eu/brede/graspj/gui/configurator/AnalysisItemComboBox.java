package eu.brede.graspj.gui.configurator;

import java.util.Vector;

import eu.brede.graspj.datatypes.AnalysisItem;
import eu.brede.graspj.gui.GraspJ;
import eu.brede.graspj.gui.views.AnalysisItemView;
import eu.brede.graspj.gui.views.View;

public class AnalysisItemComboBox extends ObjectComboBox<AnalysisItem> {
	
	private AnalysisItem item;

	public AnalysisItemComboBox(AnalysisItem selectedItem) {
		super();
		this.item = selectedItem;
		comboBox.setEditable(false);
		comboBox.setSelectedItem(selectedItem);
	}

	@Override
	protected Vector<AnalysisItem> getAvailableObjects() {
		Vector<AnalysisItem> items = new Vector<>();
		for(GraspJ instance : GraspJ.getInstances()) {
			for(View view : instance.getViewManager().getAllViews()) {
				if(view instanceof AnalysisItemView) {
					items.add(((AnalysisItemView) view).getAnalysisItem());
				}
			}
		}
		return items;
	}

	@Override
	public AnalysisItem getValue() {
		Object selectedItem = super.getValue(); 
		if(selectedItem==null) {
			selectedItem = item;
		}
		return (AnalysisItem)selectedItem;
	}
	
	

}
