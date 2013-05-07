package eu.brede.common.pipeline;

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import eu.brede.common.pipeline.gui.PipeTree;

public enum ListType {
	UNKNOWN,NOLIST,PRODUCER,PROCESSOR,CONSUMER;
	
	static public ListType getListType(DefaultMutableTreeNode node, PipeTree<?> tree) {
		Object object = node.getUserObject();
		if(!(object instanceof List)) {
			return NOLIST;
		}
		if(node.isRoot()) {
			return NOLIST;
		}
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
		int index = tree.getModel().getIndexOfChild(parent, node);
		return indexToType(index);
	}
	
	static public ListType indexToType(int index) {
		switch(index) {
			case 0: return PRODUCER;
			case 1: return PROCESSOR;
			case 2: return CONSUMER;
		}
		return UNKNOWN;
	}
}
