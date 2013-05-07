package eu.brede.common.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;

public class DebugTools {
	public static void recursivePreferedSize(Container container) {
		for(Component component : container.getComponents()) {
			Dimension dim = component.getPreferredSize();
			System.out.println(component.getClass().getSimpleName() + " prefers [width="
					+ dim.width + ", height=" + dim.height + "]");
			if(component instanceof Container) {
				recursivePreferedSize((Container)component);
			}
		}
	}
	
	public static <T> T print(T object) {
		System.out.println(object);
		return object;
	}
}
