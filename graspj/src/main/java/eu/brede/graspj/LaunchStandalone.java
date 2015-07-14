package eu.brede.graspj;

import ij.plugin.PlugIn;

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


/*public class LaunchStandalone implements PlugIn {
	
	@Override
	public void run(String arg) {
		
		JarClassLoader jcl = new JarClassLoader();
        try {
            jcl.invokeMain("eu.brede.graspj.gui.GraspJ", new String[] {arg});
        }
        catch (Throwable e) {
           e.printStackTrace();
        }
		
		
		
	}
}*/
 
