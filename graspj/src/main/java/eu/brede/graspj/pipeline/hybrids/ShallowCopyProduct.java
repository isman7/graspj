package eu.brede.graspj.pipeline.hybrids;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;

import eu.brede.common.pipeline.NeedsProduct;
import eu.brede.common.pipeline.consumers.AbstractConsumer;
import eu.brede.common.pipeline.hybrids.ProducingConsumer;
import eu.brede.graspj.datatypes.AnalysisItem;

public class ShallowCopyProduct extends AbstractConsumer<AnalysisItem> 
	implements ProducingConsumer<AnalysisItem,AnalysisItem>,
	NeedsProduct<AnalysisItem>, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private BlockingQueue<AnalysisItem> queue;
	private AnalysisItem product; 

	@Override
	public void setQueueOut(BlockingQueue<AnalysisItem> queue) {
		this.queue = queue;
	}

	@Override
	protected void use(AnalysisItem item) throws InterruptedException {
		queue.put(product.shallowCopy());
	}

	@Override
	public void setProduct(AnalysisItem product) {
		this.product = product;
	}

	@Override
	public BlockingQueue<AnalysisItem> getQueueOut() {
		return queue;
	}


}
