package eu.brede.common.pipeline.producers;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

import eu.brede.common.pipeline.Component;
import eu.brede.common.pipeline.Controllable;

public interface Producer<T> extends Component, Callable<Object>, Controllable {
	public void setQueueOut(BlockingQueue<T> queue);
	public BlockingQueue<T> getQueueOut();
}
