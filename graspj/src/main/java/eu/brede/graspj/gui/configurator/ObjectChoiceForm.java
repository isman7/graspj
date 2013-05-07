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

import eu.brede.common.config.Configurable;
import eu.brede.common.config.EnhancedConfig;
import eu.brede.graspj.datatypes.ObjectChoice;

public class ObjectChoiceForm<T> extends JXTaskPaneContainer 
	implements ConfigFormItem<ObjectChoice<T>>, CustomWizardComponent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ObjectChoice<T> choice;
//	private DefaultFormBuilder builder;
	private ButtonGroup buttonGroup;
	private LinkedHashMap<T,ChoiceItem> buttonMap;
	private JXTaskPane panel;
//	private JXTaskPaneContainer container = new JXTaskPaneContainer();
	
	public ObjectChoiceForm(ObjectChoice<T> choice) {
//		setBorder(BorderFactory.createLineBorder(Color.BLUE));
//		panel = new JPanel(new VerticalLayout());
		panel = new JXTaskPane();
		panel.setLayout(new VerticalLayout());
		
		setChoice(choice);
		
//		container.add(panel);
//		this.add(container);
		this.add(panel);
	}
	
	private void setChoice(ObjectChoice<T> choice) {
		panel.removeAll();
		this.choice = choice;
		buttonMap = new LinkedHashMap<>();
//		panel = new JPanel(new VerticalLayout());
		buttonGroup = new ButtonGroup();
		for(T object : choice.getChoices()) {
			
			EnhancedConfig config = null;
			
			if(object instanceof Configurable) {
				config = ((Configurable) object).getConfig();
			}
			else {
				config = new EnhancedConfig();
			}
			
			ChoiceItem choiceItem = new ChoiceItem(config);
			choiceItem.setLabelText(object.toString());
			buttonGroup.add(choiceItem.getToggle());
			buttonMap.put(object,choiceItem);
			
			panel.add(choiceItem.getGUI());
			if(object==choice.getChosen()) {
				choiceItem.getToggle().setSelected(true);
			}
		}
	}
	
//	public ObjectChoiceForm(Choice option, String title) {
//		this(option);
//		this.setLabelText(title);
//	}
	
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
	public ObjectChoice<T> getValue() {
		for(Entry<T,ChoiceItem> entry : buttonMap.entrySet()) {
			T object = entry.getKey();
			ChoiceItem choiceItem = entry.getValue();
			
			if(object instanceof Configurable) {
				((Configurable) object).setConfig(choiceItem.getValue());
			}
			
			if(choiceItem.getToggle().isSelected()) {
				choice.setChosen(object);
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
		for(ChoiceItem optionItem : buttonMap.values()) {
			optionItem.setEnabled(enabled);
		}
	}

	@Override
	public void setValue(Object value) {
		setChoice((ObjectChoice<T>) value);
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
