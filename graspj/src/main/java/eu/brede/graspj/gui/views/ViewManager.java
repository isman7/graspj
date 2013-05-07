package eu.brede.graspj.gui.views;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;
import java.util.zip.DataFormatException;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import eu.brede.common.gui.FileDrop;
import eu.brede.common.io.easy.EasyLoadable;
import eu.brede.common.io.easy.EasySavable;
import eu.brede.common.io.easy.ExtFileChooser;
import eu.brede.common.util.ExceptionTools.OperationCanceledException;
import eu.brede.graspj.datatypes.CommandRunner;

public class ViewManager implements CommandRunner {
	private JFrame displayFrame;
	private JPanel empty;
	private View displayedView;
	private Stack<View> availableViews = new Stack<>();
	private CmdMenu viewsMenu = new CmdMenu(this);
//	private JMenuItem closeCurrentMenu;
//	private JMenuItem closeAllMenu;
	private Set<Class<? extends View>> supportedViews = new HashSet<>(); 
	private ExtFileChooser fileChooser;
	private ViewPanel panel;

	public ViewManager(JFrame displayFrame) {
		super();
		this.displayFrame = displayFrame;
		panel = new ViewPanel();
		displayFrame.setContentPane(panel);
		fileChooser = new ExtFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(false);
//		initCloseCurrentMenu();
//		initCloseAllMenu();
		empty = new JPanel();
//		empty.setBounds(0, 0, 450, 300);
		empty.setPreferredSize(new Dimension(450, 300));
		panel.setContent(empty);
	}
	
	private class ViewPanel extends JPanel {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public ViewPanel() {
			setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
			new FileDrop( System.out, this, /*dragBorder,*/ new FileDrop.Listener() {
				public void filesDropped( java.io.File[] files ) {
					for( int i = 0; i < files.length; i++ ) {
						try {
							ViewManager.this.open(files[i]);
						} catch (DataFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}   // end for: through each dropped file
				}   // end filesDropped
			}); // end FileDrop.Listener
		}
		
		public void setContent(JPanel content) {
			this.removeAll();
			this.add(content);
		}
	}

    public View getDisplayedView() {
        return displayedView;
    }
	
	public void switchView(View newView, SwitchType switchType) {
		// if newView is already displayed, do nothing
		if(displayedView == newView) {
			return;
		}
		
		
		if(displayedView != null) {
			displayFrame.getJMenuBar().remove(displayedView.getViewMenu());
			switch(switchType) {
				case SUSPEND:
					displayedView.suspend();
					availableViews.push(displayedView);
					break;
				case DISPOSE:
					displayedView.dispose();
					displayedView = null;
					break;
			}
		}
		
		if(newView != null) {
			if(!availableViews.remove(newView)) {
				newView.setManager(this);
			}
//			displayFrame.setContentPane(newView);
			panel.setContent(newView);
			displayFrame.getJMenuBar().add(newView.getViewMenu());
		}
		else {
			panel.setContent(empty);
		}
		// manage available views stack
		
		
		// display newView and its menu and call pack to resize
		displayedView = newView;
		updateMenu();
		displayFrame.pack();
		
	}
	
	public ArrayList<View> getAllViews() {
		ArrayList<View> views = new ArrayList<>(availableViews);
		views.add(displayedView);
		return views;
	}
	
	public void switchView(View newView) {
		switchView(newView, SwitchType.SUSPEND);
	}
	
	public JFrame getDisplayFrame() {
		return displayFrame;
	}
	
