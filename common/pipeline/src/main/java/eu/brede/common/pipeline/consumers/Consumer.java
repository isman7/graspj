package eu.brede.common.pipeline.consumers;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

import eu.brede.common.pipeline.BasicPipeline.Execution;
import eu.brede.common.pipeline.Component;
import eu.brede.common.pipeline.Controllable;

public interface Consumer<T> extends Component, Callable<Object>, Controllable {
	public BlockingQueue<T> getQueueIn();
	public void setStopCondition(Execution<?> execution);
}
