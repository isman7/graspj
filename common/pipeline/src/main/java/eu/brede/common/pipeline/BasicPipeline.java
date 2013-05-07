package eu.brede.common.pipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import eu.brede.common.pipeline.consumers.Consumer;
import eu.brede.common.pipeline.producers.Producer;
import eu.brede.common.util.ReportingExecutorService;

public class BasicPipeline<T> implements Pipeline<T> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private List<Producer<T>> producers;
	private List<Processor<T>> processors;
	private List<Consumer<T>> consumers;
	
	private transient Controller controller;
	
	private transient BlockingQueue<T> itemQueue;// = new LinkedBlockingQueue<T>(1);
	private transient BlockingQueue<T> productQueue;// = new LinkedBlockingQueue<T>(1);
	
	private transient List<Handler<T>> handlers;
	private transient Dispatcher<T> dispatcher;// = new Dispatcher<T>();
	
	private transient ExecutorService executor;
	
//	private final ExecutorService executor = Executors.newCachedThreadPool();
	
	// modified executor to print exceptions! NOTE: only one of three submit methods is overridden
//	private final ExecutorService executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
//            60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>()) {
//				@Override
//				public <Y> Future<Y> submit(final Callable<Y> task) {
//					Callable<Y> wrappedTask = new Callable<Y>() {
//			            @Override
//			            public Y call() throws Exception {
//			                try {
//			                    return task.call();
//			                }
//			                catch (Exception e) {
//			                    System.out.println("Problem reported by ExecutorService:\n");
//			                    e.printStackTrace();
//			                    throw e;
//			                }
//			            }
//			        };
//			        
//					return super.submit(wrappedTask);
//				}
//		
//	};
	
//	private transient ExecutorService executor = new ReportingExecutorService();
	
	
	private transient Production production;
	private transient Handling handling;
	private transient Dispatching dispatching;
	private transient Consumption consumption;
	
	// creates empty pipeline
	public BasicPipeline() {
		this(	new ArrayList<Producer<T>>(),
				new ArrayList<Processor<T>>(),
				new ArrayList<Consumer<T>>());
	}
	
	public BasicPipeline(List<Producer<T>> producers, List<Processor<T>> processors,
			List<Consumer<T>> consumers) {
		
		super();
		this.producers = producers;
		this.processors = processors;
		this.consumers = consumers;
		
	}
	
//	private Object readResolve() throws ObjectStreamException {
//		executor = new ReportingExecutorService();
//		return this;
//	}

	public List<Producer<T>> getProducers() {
		return producers;
	}

	public List<Processor<T>> getProcessors() {
		return processors;
	}

	public List<Consumer<T>> getConsumers() {
		return consumers;
	}

	// TODO instead define start method for pipeline and lager call aus :-)
	protected void initComponents() {
		if(getController()==null) {
			setController(new Controller());
		}
		
//		for(Producer<T> producer : getProducers()) {
//			if(producer.getController()==null) {
//				producer.setController(getController());
//			}
//		}
//		
//		for(Consumer<T> consumer : getConsumers()) {
//			if(consumer.getController()==null) {
//				consumer.setController(getController());
//			}
//		}
//		
//		for(Consumer<T> consumer : getConsumers()) {
//			if(consumer.getController()==null) {
//				consumer.setController(getController());
//			}
//		}
		
		itemQueue = new LinkedBlockingQueue<T>(1);
		productQueue = new LinkedBlockingQueue<T>(1);
		dispatcher = new Dispatcher<T>();
//		Handler<T> handler = new Handler<T>(processors);
		handlers = new ArrayList<Handler<T>>();
		handlers.add(initHandler(processors));
		
		production = new Production();
		handling = new Handling();
		dispatching = new Dispatching();
		consumption = new Consumption();
		
		Execution<?>[] executions = new Execution<?>[] {
				production, handling, dispatching, consumption};
		
		for(Execution<?> execution : executions) {
			execution.setExecutor(getExecutor());
		}
	}
	
	protected Handler<T> initHandler(List<Processor<T>> processors) {
		return new Handler<T>(processors);
	}


	@Override
	public Object call() throws Exception {
		initComponents();
//		Execution.PRODUCTION.start(producers);
//		Execution.HANDLING.start(handlers);
//		Execution.DISPATCHING.start(dispatcher);
//		Execution.CONSUMPTION.start(consumers);
//		while(Execution.isRunning()) {
//			Thread.sleep(500);
//		}

		production.start();
		handling.start();
		dispatching.start();
		consumption.start();
		
		while(!consumption.hasFinished()) {
			Thread.sleep(500);
		}
		return production.getProducts();
	}
	
	public static class Execution<E extends Callable<?>> {
		protected ArrayList<Future<?>> futures = new ArrayList<Future<?>>();
		protected List<E> callables;
		protected ExecutorService executor;
		
		public Execution(List<E> callables) {
			this.callables = callables; 
		}
		
		public Execution(E callable) {
			this(new ArrayList<E>());
			callables.add(callable);
		}
		
		public void start() {
//			
//			for(Callable<?> callable : callables) {
//				futures.add(Global.INSTANCE.getExecutor().submit(callable));
//			}
//			
			for (Iterator<E> it = callables.iterator(); it.hasNext(); ) {
				Callable<?> callable = it.next();
			    futures.add(getExecutor().submit(callable));
				// TODO needs fix
			}

		}
		
		public boolean hasFinished() {
//			for(Future<?> future : futures) {
//				if(!future.isDone()) {
//					return false;
//				}
//			}
//			
			synchronized (futures) {				
				for (Iterator<Future<?>> it = futures.iterator(); it.hasNext(); ) {
					Future<?> future = it.next();
					if(!future.isDone()) {
						return false;
					}
				}
			}
			return true;
		}
		
		public synchronized ExecutorService getExecutor() {
			if(executor==null) {
				executor = new ReportingExecutorService();
			}
			return executor;
		}

		public void setExecutor(ExecutorService executor) {
			this.executor = executor;
		}
	}
	
	private class Production extends Execution<Producer<T>> {
		
		public Production() {
			super(producers);
			for(Producer<T> producer : producers) {
				producer.setQueueOut(itemQueue);
				producer.setController(getController());
			}
		}
		
		public Collection<Object> getProducts() {
			Collection<Object> result = new ArrayList<Object>();
			for(Future<?> future : futures) {
				try {
					result.add((Object)future.get());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return result;
		}
		
	}
	
	private class Handling extends Execution<Handler<T>> {
		
		public Handling() {
			super(handlers);
			for(Handler<T> handler : handlers) {
				handler.setQueueIn(itemQueue);
				handler.setQueOut(productQueue);
				handler.setStopCondition(production);
//				handler.setController(getController());
			}
		}
	}
	
	private class Dispatching extends Execution<Dispatcher<T>> {
		
		public Dispatching() {
			super(dispatcher);
			dispatcher.setConsumers(consumers);
			dispatcher.setQueueIn(productQueue);
			dispatcher.setStopCondition(handling);
//			dispatcher.setController(getController());
		}
	}
	
	private class Consumption extends Execution<Consumer<T>> {
		
		public Consumption() {
			super(consumers);
			for(Consumer<T> consumer : consumers) {
				consumer.setStopCondition(dispatching);
//				consumer.setController(getController());
			}
		}
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

	public synchronized ExecutorService getExecutor() {
		if(executor==null) {
			executor = new ReportingExecutorService();
		}
		return executor;
	}

	public void setExecutor(ExecutorService executor) {
		this.executor = executor;
	}
}
