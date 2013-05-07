package eu.brede.graspj.gui.views;

import javax.swing.JPanel;

import eu.brede.graspj.datatypes.CommandRunner;

public abstract class View extends JPanel implements CommandRunner {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ViewManager manager;
	private CmdMenu viewMenu;// = new ViewMenu();//(this.getClass().getSimpleName());

	public View(ViewManager manager) {
		this();
		this.manager = manager;	
	}
	
	public View() {
		super();
		this.viewMenu = new CmdMenu(this);
	}

	public ViewManager getManager() {
		return manager;
	}

	public void setManager(ViewManager manager) {
		this.manager = manager;
	}
	
	public void dispose() {
		// Do nothing
	}
	
	public void suspend() {
		// Do nothing
	}
	
	public void resume() {
		// Do nothing
	}
	
	public void refresh() {
		// Do nothing
	}
	
	public CmdMenu getViewMenu() {
		return viewMenu;
	}

	public void setViewMenu(CmdMenu viewMenu) {
		this.viewMenu = viewMenu;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

	@Override
	public void runCommand(String cmd) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void finalize() throws Throwable {
		dispose();
		super.finalize();
	}
	
	

}
