package eu.brede.graspj.gui;

import ij.util.Java2;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DropMode;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;

import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;

import eu.brede.common.config.Configurable;
import eu.brede.common.pipeline.ItemLoader;
import eu.brede.common.pipeline.Pipeline;
import eu.brede.common.pipeline.gui.PipeTree;
import eu.brede.common.pipeline.gui.TreeTransferHandler;
import eu.brede.graspj.configs.Global;
import eu.brede.graspj.datatypes.AnalysisItem;
import eu.brede.graspj.datatypes.Workflow;
import eu.brede.graspj.gui.configurator.ConfigForm;
import eu.brede.graspj.gui.configurator.NotConfigurable;
import eu.brede.graspj.gui.configurator.NothingSelected;
import eu.brede.graspj.utils.Utils;

public class WorkflowEditor extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Workflow pipeline;
	private final PipeTree<AnalysisItem> tree;
	
	public WorkflowEditor() {
//		this(Global.getPipeline());
		this(Global.getEmptyPipeline());
	}
	
	private static class TTDefaultTreeCellRenderer extends
			DefaultTreeCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {

			setToolTipText(Utils.htmlify(Global.getStringfromBundle(
					"ComponentDescriptions", getNodeName(value))));

			return super.getTreeCellRendererComponent(tree, value, sel,
					expanded, leaf, row, hasFocus);
		}
		
		public String getNodeName(Object value) {
			if(value!=null) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
				Object object = node.getUserObject();
				if(object instanceof List) {
					if(!node.isRoot()) {
						int index = node.getParent().getIndex(node);
						switch(index) {
							case 0: 
								return "Producers";
							case 1:
								return "Processors";
							case 2:
								return "Consumers";
						}
					}
				}
				else {
					return object.getClass().getSimpleName();
				}				
			}
			return "null";
		}
	}

	
	public WorkflowEditor(Workflow pipeline) {
		this.pipeline = pipeline;
		// this line is obsolete, isn't it?
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setLayout(new HorizontalLayout());
		setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		
		final JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
//		panel.setBorder(BorderFactory.createLineBorder(Color.green));
		ItemLoader<AnalysisItem> loader = new ItemLoader<AnalysisItem>(Global.getPipeClasses());
		
		tree = new PipeTree<AnalysisItem>(pipeline,loader,
				Global.getBundle("ComponentLabels"), Global.getBundle("MenuLabels"));	
		
		tree.setCellRenderer(new TTDefaultTreeCellRenderer());
		ToolTipManager.sharedInstance().registerComponent(tree);
		
		tree.setAlignmentX(Component.LEFT_ALIGNMENT);
		tree.setAlignmentY(Component.TOP_ALIGNMENT);
		tree.setAlignmentY(Component.TOP_ALIGNMENT);
		add(tree);
		add(panel);
		tree.setDragEnabled(true);  
        tree.setDropMode(DropMode.ON_OR_INSERT);  
        tree.setTransferHandler(new TreeTransferHandler());
        tree.expandRow(0);
        for (int i = 1; i < tree.getRowCount(); i++) {
        	DefaultMutableTreeNode node = (DefaultMutableTreeNode) 
        			tree.getPathForRow(i).getLastPathComponent();
        	if(node.getUserObject() instanceof Pipeline) {        		
        		tree.collapsePath(tree.getPathForRow(i));
        	}
        	else {
        		tree.expandRow(i);
        	}
        }
        tree.getSelectionModel().setSelectionMode(  
                TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);  
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			private ConfigForm configForm;
			private TreeNode configNode;
			public void valueChanged(TreeSelectionEvent e) {
				panel.removeAll();
				panel.repaint();
				
				DefaultTreeModel model = ((DefaultTreeModel)tree.getModel());
				
				Object selectedComponent = tree.getLastSelectedPathComponent();
				DefaultMutableTreeNode node = null;
				Object object = null;
				
				if(selectedComponent instanceof DefaultMutableTreeNode) {
					node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
					object = node.getUserObject();
				}
				
				if(configForm!=null) {
					configForm.getConfig();
					model.nodeChanged(configNode);
				}
				else {
					model.nodeChanged((TreeNode) model.getRoot());
				}
				
				if(object instanceof Configurable) {
					configForm = new ConfigForm(((Configurable)object).getConfig());
					configNode = node;
					panel.add(configForm.getGUI());
//					panel.updateUI();
					// TODO use CardLayout instead!
//					panel.repaint();
				}
				else {
					configForm = null;
					configNode = null;
					JPanel pane = null;
					if(object==null) {
						pane = new NothingSelected();
					}
					else {						
						pane = new NotConfigurable(tree.convertValueToText(node));
					}
					panel.add(pane, new GridBagConstraints());
//					pane.setAlignmentX(SwingConstants.CENTER);
//					pane.setAlignmentY(SwingConstants.CENTER);
				}
//				GUITools.parentPack(panel);
			}
		});
		
		panel.add(new NothingSelected(), new GridBagConstraints());

	}
	
	public Workflow getPipeline() {
		updatePipeline();
		return pipeline;
	}
	
	private void updatePipeline() {
		pipeline = (Workflow) tree.getPipeline();
	}
	
	public void setPipeline(Pipeline<AnalysisItem> pipeline) {
		tree.setPipeline(pipeline);
	}
	
//	public BasicPipeline<AnalysisItem> getPipeline() {
//		AcquisitionData acqData = new AcquisitionData(new AcquisitionConfig());
//		acqData.getConfig().setProperty("file", 
//				new File("D:\\nbrede\\mito-storm_000_copy.dax"));
//		acqData.getConfig().setProperty("numFrames", Integer.valueOf(1024));
//		
//		AnalysisItem finalItem = new AnalysisItem();
//		// TODO configure finalItem
//		finalItem.setAcquisition(acqData);
//		finalItem.setSpots(new BufferSpotCollection((int)1e6,(int)1e4));
//		
//		AnalysisItem templateItem = new AnalysisItem();
//		// TODO configure templateItem
//		templateItem.setAcquisition(acqData);
//		
//		ArrayList<Producer<AnalysisItem>> producers = new ArrayList<Producer<AnalysisItem>>();
//		ArrayList<Processor<AnalysisItem>> processors = new ArrayList<Processor<AnalysisItem>>();
//		ArrayList<Consumer<AnalysisItem>> consumers = new ArrayList<Consumer<AnalysisItem>>();
//		
//		producers.add(new LiveProducer(templateItem));
//		processors.add(new SpotFinder());
//		processors.add(new SpotFitter());
//		processors.add(new SpotFitFilterer());
//		processors.add(new TrailGenerator());
//		processors.add(new PackageDriftCorrector());
//		processors.add(new ProductMerger());
//		consumers.add(new LiveConsumerRenderer());
////		consumers.add(new LiveRenderColor());
//		
//		ProductionPipeline<AnalysisItem,AnalysisItem> pipe = 
//			new ProductionPipeline<AnalysisItem,AnalysisItem>(producers, processors, consumers, templateItem);
//		return pipe;
//	}
	
	public static void main(String[] args) {

		Java2.setSystemLookAndFeel();
		LookAndFeelAddons.contribute(new W7TaskPaneAddon());
		
		UIManager.put("Panel.background", Color.WHITE);
		JFrame frame = new JFrame();
		frame.getContentPane().add(new WorkflowEditor());
		frame.setVisible(true);
		frame.pack();
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
}
