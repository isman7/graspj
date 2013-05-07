package eu.brede.graspj.gui.views;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.commons.lang.StringUtils;

import eu.brede.graspj.configs.Global;
import eu.brede.graspj.datatypes.CommandRunner;

public class CmdMenu extends JMenu {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private CommandRunner cmdRunner;
	
	private String identifier; 
	
	public CmdMenu(CommandRunner cmdRunner, String name) {
		this.cmdRunner = cmdRunner;
		this.identifier = 
				StringUtils.join(new String[] {
					cmdRunner.getClass().getSimpleName(),
					name
				}, ".");
		setText(Global.getStringfromBundle("MenuLabels", identifier));
	}
	
	public CmdMenu(CommandRunner cmdRunner) {
		this(cmdRunner, CmdMenu.class.getSimpleName());
	}
	
//	public CmdMenu(View view, String... menuCmds) {
//		this(view);
//		for(String cmd : menuCmds) {
//			addCmdMenuItem(cmd);
//		}
//	}

	public void addCmdMenuItem(final String cmd) {
		
		String itemId = StringUtils.join(new String[] {this.identifier, cmd}, ".");
		
		JMenuItem menuItem = new JMenuItem(Global.getStringfromBundle("MenuLabels", itemId));
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cmdRunner.runCommand(cmd);
			}
		});
		add(menuItem);
	}

	public void addSubmenu(JMenu menu) {
		String itemId = StringUtils.join(
				new String[] {this.identifier, menu.getText()}, ".");
		menu.setText(Global.getStringfromBundle("MenuLabels", itemId));
		add(menu);
	}
}
