package eu.brede.graspj.gui;

import java.awt.Color;
import java.awt.Toolkit;

import javax.swing.plaf.ColorUIResource;

import org.jdesktop.swingx.plaf.AbstractComponentAddon;
import org.jdesktop.swingx.plaf.DefaultsList;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;

public class W7TaskPaneAddon extends AbstractComponentAddon {

	protected W7TaskPaneAddon() {
		super("JXTaskPane");
	}

	@Override
	protected void addWindowsDefaults(LookAndFeelAddons addon,
			DefaultsList defaults) {

		super.addWindowsDefaults(addon, defaults);
        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        
        defaults.add("TaskPane.foreground", new ColorUIResource(Color.WHITE));
        defaults.add("TaskPane.background",
                new ColorUIResource((Color)toolkit.getDesktopProperty("win.3d.backgroundColor")));
        defaults.add("TaskPane.specialTitleBackground", new ColorUIResource(33, 89, 201));
        defaults.add("TaskPane.titleBackgroundGradientStart", new ColorUIResource(Color.WHITE));
        defaults.add("TaskPane.titleBackgroundGradientEnd",
                new ColorUIResource((Color)toolkit.getDesktopProperty("win.frame.inactiveCaptionColor")));
        defaults.add("TaskPane.titleForeground",
                new ColorUIResource((Color)toolkit.getDesktopProperty("win.frame.inactiveCaptionTextColor")));
        defaults.add("TaskPane.specialTitleForeground", new ColorUIResource(Color.WHITE));
        defaults.add("TaskPane.borderColor", new ColorUIResource(Color.WHITE));
	}
	
}
