package eu.brede.common.util;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class AutoPackAdapter extends ComponentAdapter {
	
	private Component component;
	private AutoPackType type;
	
	public enum AutoPackType {
		FIRST, ALL
	}
	
	
	
	public AutoPackAdapter(Component component, AutoPackType type) {
		super();
		this.component = component;
		this.type = type;
	}
	
	
	public void componentResized(ComponentEvent e) {
		switch(type) {
			case FIRST:
				parentPackFirst(component.getParent());
				break;
			case ALL:
				parentPackAll(component.getParent());
				break;
		}
	}
	
	private void parentPackAll(Component parent) {
		if(parent instanceof Frame) {
			((Frame)parent).pack();
		}
		if(parent != null) {
			parentPackAll(parent.getParent());
		}
		return;
	}
	
	private void parentPackFirst(Component parent) {
		if(parent instanceof Frame) {
			((Frame)parent).pack();
			return;
		}
		if(parent != null) {
			parentPackFirst(parent.getParent());
		}
		return;
	}
}
