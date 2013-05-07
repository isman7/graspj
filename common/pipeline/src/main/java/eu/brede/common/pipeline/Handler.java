package eu.brede.common.pipeline;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import eu.brede.common.pipeline.consumers.*;



public class Handler<T> extends AbstractConsumer<T> {
	protected List<Processor<T>> processors;
	protected BlockingQueue<T> queueOut;
	
	public Handler(List<Processor<T>> processors) {
		this.processors = processors;
	}
	
	public void setQueueIn(BlockingQueue<T> queueIn) {
		this.queueIn = queueIn;
	}
	
	public void setQueOut(BlockingQueue<T> queueOut) {
		this.queueOut = queueOut;
	}

	@Override
	protected boolean dontStopHook() {
		// don't stop is queueIn is not yet empty!
		return !queueIn.isEmpty();
	}

	@Override
	protected void use(T item) throws InterruptedException {
//		System.out.println("Handler: " + this.hashCode() + " uses Item:" + item.hashCode());
//		long startTime = System.currentTimeMillis();
		for(Processor<T> processor : processors) {
//			System.out.println("Processor " + stage.getProcessor().getClass().getSimpleName() + " called.");
//			stage.process(item);
//			System.out.println("Handler: " + this.hashCode() + " calls " + processor.getClass().getSimpleName());
//			long startTimeProcessor = System.currentTimeMillis();
//			processor.process(item);
			useWithProcessor(item, processor);
//			System.out.println("Processor " + processor.getClass().getSimpleName() + " needed "
//					+ (System.currentTimeMillis()-startTimeProcessor) + "ms");
		}
		
//		System.out.println("Full item processing took: "
//				+ (System.currentTimeMillis()-startTime) + "ms");
		
		queueOut.put(item);
	}
	
	protected void useWithProcessor(T item, Processor<T> processor) {
		processor.process(item);
	}
	
//	@Override
//	public Integer call() throws Exception {
////		System.out.println("New ItemHandler started, ID:" + this.hashCode());
//		queue.poll(timeout, unit)
//		long startTime = System.currentTimeMillis();
//		for(Stage<T> stage : stages) {
//			System.out.println("Processor " + stage.getProcessor().getClass().getSimpleName() + " called.");
//			stage.process(item);
//		}
//		System.out.println("Full package analysis took: "
//				+ (System.currentTimeMillis()-startTime) + "ms");
////		System.out.println("ItemHandler finished, ID:" + this.hashCode());
//		return item;
//	}
}
