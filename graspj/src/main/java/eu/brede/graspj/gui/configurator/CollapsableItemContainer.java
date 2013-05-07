package eu.brede.graspj.gui.configurator;

/**
 * Inspired by ConsoleConfigPanel from andrebossard.com
 */

import javax.swing.JPanel;

import org.jdesktop.swingx.JXTaskPane;

/**
 * Stub for displaying Configuration item.
 *
 * @author Norman
 *
 */

// TODO allow for display modification (add a row map?)

public abstract class CollapsableItemContainer extends ItemContainer {

//	private boolean childCollapsed = true;
//	private JXTaskPaneContainer container;
	private JXTaskPane taskPane;

// TODO implement more constructors! Read labels&tooltips from properties?
	
	public CollapsableItemContainer() {
		super();
//		initBuilder();
//		container = new JXTaskPaneContainer();
//		container.setBorder(BorderFactory.createEmptyBorder());
//		taskPane = new JXTaskPane("container");
		taskPane = new JXTaskPane();
//		taskPane.setLayout(new VerticalLayout());
		taskPane.setAnimated(false);
		taskPane.add(builder.getPanel());
//		container.add(builder.getPanel());
//		container.add(taskPane);
//		this.add(container);
		this.add(taskPane);
	}
	
	
	
//	@SuppressWarnings("serial")
//	private class ValueJXTaskPaneContainer extends JXTaskPaneContainer implements CustomWizardComponent {
//		@Override
//		public Object getValue() {
//			return CollapsableItemContainer.this.getValue();
//		}
//	}
	
	@Override
	protected JPanel initPanel() {
		return new JPanel();
	}



	public CollapsableItemContainer(boolean collapsed) {
		this();
//		((JXTaskPane)builder.getPanel()).setCollapsed(collapsed);
		taskPane.setCollapsed(collapsed);
	}
	
	
	
//	@Override
//	protected JPanel initPanel() {
//		JXTaskPane taskPane = new JXTaskPane("Container");
//		taskPane.setTitle("testest");
//		taskPane.setAnimated(false);
//		return taskPane;
//	}
	
//	public JComponent getGUI() {
////		this.setContentPane(builder.getPanel());
////		this.add(taskPane);
////		return builder.getPanel();
//		return container;
//	}

	@Override
	public void setLabelText(String label) {
//		((JXTaskPane)builder.getPanel()).setTitle(label);
		taskPane.setTitle(label);
	}
	
	public void setCollapsed(boolean collapsed) {
		taskPane.setCollapsed(collapsed);
	}

}