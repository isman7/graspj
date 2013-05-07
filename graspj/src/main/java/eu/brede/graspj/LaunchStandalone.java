package eu.brede.graspj;

import com.jdotsoft.jarloader.JarClassLoader;

public class LaunchStandalone {
	
	public static void main(String[] args) {
		JarClassLoader jcl = new JarClassLoader();
        try {
            jcl.invokeMain("eu.brede.graspj.gui.GraspJ", args);
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
	}
}
