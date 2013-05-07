package eu.brede.common.pipeline.gui;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import net.vivin.GenericTreeNode;
import eu.brede.common.config.Configurable;
import eu.brede.common.pipeline.ItemLoader;
import eu.brede.common.pipeline.ListType;
import eu.brede.common.pipeline.Pipeline;
import eu.brede.common.pipeline.Processor;
import eu.brede.common.pipeline.consumers.Consumer;
import eu.brede.common.pipeline.producers.Producer;
import eu.brede.common.util.StringTools;

public class PipeTree<T> extends JTree {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Pipeline<T> pipeline;
	private JPopupMenu popupMenuConsumer = new JPopupMenu();
	private JPopupMenu popupMenuProducer = new JPopupMenu();
	private JPopupMenu popupMenuProcessor = new JPopupMenu();
	private JPopupMenu popupMenuItem = new JPopupMenu();
	private ItemLoader<T> loader;
	private ResourceBundle componentBundle;
	private ResourceBundle menuBundle;
	
	
	public PipeTree(Pipeline<T> pipeline, ItemLoader<T> loader, ResourceBundle componentBundle,
			ResourceBundle menuBundle) {
		super(new DefaultTreeModel(nodeFromPipeline(pipeline)));
		this.pipeline = pipeline;
		this.loader = loader;
		this.componentBundle = componentBundle;
		this.menuBundle = menuBundle;
		rightClick();
		autoPack();
		createPopupMenus();
	}
	
	public void setPipeline(Pipeline<T> pipeline) {
		this.pipeline = pipeline;
		this.setModel(new DefaultTreeModel(nodeFromPipeline(pipeline)));
	}
	
	// TODO should be tested!
	public Pipeline<T> getPipeline() {
		updatePipeline();
		return pipeline;
	}
	
	// TODO should be cleaned up
	@SuppressWarnings("unchecked")
	public void updatePipeline() {
		pipeline.getProducers().clear();
		pipeline.getProcessors().clear();
		pipeline.getConsumers().clear();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) getModel().getRoot();
		DefaultMutableTreeNode subRoot = (DefaultMutableTreeNode) root.getChildAt(0);
		for(int i=0;i<subRoot.getChildCount();i++) {
			pipeline.getProducers().add((Producer<T>)((DefaultMutableTreeNode)
					subRoot.getChildAt(i)).getUserObject());
		}
		subRoot = (DefaultMutableTreeNode) root.getChildAt(1);
		for(int i=0;i<subRoot.getChildCount();i++) {
			pipeline.getProcessors().add((Processor<T>)((DefaultMutableTreeNode)
					subRoot.getChildAt(i)).getUserObject());
		}
		subRoot = (DefaultMutableTreeNode) root.getChildAt(2);
		for(int i=0;i<subRoot.getChildCount();i++) {
			pipeline.getConsumers().add((Consumer<T>)((DefaultMutableTreeNode)
					subRoot.getChildAt(i)).getUserObject());
		}
		return;
	}
	
	@Override
	public DefaultTreeModel getModel() {
		return (DefaultTreeModel) super.getModel();
	}

	private static DefaultMutableTreeNode nodeFromPipeline(Pipeline<?> pipeline) {
		
		List<List<?>> list = new ArrayList<List<?>>();
		list.add(pipeline.getProducers());
		list.add(pipeline.getProcessors());
		list.add(pipeline.getConsumers());
		
		DefaultMutableTreeNode root = nodeFromList(list);
		root.setUserObject(pipeline);
//		root.
		return root;
	}
	
	private static DefaultMutableTreeNode nodeFromList(List<?> list) {
		// TODO add support for nested pipes
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(list);
		for(Object entry : list) {
			if(entry instanceof List) {
				root.add(nodeFromList((List<?>)entry));
			}
			else if (entry instanceof Pipeline) {
				root.add(nodeFromPipeline((Pipeline<?>)entry));
			}
			else {
				root.add(new DefaultMutableTreeNode(entry));
			}
		}
		return root;
	}
	
//	private PipeTree(PipelineTreeModel treeModel) {
////		super(treeModel);
//		super();
//		autoPack();
//		rightClick();
//		this.setDragEnabled(true);
//		this.setDropMode(DropMode.ON_OR_INSERT);
////		this.setTransferHandler(new PipelineTreeTransferHandler(this));
//		this.getSelectionModel().setSelectionMode(
//                TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
//
//	}
	
	private void rightClick() {
		this.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent e) {
				super.mouseReleased(e);
				
				if(e.getButton()==3) {
					TreePath path = PipeTree.this.getClosestPathForLocation(e.getX(), e.getY());
					DefaultMutableTreeNode node = 
							(DefaultMutableTreeNode) path.getLastPathComponent();  
					DefaultMutableTreeNode parent = 
							(DefaultMutableTreeNode)node.getParent();
					if(node.isRoot()) {
						// show remove menu?
					}
					else if(parent.getUserObject() instanceof Pipeline) {
						PipeTree.this.setSelectionPath(path);
						showMenu(getPopupMenu(ListType.getListType(node, PipeTree.this)),e);
					}
					else {
						PipeTree.this.setSelectionPath(path);
						showMenu(popupMenuItem,e);
					}
					
				}
			}
			
			private void showMenu(JPopupMenu popup, MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
			
		});
	}
	
	private JPopupMenu getPopupMenu(ListType type) {
		switch(type) {
			case PRODUCER: return popupMenuProducer;
			case PROCESSOR: return popupMenuProcessor;
			case CONSUMER: return popupMenuConsumer;
		}
		return null;
	}
	
	private void createPopupMenus() {
		createPopupMenu(ListType.PRODUCER);
		createPopupMenu(ListType.PROCESSOR);
		createPopupMenu(ListType.CONSUMER);
		createPopupMenuItem();
	}
	
	private void createPopupMenuItem() {
		JMenuItem mntmRemove = new JMenuItem(
				StringTools.getStringFromBundle(menuBundle, 
						getClass().getSimpleName() + "." + "remove"));
		popupMenuItem.add(mntmRemove);
		mntmRemove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)
						PipeTree.this.getSelectionPath().getLastPathComponent();
