package eu.brede.common.pipeline.consumers;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.brede.common.pipeline.BasicPipeline.Execution;
import eu.brede.common.pipeline.Controller;

public abstract class AbstractConsumer<T> implements Consumer<T> {
	
	protected BlockingQueue<T> queueIn;// = new LinkedBlockingQueue<T>(1);
	protected Execution<?> stopCondition;
	private Controller controller;
	
	final static Logger logger = LoggerFactory.getLogger(AbstractConsumer.class);

	public AbstractConsumer() {
		queueIn = new LinkedBlockingQueue<T>(1);
		this.controller = new Controller();
	}
	
//	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
//		in.defaultReadObject();
//		queueIn = new LinkedBlockingQueue<T>(1);
//	}
	
	@Override
	public BlockingQueue<T> getQueueIn() {
		return queueIn;
	}

	@Override
	public void setStopCondition(Execution<?> execution) {
		this.stopCondition = execution;
	}
	
	
	@Override
	public Integer call() throws Exception {
		logger.debug("{} started", this.getClass().getSimpleName());
		int numConsumed = 0;
		while (
				((!stopCondition.hasFinished() && !getController().isStopped()) 
						|| dontStopHook())
				&& !stopHook()
		) {
			while(getController().isPaused()) {
				synchronized(this) {
					wait();
				}
			}
			T newItem = queueIn.poll(200, TimeUnit.MILLISECONDS);
			if(newItem!=null) {
//				System.out.println(this.getClass().getSimpleName() + " polled package " + ((AnalysisItem)newItem).getNotes().<Integer>gett("packageNr") + " of Queue: " + queueIn);
				use(newItem);
				numConsumed++;
			}
		}
		finish();
		logger.debug("{} finished", this.getClass().getSimpleName());
		return numConsumed;
	}

	protected boolean stopHook() {
		return false;
	}

	protected boolean dontStopHook() {
		return false;
	}

	protected void finish() {
		// do nothing
	}

	abstract protected void use(T item) throws InterruptedException;

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
