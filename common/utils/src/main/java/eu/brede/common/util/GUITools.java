package eu.brede.common.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.lang.Math;

public class GUITools {
	public static void setSizeToUserPrefered(Component component) {
		if(component==null) {
			return;
		}
		Dimension preferedSize = component.getPreferredSize();
		Dimension currentSize = component.getSize();
		if(!preferedSize.equals(currentSize)) {
			Dimension newSize = new Dimension();
			newSize.width = (int) Math.max(preferedSize.getWidth(),currentSize.getWidth());
			newSize.height = (int) Math.max(preferedSize.getHeight(),currentSize.getHeight());
			component.setSize(newSize);
		}
	}
	
	public static <T extends Component> T makeParentPacking(final T component) {
		component.addComponentListener(new ComponentAdapter() {
        	public void componentResized(ComponentEvent e) {
        		parentPack(component.getParent());
        	}
		});
		return component;
	}

	public static void parentPack(Component parent) {
		if(parent instanceof Frame) {
			((Frame)parent).pack();
		}
		if(parent != null) {
			parentPack(parent.getParent());
		}
		return;
	}
}

