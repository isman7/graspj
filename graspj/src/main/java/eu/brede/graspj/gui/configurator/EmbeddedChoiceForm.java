package eu.brede.graspj.gui.configurator;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.ciscavate.cjwizard.CustomWizardComponent;
import org.jdesktop.swingx.VerticalLayout;

import eu.brede.common.config.EnhancedConfig;
import eu.brede.graspj.datatypes.Choice;

public class EmbeddedChoiceForm implements ConfigFormItem<Choice>, CustomWizardComponent {
	
	private Choice choice;
//	private DefaultFormBuilder builder;
	private ButtonGroup buttonGroup;
	private LinkedHashMap<ChoiceItem,String> buttonMap;
	private JPanel panel;
	
	public EmbeddedChoiceForm(Choice choice) {
		setChoice(choice);
	}
	
	private void setChoice(Choice choice) {
		this.choice = choice;
		buttonMap = new LinkedHashMap<>();
		panel = new JPanel(new VerticalLayout());
		buttonGroup = new ButtonGroup();
		for(Entry<String,EnhancedConfig> entry : choice.getMap().entrySet()) {
			ChoiceItem optionItem = new ChoiceItem(entry.getValue());
			optionItem.setLabelText(entry.getKey());
			buttonGroup.add(optionItem.getToggle());
			buttonMap.put(optionItem, entry.getKey());
			panel.add(optionItem.getGUI());
		}
	}
	
	public EmbeddedChoiceForm(Choice choice, String title) {
		this(choice);
		this.setLabelText(title);
	}
	
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
		return panel;
	}

	@Override
	public void setLabelText(String labelText) {
//		panel.setTitle(labelText);
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
		setChoice(choice);
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
