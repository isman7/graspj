package eu.brede.common.pipeline.hybrids;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import eu.brede.common.pipeline.NeedsProduct;
import eu.brede.common.pipeline.Pipeline;
import eu.brede.common.pipeline.Processor;
import eu.brede.common.pipeline.consumers.AbstractConsumer;
import eu.brede.common.pipeline.consumers.Consumer;
import eu.brede.common.pipeline.producers.Producer;

public abstract class AbstractPCPipeline<T,P,C> extends AbstractConsumer<C>
	implements Pipeline<T>, Producer<P>, NeedsProduct<P> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected ProductionPipeline<T,P> pipeline;

	public AbstractPCPipeline() {
		super();
		this.pipeline = new ProductionPipeline<>();
	}


	@Override
	public void setQueueOut(BlockingQueue<P> queue) {
		pipeline.setQueueOut(queue);
	}

	
	@Override
	public List<Producer<T>> getProducers() {
		return pipeline.getProducers();
	}


	@Override
	public List<Processor<T>> getProcessors() {
		return pipeline.getProcessors();
	}


	@Override
	public List<Consumer<T>> getConsumers() {
		return pipeline.getConsumers();
	}


	@Override
	public void setProduct(P product) {
		pipeline.setProduct(product);
	}


	@Override
	public BlockingQueue<P> getQueueOut() {
		return pipeline.getQueueOut();
	}

	
}
