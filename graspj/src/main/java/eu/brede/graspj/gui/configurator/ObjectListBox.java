package eu.brede.graspj.gui.configurator;

import ij.IJ;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPopupMenu;

import org.apache.commons.lang.StringUtils;

import eu.brede.graspj.datatypes.CommandRunner;
import eu.brede.graspj.gui.views.CmdMenu;
import eu.brede.graspj.configs.Global;

public class ObjectListBox<T> implements ConfigFormItem<Map<String,T>>, CommandRunner {
	
	protected JList<String> list;
	protected DefaultListModel<String> listModel;
	protected Map<String,T> map;
	protected CmdMenu cmdMenu = new CmdMenu(this);
	
	
	public ObjectListBox(Map<String,T> map) {
		this.map = map;
		this.listModel = new DefaultListModel<>();
		for(String key : map.keySet()) {
			listModel.addElement(key);
		}
		this.list = new JList<String>(listModel);
		initMenu();
		rightClick();
	}
	
	@Override
	public Map<String,T> getValue() {
		HashMap<String,T> tempMap = new HashMap<>(map);
		map.clear();
		for(int i=0; i<list.getModel().getSize(); i++) {
			String key = list.getModel().getElementAt(i);
			// TODO check if tempMap contains key?
			map.put(key, tempMap.get(key));
		}
		return map;
	}

	@Override
	public void setLabelText(String labelStr) {
		//comboBox.setText(labelStr);
	}

	@Override
	public void setToolTipText(String text) {
		list.setToolTipText(text);
	}

	@Override
	public JComponent getGUI() {
		return list;
	}

	@Override
	public void setEnabled(boolean enabled) {
		list.setEnabled(enabled);
	}
	
	protected void initMenu() {
		cmdMenu.addCmdMenuItem("Remove");
		cmdMenu.addCmdMenuItem("Rename");
	}

	@Override
	public void runCommand(String cmd) {
		switch(cmd) {
			case "Remove":
				for(String key : list.getSelectedValuesList()) {
					map.remove(key);
				}
				int[] indices = list.getSelectedIndices();
				for(int i=indices.length; --i >= 0;) {
					listModel.remove(i);
				}
				break;
			case "Rename":
				for(String oldName : list.getSelectedValuesList()) {
					String questionBundleKey = StringUtils.join(new String[] {
							getClass().getSimpleName(),
							cmdMenu.getClass().getSimpleName(),
							"Rename", "message"
					}, "."); 

						String newName = IJ.getString(
								Global.getStringfromBundle("MenuLabels", questionBundleKey),
								oldName);
					map.put(newName,map.remove(oldName));
					listModel.removeElement(oldName);
					listModel.addElement(newName);
				}
				break;
		}
	}
	
	private void rightClick() {
		list.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent e) {
				super.mouseReleased(e);
				
				if(e.getButton()==3) {
					showMenu(cmdMenu.getPopupMenu(),e);					
				}
				
			}
			
			private void showMenu(JPopupMenu popup, MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
			
		});
	}

	@Override
	public void setValue(Object value) {
		map = (Map<String, T>) value;
		listModel.removeAllElements();
		for(String key : map.keySet()) {
			listModel.addElement(key);
		}
	}

}
