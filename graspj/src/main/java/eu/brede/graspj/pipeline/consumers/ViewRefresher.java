package eu.brede.graspj.pipeline.consumers;

import java.io.Serializable;

import eu.brede.common.pipeline.consumers.AbstractConsumer;
import eu.brede.graspj.datatypes.AnalysisItem;
import eu.brede.graspj.gui.views.View;

public class ViewRefresher extends AbstractConsumer<AnalysisItem>
		implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private transient View view;
	
	public ViewRefresher(View view) {
		this.view = view;
	}

	@Override
	protected void use(AnalysisItem item) throws InterruptedException {
		view.refresh();
	}

	
}
