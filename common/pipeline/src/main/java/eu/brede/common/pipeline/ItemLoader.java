package eu.brede.common.pipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import eu.brede.common.pipeline.BasicPipeline.Execution;
import eu.brede.common.pipeline.consumers.Consumer;
import eu.brede.common.pipeline.producers.Producer;

import net.vivin.GenericTree;
import net.vivin.GenericTreeNode;


public class ItemLoader<T> {
	
	private GenericTree<Class<? extends Producer<T>>> producers;
	private GenericTree<Class<? extends Processor<T>>> processors;
	private GenericTree<Class<? extends Consumer<T>>> consumers;
	
	private ItemLoader() {
		producers = new GenericTree<Class<? extends Producer<T>>>();
		processors = new GenericTree<Class<? extends Processor<T>>>();
		consumers = new GenericTree<Class<? extends Consumer<T>>>();
	}
	
	public ItemLoader(List<Class<?>> classList) {
		this();
		buildTreesFromClassList(classList);
	}
	
	
	public GenericTree<Class<? extends Producer<T>>> getProducers() {
		return producers;
	}

	public GenericTree<Class<? extends Processor<T>>> getProcessors() {
		return processors;
	}

	public GenericTree<Class<? extends Consumer<T>>> getConsumers() {
		return consumers;
	}

	public  GenericTree<?> get(ListType type) {
		switch(type) {
			case PRODUCER: return getProducers();
			case PROCESSOR: return getProcessors(); 
			case CONSUMER: return getConsumers(); 
		}
		return null;
	}
	
	// TODO clean up!
	@SuppressWarnings("unchecked")
	private void buildTreesFromClassList(List<Class<?>> classList) {
		List<GenericTreeNode<Class<? extends Producer<T>>>> producerList = 
				new ArrayList<GenericTreeNode<Class<? extends Producer<T>>>>();
		
		List<GenericTreeNode<Class<? extends Processor<T>>>> processorList = 
				new ArrayList<GenericTreeNode<Class<? extends Processor<T>>>>();
		
		List<GenericTreeNode<Class<? extends Consumer<T>>>> consumerList = 
				new ArrayList<GenericTreeNode<Class<? extends Consumer<T>>>>();

		for(Class<?> clazz : classList) {
			if(Producer.class.isAssignableFrom(clazz)) {
				producerList.add(new GenericTreeNode<Class<? extends Producer<T>>>((Class<? extends Producer<T>>) clazz));
			}
			if(Processor.class.isAssignableFrom(clazz)) {
				processorList.add(new GenericTreeNode<Class<? extends Processor<T>>>((Class<? extends Processor<T>>) clazz));
			}
			if(Consumer.class.isAssignableFrom(clazz)) {
				consumerList.add(new GenericTreeNode<Class<? extends Consumer<T>>>((Class<? extends Consumer<T>>) clazz));
			}
		}
		
		Producer<T> producer = new Producer<T>() {

			@Override
			public Object call() throws Exception {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Controller getController() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void setController(Controller controller) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setQueueOut(BlockingQueue<T> queue) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public BlockingQueue<T> getQueueOut() {
				// TODO Auto-generated method stub
				return null;
			}
		};
		GenericTreeNode<Class<? extends Producer<T>>> producerRootNode = 
				new GenericTreeNode<Class<? extends Producer<T>>>((Class<? extends Producer<T>>)producer .getClass());
		buildTreeFromClassNodes(producerRootNode,producerList);
		this.getProducers().setRoot(producerRootNode);
		
		Processor<T> processor = new Processor<T>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void process(T item) {
				// TODO Auto-generated method stub
				
			}
		};
		
		
		GenericTreeNode<Class<? extends Processor<T>>> processorRootNode = 
				new GenericTreeNode<Class<? extends Processor<T>>>((Class<? extends Processor<T>>) processor.getClass());
		buildTreeFromClassNodes(processorRootNode,processorList);
		this.getProcessors().setRoot(processorRootNode);
		
		Consumer<T> consumer = new Consumer<T>() {

			@Override
			public Object call() throws Exception {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Controller getController() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void setController(Controller controller) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public BlockingQueue<T> getQueueIn() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void setStopCondition(Execution<?> execution) {
				// TODO Auto-generated method stub
				
			}
		};
		GenericTreeNode<Class<? extends Consumer<T>>> consumerRootNode = 
				new GenericTreeNode<Class<? extends Consumer<T>>>((Class<? extends Consumer<T>>) consumer .getClass());
		buildTreeFromClassNodes(consumerRootNode,consumerList);
		this.getConsumers().setRoot(consumerRootNode);
		
		return;
	}
	
	private <P> void buildTreeFromClassNodes(GenericTreeNode<Class<? extends P>> rootNode, List<GenericTreeNode<Class<? extends P>>> list) {
		for(GenericTreeNode<Class<? extends P>> outerNode : list) {
			for(GenericTreeNode<Class<? extends P>> innerNode : list) {
				if(outerNode.getData().getSuperclass().equals(innerNode.getData())) {
					innerNode.addChild(outerNode);
					break;
				}
			}
			if(outerNode.getParent()==null) {
				rootNode.addChild(outerNode);
			}
		}
	}

}
