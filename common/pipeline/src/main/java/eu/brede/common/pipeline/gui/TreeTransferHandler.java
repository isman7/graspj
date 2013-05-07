package eu.brede.common.pipeline.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import eu.brede.common.pipeline.ListType;
import eu.brede.common.pipeline.BasicPipeline;
import eu.brede.common.pipeline.Processor;
import eu.brede.common.pipeline.consumers.Consumer;
import eu.brede.common.pipeline.producers.Producer;

public class TreeTransferHandler extends TransferHandler {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	DataFlavor nodesFlavor;
    DataFlavor[] flavors = new DataFlavor[1];
    DefaultMutableTreeNode[] nodesToRemove;

    public TreeTransferHandler() {
        try {
            String mimeType = DataFlavor.javaJVMLocalObjectMimeType +
                              ";class=\"" +
                javax.swing.tree.DefaultMutableTreeNode[].class.getName() +
                              "\"";
            nodesFlavor = new DataFlavor(mimeType);
            flavors[0] = nodesFlavor;
        } catch(ClassNotFoundException e) {
            System.out.println("ClassNotFound: " + e.getMessage());
        }
    }
    
    
    // TODO can be made much more efficient!
    public boolean canImport(TransferHandler.TransferSupport support) {
        if(!support.isDrop()) {
            return false;
        }
        support.setShowDropLocation(true);
        if(!support.isDataFlavorSupported(nodesFlavor)) {
            return false;
        }
        
        // get transferObject. On fail, return false.
        Object transferObject = null;
        try {
			DefaultMutableTreeNode[] transferData = (DefaultMutableTreeNode[])
					support.getTransferable().getTransferData(nodesFlavor);
			transferObject = transferData[0].getUserObject();
			
		}
        catch (UnsupportedFlavorException e) {
			e.printStackTrace();
			return false;
		}
        catch (IOException e) {
			e.printStackTrace();
			return false;
		}
        
        // do not support movement of Lists!
        if(transferObject instanceof List) {
			return false;
		}
        
        // Do not allow a drop on the drag source selections.
        JTree.DropLocation dl =
                (JTree.DropLocation)support.getDropLocation();
        PipeTree<?> tree = (PipeTree<?>)support.getComponent();
        int dropRow = tree.getRowForPath(dl.getPath());
        int[] selRows = tree.getSelectionRows();
        for(int i = 0; i < selRows.length; i++) {
            if(selRows[i] == dropRow) {
                return false;
            }
        }
        
        DefaultMutableTreeNode dropNode = (DefaultMutableTreeNode)
        		dl.getPath().getLastPathComponent();
        
        // Do only allow drops to list (make case below impossible)
        if(!(dropNode.getUserObject() instanceof List)) {
        	return false;
        }
        
        // Do not allow drops on pipeline node
        if(dropNode.getUserObject() instanceof BasicPipeline) {
        	return false;
        }
        
        // Do only allow drop on list if transferable supports
        // needed operation (consume, produce, process...)
        switch(ListType.getListType(dropNode, tree)) {
        	case PRODUCER: if(!(transferObject instanceof Producer)) { return false;} break;
        	case PROCESSOR: if(!(transferObject instanceof Processor)) { return false;} break;
        	case CONSUMER: if(!(transferObject instanceof Consumer)) { return false;} break;
        }
        
        // Do not allow MOVE-action drops if a non-leaf node is
        // selected unless all of its children are also selected.
        int action = support.getDropAction();
        if(action == MOVE) {
            return haveCompleteNode(tree);
        }
        // Do not allow a non-leaf node to be copied to a level
        // which is less than its source level.
        TreePath dest = dl.getPath();
        DefaultMutableTreeNode target =
            (DefaultMutableTreeNode)dest.getLastPathComponent();
        TreePath path = tree.getPathForRow(selRows[0]);
        DefaultMutableTreeNode firstNode =
            (DefaultMutableTreeNode)path.getLastPathComponent();
        if(firstNode.getChildCount() > 0 &&
               target.getLevel() < firstNode.getLevel()) {
            return false;
        }
        return true;
    }

    private boolean haveCompleteNode(JTree tree) {
        int[] selRows = tree.getSelectionRows();
        TreePath path = tree.getPathForRow(selRows[0]);
        DefaultMutableTreeNode first =
            (DefaultMutableTreeNode)path.getLastPathComponent();
        int childCount = first.getChildCount();
        // first has children and no children are selected.
        if(childCount > 0 && selRows.length == 1)
            return false;
        // first may have children.
        for(int i = 1; i < selRows.length; i++) {
            path = tree.getPathForRow(selRows[i]);
            DefaultMutableTreeNode next =
                (DefaultMutableTreeNode)path.getLastPathComponent();
            if(first.isNodeChild(next)) {
                // Found a child of first.
                if(childCount > selRows.length-1) {
                    // Not all children of first are selected.
                    return false;
                }
            }
        }
        return true;
    }

