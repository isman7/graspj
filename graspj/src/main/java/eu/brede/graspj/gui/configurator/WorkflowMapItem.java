package eu.brede.graspj.gui.configurator;

import java.util.List;

import eu.brede.graspj.datatypes.Workflow;
import eu.brede.graspj.datatypes.WorkflowMap;
import eu.brede.graspj.gui.GraspJ;
import eu.brede.graspj.gui.WorkflowEditor;
import eu.brede.graspj.gui.views.WorkflowView;

public class WorkflowMapItem extends ObjectListBox<Workflow> {
	
	public WorkflowMapItem(WorkflowMap map) {
		super(map);
	}

	@Override
	protected void initMenu() {
		super.initMenu();
		cmdMenu.addCmdMenuItem("Edit");
	}

	@Override
	public void runCommand(String cmd) {
		switch(cmd) {
			case "Edit":
				for(String name : list.getSelectedValuesList()) {
					List<GraspJ> gjList = GraspJ.getInstances();
					GraspJ gj = gjList.get(gjList.size()-1);
					gj.getViewManager().switchView(new WorkflowView(
							new WorkflowEditor(map.get(name))));
				}
				break;
		}
		super.runCommand(cmd);
	}

}