//				node.removeAllChildren();
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
				int index = parent.getIndex(node);
				node.removeFromParent();
				PipeTree.this.getModel().nodesWereRemoved(parent, new int[]{index}, new Object[]{node});
			}
		});
	}
	
	protected void createPopupMenu(ListType type) {
		JPopupMenu popupMenu = getPopupMenu(type);
		
		@SuppressWarnings("unchecked")
		GenericTreeNode<Class<?>> rootNode = 
				(GenericTreeNode<Class<?>>) loader.get(type).getRoot();
		
		JMenu mnRoot = new JMenu(
				StringTools.getStringFromBundle(menuBundle, 
						getClass().getSimpleName() + "." + "add"));
		addChildren(mnRoot,rootNode);
		popupMenu.add(mnRoot);
	}

	private void addChildren(JMenu mnRoot, GenericTreeNode<Class<?>> rootNode) {
		for(final GenericTreeNode<Class<?>> child : rootNode.getChildren()) {
			if(child.hasChildren()) {
				Class<?> clazz = child.getData();
				JMenu mnChild = new JMenu(StringTools.
						getStringFromBundle(componentBundle,clazz.getSimpleName()));
				if(!Modifier.isAbstract(clazz.getModifiers())) {
					try {
						clazz.getConstructor();
						mnChild.addMouseListener(new ClassCreatingListener(clazz));
					}
					catch (NoSuchMethodException e) {
						// happens, but is not interesting/important
					} catch (SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				addChildren(mnChild,child);
				mnRoot.add(mnChild);
			}
			else {
				JMenuItem mntmLeaf = new JMenuItem(StringTools.
						getStringFromBundle(componentBundle,child.getData().getSimpleName()));
				mntmLeaf.addActionListener(new ClassCreatingListener(child.getData()));
				mnRoot.add(mntmLeaf);
			}
		}
	}
	
	private class ClassCreatingListener extends MouseAdapter implements ActionListener {
		Class<?> clazz;
		
		public ClassCreatingListener(Class<?> clazz) {
			this.clazz = clazz;
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			instantiateAndAddClass();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			instantiateAndAddClass();
		}
		
		private void instantiateAndAddClass() {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)
					PipeTree.this.getSelectionPath().getLastPathComponent();
					
			try {
				Object object = clazz.newInstance();
				if(object instanceof Pipeline) {
					node.add(nodeFromPipeline((Pipeline<?>)object));
				}
				else {
					node.add(new DefaultMutableTreeNode(object));
				}
				PipeTree.this.getModel().nodesWereInserted(node, new int[] {node.getChildCount()-1});
			} catch (InstantiationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	protected void showProducerMenu() {
		// TODO Auto-generated method stub
		
	}

	protected void showProcessorMenu() {
	// TODO Auto-generated method stub
	
	}


	@Override
	public String convertValueToText(Object value, boolean selected,
			boolean expanded, boolean leaf, int row, boolean hasFocus) {
		return convertValueToText(value);
	}
	
	public String convertValueToText(Object value) {
		if(value!=null) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			Object object = node.getUserObject();
			if(object instanceof List) {
				if(!node.isRoot()) {
					int index = node.getParent().getIndex(node);
					switch(index) {
						case 0: 
							return StringTools.getStringFromBundle(componentBundle,"Producers");
						case 1:
							return StringTools.getStringFromBundle(componentBundle,"Processors");
						case 2:
							return StringTools.getStringFromBundle(componentBundle,"Consumers");
					}
				}
			}
			if(object instanceof Configurable) {
				String string = StringTools.
						getStringFromBundle(componentBundle,object.getClass().getSimpleName());
				if(!((Configurable)object).getConfig().getMetaData().getName().isEmpty()) {
					string += " - " + ((Configurable)object).getConfig().getMetaData().getName();
				}
				return string;
			}
			
			
			return StringTools.getStringFromBundle(componentBundle,object);
//			return value.getClass().getSimpleName();
			
		}
		return "";
	}
	
	private void autoPack() {
		this.addTreeExpansionListener(new TreeExpansionListener() {
					
					@Override
					public void treeExpanded(TreeExpansionEvent event) {
						parentPack(PipeTree.this.getParent());
					}
					
					@Override
					public void treeCollapsed(TreeExpansionEvent event) {
						parentPack(PipeTree.this.getParent());
					}
					
					private void parentPack(Component parent) {
		        		if(parent instanceof Frame) {
		        			((Frame)parent).pack();
		        		}
		        		if(parent != null) {
		        			parentPack(parent.getParent());
		        		}
		        		return;
		        	}
				});
	}
	
}
