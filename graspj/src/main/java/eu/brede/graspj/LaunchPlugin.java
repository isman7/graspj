package eu.brede.graspj;

import ij.plugin.PlugIn;

public class LaunchPlugin implements PlugIn {
	
	@Override
	public void run(String arg) {
		LaunchStandalone.main(new String[] {arg});
	}
}
