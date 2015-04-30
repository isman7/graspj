package eu.brede.graspj;

import ij.plugin.PlugIn;
//import fiji.Debug;

/*import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.*;*/

public class LaunchPlugin implements PlugIn {
	
	@Override
	public void run(String arg) {
		LaunchStandalone.main(new String[] {arg});
		//IJ.showMessage("My_Plugin","Hello world!");
	}
}
