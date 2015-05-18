package eu.brede.graspj;

import ij.plugin.PlugIn;
//import ij.IJ;
//import fiji.Debug;

/*import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.*;*/

public class LaunchPlugin implements PlugIn {
	
	@Override
	public void run(String arg) {
		//Debug.run("GraspJ", "");
		//IJ.error("Welcome to Image J");
		LaunchStandalone.main(new String[] {arg});
		//IJ.showMessage("LaunchPlugin","Hello world!");
	}
}



