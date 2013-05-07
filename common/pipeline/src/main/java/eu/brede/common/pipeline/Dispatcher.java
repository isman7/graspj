package eu.brede.common.pipeline;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import eu.brede.common.pipeline.consumers.AbstractConsumer;
import eu.brede.common.pipeline.consumers.Consumer;

public class Dispatcher<T> extends AbstractConsumer<T> {
	private List<Consumer<T>> consumers;
	
	public void setQueueIn(BlockingQueue<T> queueIn) {
		this.queueIn = queueIn;
	}
	
	public void setConsumers(List<Consumer<T>> consumers) {
		this.consumers = consumers;
	}

	@Override
	protected void use(T item) throws InterruptedException {
		for(Consumer<T> consumer : consumers) {
//			consumer.getQueueIn().add(item);
			consumer.getQueueIn().put(item);
		}
	}
	
	
	
}