	private void updateMenu() {
		viewsMenu.removeAll();
		if(displayedView!=null) {
			ViewMenuItem displayedViewMenuItem =  new ViewMenuItem(displayedView);
			displayedViewMenuItem.setText("*" + displayedViewMenuItem.getText());
			displayedViewMenuItem.setEnabled(false);
			viewsMenu.add(displayedViewMenuItem);
//			closeCurrentMenu.setEnabled(true);
//			closeAllMenu.setEnabled(true);
		}
//		else {
//			closeCurrentMenu.setEnabled(false);
//			closeAllMenu.setEnabled(false);
//		}
		Iterator<View> it = availableViews.iterator();
		while(it.hasNext()) {
			viewsMenu.add(new ViewMenuItem(it.next()));
//			closeAllMenu.setEnabled(true);
		}
		viewsMenu.add(new JSeparator());
//		viewsMenu.add(closeCurrentMenu);
//		viewsMenu.add(closeAllMenu);
		viewsMenu.addCmdMenuItem("closeCurrent");
		viewsMenu.addCmdMenuItem("closeAll");
	}
	
	public enum SwitchType {
		SUSPEND, DISPOSE;
	}
	
	private class ViewMenuItem extends JMenuItem {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private View view;

		public ViewMenuItem(View view) {
			super();
			this.view = view;
			setText(view.toString());
			init();
		}
		
		private void init() {
			addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					view.getManager().switchView(view);
				}
			});
		}
	}
	
	public void viewHasBeenClosed(View view) {
		if(view==displayedView) {
			closeCurrent();
		}
		else {
			availableViews.remove(view);
		}
		updateMenu();
	}
	
	public void closeCurrent() {
		if(availableViews.size()>0) {
			switchView(availableViews.get(0),SwitchType.DISPOSE);
		}
		else {
			switchView(null,SwitchType.DISPOSE);
		}
	}
	
	public void closeAll() {
		for(View view : availableViews) {
			view.dispose();
		}
		availableViews.clear();
		switchView(null,SwitchType.DISPOSE);
	}
	
//	private void initCloseCurrentMenu() {
//		closeCurrentMenu = new JMenuItem("close current");
//		closeCurrentMenu.addActionListener(new ActionListener() {
//			
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				closeCurrent();
//			}
//		});
//	}
//	
//	private void initCloseAllMenu() {
//		closeAllMenu = new JMenuItem("close all");
//		closeAllMenu.addActionListener(new ActionListener() {
//			
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				closeAll();
//			}
//		});
//	}
	
	public JMenu getViewsMenu() {
		return viewsMenu;
	}
	
	public Set<Class<? extends View>> getSupportedViews() {
		return supportedViews;
	}

	@Override
	public void runCommand(String cmd) {
		switch(cmd) {
			case "closeCurrent":
				closeCurrent();
				break;
			case "closeAll":
				closeAll();
				break;
		}
		
	}

	public ExtFileChooser getFileChooser() {
		return fileChooser;
	}

	public void setFileChooser(ExtFileChooser fileChooser) {
		this.fileChooser = fileChooser;
	}
	
	public <V extends View> void openDialog(V view) throws ClassNotFoundException,
		IOException, ClassCastException, OperationCanceledException {
			((EasyLoadable<?>) view).getEasyLoader().easyLoadDialog(fileChooser);
			switchView(view);
	}
	
	public void openDialog() throws ClassNotFoundException,
	IOException, ClassCastException {
		throw new Error("missing implementation");
	}	
	
	public void open(File file) throws DataFormatException {
		for(Class<? extends View> clazz : getSupportedViews()) {
			if(EasyLoadable.class.isAssignableFrom(clazz)) {
				try {
					View view = clazz.newInstance();
					EasyLoadable<?> el = (EasyLoadable<?>) view;
					el.getEasyLoader().easyLoad(file);
					switchView(view);
					return;
				} catch (InstantiationException | IllegalAccessException 
						| ClassNotFoundException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		throw new DataFormatException(file.getName());
	}
	
	// TODO throw NotEasySavableException
	public void saveDialog() throws FileNotFoundException, IOException, OperationCanceledException {
		((EasySavable) displayedView).getEasySaver().showSaveDialog(fileChooser);
	}
	
	

}