    protected Transferable createTransferable(JComponent c) {
        JTree tree = (JTree)c;
        TreePath[] paths = tree.getSelectionPaths();
        if(paths != null) {
            // Make up a node array of copies for transfer and
            // another for/of the nodes that will be removed in
            // exportDone after a successful drop.
            List<DefaultMutableTreeNode> copies =
                new ArrayList<DefaultMutableTreeNode>();
            List<DefaultMutableTreeNode> toRemove =
                new ArrayList<DefaultMutableTreeNode>();
            DefaultMutableTreeNode node =
                (DefaultMutableTreeNode)paths[0].getLastPathComponent();
            DefaultMutableTreeNode copy = copy(node);
            copies.add(copy);
            toRemove.add(node);
            for(int i = 1; i < paths.length; i++) {
                DefaultMutableTreeNode next =
                    (DefaultMutableTreeNode)paths[i].getLastPathComponent();
                // Do not allow higher level nodes to be added to list.
                if(next.getLevel() < node.getLevel()) {
                    break;
                } else if(next.getLevel() > node.getLevel()) {  // child node
                    copy.add(copy(next));
                    // node already contains child
                } else {                                        // sibling
                    copies.add(copy(next));
                    toRemove.add(next);
                }
            }
            DefaultMutableTreeNode[] nodes =
                copies.toArray(new DefaultMutableTreeNode[copies.size()]);
            nodesToRemove =
                toRemove.toArray(new DefaultMutableTreeNode[toRemove.size()]);
            return new NodesTransferable(nodes);
        }
        return null;
    }

    /** Defensive copy used in createTransferable. */
    // TODO check for: Cloneable, Copy Constructor or copy method
    private DefaultMutableTreeNode copy(TreeNode node) {
    	Object object = ((DefaultMutableTreeNode)node).getUserObject();
    	Class<?> clazz = object.getClass();
    	try {
			return new DefaultMutableTreeNode(clazz.getConstructor(clazz).newInstance(object));
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			System.out.println("Object has no copy-constructor");
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	System.out.println("WARNING: shallow copy performed");
        return new DefaultMutableTreeNode(((DefaultMutableTreeNode)node).getUserObject());
    }

    protected void exportDone(JComponent source, Transferable data, int action) {
        if((action & MOVE) == MOVE) {
            JTree tree = (JTree)source;
            DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
            // Remove nodes saved in nodesToRemove in createTransferable.
            for(int i = 0; i < nodesToRemove.length; i++) {
                model.removeNodeFromParent(nodesToRemove[i]);
            }
        }
    }

    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }

    public boolean importData(TransferHandler.TransferSupport support) {
        if(!canImport(support)) {
            return false;
        }
        // Extract transfer data.
        DefaultMutableTreeNode[] nodes = null;
        try {
            Transferable t = support.getTransferable();
            nodes = (DefaultMutableTreeNode[])t.getTransferData(nodesFlavor);
        } catch(UnsupportedFlavorException ufe) {
            System.out.println("UnsupportedFlavor: " + ufe.getMessage());
        } catch(java.io.IOException ioe) {
            System.out.println("I/O error: " + ioe.getMessage());
        }
        // Get drop location info.
        JTree.DropLocation dl =
                (JTree.DropLocation)support.getDropLocation();
        int childIndex = dl.getChildIndex();
        TreePath dest = dl.getPath();
        DefaultMutableTreeNode parent =
            (DefaultMutableTreeNode)dest.getLastPathComponent();
        JTree tree = (JTree)support.getComponent();
        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
        // Configure for drop mode.
        int index = childIndex;    // DropMode.INSERT
        if(childIndex == -1) {     // DropMode.ON
            index = parent.getChildCount();
        }
        // Add data to model.
        for(int i = 0; i < nodes.length; i++) {
            model.insertNodeInto(nodes[i], parent, index++);
        }
        return true;
    }
    
    public String toString() {
        return getClass().getName();
    }

    public class NodesTransferable implements Transferable {
        DefaultMutableTreeNode[] nodes;

        public NodesTransferable(DefaultMutableTreeNode[] nodes) {
            this.nodes = nodes;
         }

        public Object getTransferData(DataFlavor flavor)
                                 throws UnsupportedFlavorException {
            if(!isDataFlavorSupported(flavor))
                throw new UnsupportedFlavorException(flavor);
            return nodes;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return flavors;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return nodesFlavor.equals(flavor);
        }
    }
}
