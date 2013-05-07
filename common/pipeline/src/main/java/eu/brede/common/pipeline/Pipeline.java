package eu.brede.common.pipeline;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Callable;

import eu.brede.common.pipeline.consumers.Consumer;
import eu.brede.common.pipeline.producers.Producer;

public interface Pipeline<T> extends Callable<Object>, Controllable, Serializable {

	public List<Producer<T>> getProducers();
	public List<Processor<T>> getProcessors();
	public List<Consumer<T>> getConsumers();

}
