package eu.brede.common.pipeline.hybrids;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import eu.brede.common.pipeline.BasicPipeline;
import eu.brede.common.pipeline.NeedsProduct;
import eu.brede.common.pipeline.Processor;
import eu.brede.common.pipeline.consumers.Consumer;
import eu.brede.common.pipeline.producers.Producer;

public class ProductionPipeline<T,P> extends BasicPipeline<T> 
	implements Producer<P>, NeedsProduct<P> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected transient P product;
	protected transient BlockingQueue<P> queue;
	
	@Override
	public Object call() throws Exception {
//		initComponents();
		super.call();
//		initComponents();
		if(queue!=null) {
			queue.put(product);
		}
		return product;
	}
	
	// create empty production pipeline
	public ProductionPipeline() {
		super(	new ArrayList<Producer<T>>(),
				new ArrayList<Processor<T>>(),
				new ArrayList<Consumer<T>>());
		// TODO product needs to be created by (first) producer
		
//		@SuppressWarnings("unchecked")
//		Class<P> clazz = (Class<P>) product.getClass();
//		
//		try {
//			
//			product = clazz.newInstance();
//		} catch (InstantiationException e) {
//			e.printStackTrace();
//			throw new RuntimeException("product class " + clazz.getSimpleName()
//					+ " could not be instatiated with default constructor");
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//		}
	}
	
	public ProductionPipeline(P product) {
		this();
		this.product = product;
	}
	
	public ProductionPipeline(ProductionPipeline<T,P> pipe) {
		this(	pipe.getProducers(),
				pipe.getProcessors(),
				pipe.getConsumers(),
				pipe.getProduct());
	}
	
	public ProductionPipeline(List<Producer<T>> producers, List<Processor<T>> processors,
			List<Consumer<T>> consumers, P product) {
		super(producers, processors, consumers);
		this.product = product;
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void initComponents() {
		super.initComponents();
		
		List<?>[] lists = new List<?>[] {
					getProducers(),
					getProcessors(),
					getConsumers()
				};
		for(List<?> list : lists) {
			synchronized (list) {
				for (Iterator<?> it = list.iterator(); it.hasNext(); ) {
					Object component = it.next();
					if(component instanceof NeedsProduct<?>) {
						((NeedsProduct<P>)component).setProduct(product);
					}
				}
			}
		}
	}
	
	// Returns current Product
	public P getProduct() {
		return product;		
	}
	

	@Override
	public void setProduct(P product) {
		this.product = product;
		initComponents();
	}

	@Override
	public void setQueueOut(BlockingQueue<P> queue) {
		this.queue = queue;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

	@Override
	public BlockingQueue<P> getQueueOut() {
		return queue;
	}

	
	

}
