package eu.brede.graspj.gui.configurator;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;

import org.ciscavate.cjwizard.CustomWizardComponent;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.VerticalLayout;

import eu.brede.common.config.EnhancedConfig;
import eu.brede.graspj.datatypes.Choice;

public class ChoiceForm extends JXTaskPaneContainer implements ConfigFormItem<Choice>, CustomWizardComponent {
	
	private Choice choice;
//	private DefaultFormBuilder builder;
	private ButtonGroup buttonGroup;
	private LinkedHashMap<ChoiceItem,String> buttonMap;
	private JXTaskPane panel;
//	private JXTaskPaneContainer container = new JXTaskPaneContainer();
	
	public ChoiceForm(Choice choice) {
		setChoice(choice);
	}
	
	private void setChoice(Choice choice) {
		if(panel!=null) {
			this.remove(panel);
		}
		this.choice = choice;
		buttonMap = new LinkedHashMap<>();
//		panel = new JPanel(new VerticalLayout());
		panel = new JXTaskPane();
		panel.setLayout(new VerticalLayout());
		buttonGroup = new ButtonGroup();
		for(Entry<String,EnhancedConfig> entry : choice.getMap().entrySet()) {
			ChoiceItem choiceItem = new ChoiceItem(entry.getValue());
			choiceItem.setLabelText(entry.getKey());
			buttonGroup.add(choiceItem.getToggle());
			buttonMap.put(choiceItem, entry.getKey());
			panel.add(choiceItem.getGUI());
			if(entry.getKey()==choice.getChosen()) {
				choiceItem.getToggle().setSelected(true);
			}
		}
//		container.add(panel);
//		this.add(container);
		this.add(panel);
	}
	
	public ChoiceForm(Choice option, String title) {
		this(option);
		this.setLabelText(title);
	}
	
//	@SuppressWarnings("serial")
//	private class ValueJXTaskPaneContainer extends JXTaskPaneContainer implements CustomWizardComponent {
//		@Override
//		public Object getValue() {
//			return ChoiceForm.this.getValue();
//		}
//	}
	
//	private void initBuilder() {
////		FormLayout layout = new FormLayout(new ColumnSpec[] {
////				ColumnSpec.decode("left:5dlu"),
////				ColumnSpec.decode("default:grow"),},
////			new RowSpec[] {}); 
//		
//		FormLayout layout = new FormLayout(
//			    "right:max(5dlu;p), FILL:max(80dlu;DEFAULT):GROW(1.0)", // 1st major column
//				"");
//		
////		builder = new DefaultFormBuilder(layout, new FormDebugPanel());
////		builder = new DefaultFormBuilder(layout, new JXTaskPaneContainer());
//		builder = new DefaultFormBuilder(layout);
//		
//		builder.setDefaultDialogBorder(); //TODO what's that doing?
//	}

	@Override
	public Choice getValue() {
		for(Entry<ChoiceItem,String> entry : buttonMap.entrySet()) {
			choice.getMap().put(entry.getValue(), entry.getKey().getValue());
			if(entry.getKey().getToggle().isSelected()) {
				choice.setChosen(entry.getValue());
			}
		}
		return choice;
	}

	@Override
	public JComponent getGUI() {
//		return container;
		return this;
	}

	@Override
	public void setLabelText(String labelText) {
		panel.setTitle(labelText);
	}

	@Override
	public void setToolTipText(String toolTipText) {
		panel.setToolTipText(toolTipText);
	}

	@Override
	public void setEnabled(boolean enabled) {
		panel.setEnabled(enabled);
		for(ChoiceItem optionItem : buttonMap.keySet()) {
			optionItem.setEnabled(enabled);
		}
	}

	@Override
	public void setValue(Object value) {
		// more complex update possible
		setChoice((Choice)value);
	}
	
//	public OptionForm(String name, JToggleButton toggle,
//			ArrayList<Component> components) {
//		super();
//		this.name = name;
//		this.toggle = toggle;
//		this.components = components;
//		
//		toggle.addItemListener(new ItemListener() {
//            @Override
//            public void itemStateChanged(ItemEvent e) {
//                boolean selected = (e.getStateChange()==ItemEvent.SELECTED);
//                OptionForm.this.setEnabled(selected);
//            }
//        });
//
//	}
//
//	public String getName() {
//		return name;
//	}
//
//	public JToggleButton getToggle() {
//		return toggle;
//	}
//
//	public ArrayList<Component> getComponents() {
////		setEnabled(toggle.isEnabled());
//		setEnabled(false);
//		return components;		
//	}
//	
//	public void setEnabled(boolean enable) {
////		toggle.setEnabled(enable);
//		for(Component component : components) {
//			component.setEnabled(enable);
//		}
//	}
	
}
