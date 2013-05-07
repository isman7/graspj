package eu.brede.common.pipeline.hybrids;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import eu.brede.common.pipeline.BasicPipeline;
import eu.brede.common.pipeline.BasicPipeline.Execution;
import eu.brede.common.pipeline.Controller;
import eu.brede.common.pipeline.Pipeline;
import eu.brede.common.pipeline.Processor;
import eu.brede.common.pipeline.consumers.Consumer;
import eu.brede.common.pipeline.producers.Producer;

public class ConsumptionPipeline<P,C> implements Pipeline<P>, Consumer<C>  {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected Pipeline<P> pipeline;
	private ProducingConsumer<P,C> pc;
	private Controller controller;
	
	
	@Override
	public Object call() throws Exception {
		Future<Object> pipeFuture = Executors.newSingleThreadExecutor().submit(pipeline);
		return pipeFuture.get();
//		return super.call();
	}
	
	public ConsumptionPipeline(ProducingConsumer<P,C> pc) {
		this.pc = pc;
		pipeline = new BasicPipeline<P>();
		pipeline.getProducers().add(pc);
	}
	
	public ConsumptionPipeline(ProducingConsumer<P,C> pc, Pipeline<P> pipeline) {
		this.pc = pc;
		this.pipeline = pipeline;
		pipeline.getProducers().add(pc);
	}
	
	@Override
	public List<Producer<P>> getProducers() {
		// TODO should this one be unmodifiable?
		// Would cause problem with update pipeline in PipeTree
		return pipeline.getProducers();
	}

	@Override
	public List<Processor<P>> getProcessors() {
		return pipeline.getProcessors();
	}

	@Override
	public List<Consumer<P>> getConsumers() {
		return pipeline.getConsumers();
	}

	@Override
	public BlockingQueue<C> getQueueIn() {
		return pc.getQueueIn();
	}

	@Override
	public void setStopCondition(Execution<?> execution) {
		pc.setStopCondition(execution);
	}

	@Override
	public Controller getController() {
		return controller;
	}

	@Override
	public void setController(Controller controller) {
		this.controller = controller;
		pipeline.setController(controller);
		pc.setController(controller);
		controller.getList().add(this);
	}



//	@Override
//	protected void use(C item) throws InterruptedException {
//		pc.getQueueIn().put(item);
//	}
	
}
