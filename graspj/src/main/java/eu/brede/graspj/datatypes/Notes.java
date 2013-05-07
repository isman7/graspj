package eu.brede.graspj.datatypes;

import eu.brede.common.config.ESOMap;

public class Notes extends ESOMap {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public WorkflowMap getAttachedWorkflows() {
		if(!containsKey("attachedWorkflows")) {
			put("attachedWorkflows", new WorkflowMap());
		}
		return gett("attachedWorkflows");
	}
}
