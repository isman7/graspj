package eu.brede.graspj.gui.configurator;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComponent;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import eu.brede.common.config.EnhancedConfig;

public class ChoiceItem implements ConfigFormItem<EnhancedConfig> {
	
	private JToggleButton toggle;
	private EmbeddedConfigForm configForm;
	DefaultFormBuilder builder;
	
	public ChoiceItem(EnhancedConfig config) {
		super();
		this.toggle = new JRadioButton();//(name);
		this.configForm = new EmbeddedConfigForm(config) {
			@Override
			public void setEnabled(boolean enabled) {
				super.setEnabled(enabled);
//				if(enabled) {
//					this.getGUI().setBackground(Color.LIGHT_GRAY);
//				}
//				else {
//					this.getGUI().setBackground(toggle.getBackground());
////					this.getGUI().setBackground(this.getGUI().getParent().getBackground());
//				}
			};
			
		};
		this.configForm.setEnabled(false);
		
		
		toggle.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                boolean selected = (e.getStateChange()==ItemEvent.SELECTED);
                ChoiceItem.this.configForm.setEnabled(selected);
            }
        });

		initBuilder();
		buildItem();
	}
	
	private void initBuilder() {
//		FormLayout layout = new FormLayout(new ColumnSpec[] {
//				ColumnSpec.decode("left:5dlu"),
//				ColumnSpec.decode("default:grow"),},
//			new RowSpec[] {}); 
		
		FormLayout layout = new FormLayout(
			    "right:max(5dlu;p), FILL:max(80dlu;DEFAULT):GROW(1.0)", // 1st major column
				"");
		
//		builder = new DefaultFormBuilder(layout, new FormDebugPanel());
//		builder = new DefaultFormBuilder(layout, new JXTaskPaneContainer());
		builder = new DefaultFormBuilder(layout);
		
		builder.setDefaultDialogBorder(); //TODO what's that doing?
	}
	
	private void buildItem() {
		builder.appendRow("pref");
		builder.setColumn(1);
        int columnCount = builder.getColumnCount();
        builder.setColumnSpan(columnCount);
		builder.add(getToggle());
		builder.setColumnSpan(1);
		
		if(!configForm.getConfig().isEmpty()) {
			builder.appendRow("pref");
			builder.nextLine();
			builder.setColumn(2);
			builder.append(configForm.getGUI());
		}
	}

	public JToggleButton getToggle() {
		return toggle;
	}
	
	public void setEnabled(boolean enabled) {
		toggle.setEnabled(enabled);
		configForm.setEnabled(enabled);
	}

	@Override
	public EnhancedConfig getValue() {
		return configForm.getValue();
	}

	@Override
	public JComponent getGUI() {
		return builder.getPanel();
	}

	@Override
	public void setLabelText(String labelText) {
		toggle.setText(labelText);
	}

	@Override
	public void setToolTipText(String toolTipText) {
		toggle.setToolTipText(toolTipText);
	}

	@Override
	public void setValue(Object value) {
		configForm.setValue(value);
	}
	
}
