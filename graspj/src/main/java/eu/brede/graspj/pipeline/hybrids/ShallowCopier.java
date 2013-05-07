package eu.brede.graspj.pipeline.hybrids;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;

import eu.brede.common.pipeline.consumers.AbstractConsumer;
import eu.brede.common.pipeline.hybrids.ProducingConsumer;
import eu.brede.graspj.datatypes.AnalysisItem;

public class ShallowCopier extends AbstractConsumer<AnalysisItem> 
	implements ProducingConsumer<AnalysisItem,AnalysisItem>, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private BlockingQueue<AnalysisItem> queue;

	@Override
	public void setQueueOut(BlockingQueue<AnalysisItem> queue) {
		this.queue = queue;
	}

	@Override
	protected void use(AnalysisItem item) throws InterruptedException {
		queue.put(item.shallowCopy());
	}

	@Override
	public BlockingQueue<AnalysisItem> getQueueOut() {
		return queue;
	}


}
