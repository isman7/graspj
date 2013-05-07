package eu.brede.common.pipeline.producers;

import java.util.concurrent.BlockingQueue;

import eu.brede.common.pipeline.Controller;

public abstract class AbstractProducer<T> implements Producer<T> {
	
	protected BlockingQueue<T> queue;
	private Controller controller;
	
	public AbstractProducer() {
		this.controller = new Controller();
	}

	@Override
	public void setQueueOut(BlockingQueue<T> queue) {
		this.queue = queue;
	}
	
	@Override
	public BlockingQueue<T> getQueueOut() {
		return queue;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	@Override
	public Controller getController() {
		return controller;
	}

	@Override
	public void setController(Controller controller) {
		this.controller = controller;
		controller.getList().add(this);
	}
	
	
}
