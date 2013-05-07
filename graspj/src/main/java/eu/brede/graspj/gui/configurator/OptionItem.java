package eu.brede.graspj.gui.configurator;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

import org.ciscavate.cjwizard.CustomWizardComponent;
import org.jdesktop.swingx.VerticalLayout;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import eu.brede.common.util.GUITools;
import eu.brede.graspj.datatypes.Option;

public class OptionItem implements ConfigFormItem<Option>, CustomWizardComponent {
	
	private JCheckBox checkBox;
	private EmbeddedConfigForm configForm;
//	private ConfigForm configForm;
//	DefaultFormBuilder builder;
	private Option option;
	private JPanel panel = GUITools.makeParentPacking(new JPanel());
	
	public OptionItem(Option option) {
		super();
		this.option = option;
		this.configForm = new EmbeddedConfigForm(option);
		this.configForm.setEnabled(false);
		this.configForm.getGUI().setVisible(false);
		Border dlgBorder = ((JPanel)configForm.getGUI()).getBorder();
		((JPanel)configForm.getGUI()).setBorder(new CompoundBorder( 
				BorderFactory.createLineBorder(Color.GRAY),
				dlgBorder));
		
		
//		checkBox.addItemListener(new ItemListener() {
//            @Override
//            public void itemStateChanged(ItemEvent e) {
//                boolean selected = (e.getStateChange()==ItemEvent.SELECTED);
//                OptionItem.this.configForm.setEnabled(selected);
//            }
//        });
		
		this.checkBox = new JCheckBox();//(name);
		this.checkBox.setSelected(option.isSelected());
		
//		this.configForm = new EmbeddedConfigForm(option) {
//		this.configForm = new ConfigForm(option) {
//			@Override
//			public void setEnabled(boolean enabled) {
//				super.setEnabled(enabled);
//				if(enabled) {
//					this.getGUI().setBackground(Color.WHITE);
//				}
//				else {
//					this.getGUI().setBackground(null);
//				}
//			};
//			
//		};
		
		ItemListener itemListener = new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean enabled = checkBox.isSelected();
				configForm.setEnabled(enabled);
//				configForm.setCollapsed(!enabled);
				if(!configForm.getValue().isEmpty()) {
					configForm.getGUI().setVisible(enabled);
				}
			}
		};

		this.checkBox.addItemListener(itemListener);
		
		itemListener.itemStateChanged(null);
		
//		initBuilder();
//		buildItem();
		panel.setLayout(new VerticalLayout());
		panel.add(getCheckBox());
		panel.add(configForm.getGUI());
	}
	
//	private void initBuilder() {
////		FormLayout layout = new FormLayout(new ColumnSpec[] {
////				ColumnSpec.decode("left:5dlu"),
////				ColumnSpec.decode("default:grow"),},
////			new RowSpec[] {}); 
//		
//		FormLayout layout = new FormLayout(
//			    "left:max(5dlu;p), 1dlu, FILL:max(80dlu;DEFAULT):GROW(1.0)", // 1st major column
////			    "right:max(5dlu;p), FILL:max(80dlu;DEFAULT):GROW(1.0)", // 1st major column
//				"top:pref, top:pref, top:pref");
//		
////		builder = new DefaultFormBuilder(layout, new FormDebugPanel());
////		builder = new DefaultFormBuilder(layout, new JXTaskPaneContainer());
//		builder = new DefaultFormBuilder(layout);
//		
////		builder.setDefaultDialogBorder(); //TODO what's that doing?
//		builder.setBorder(BorderFactory.createEmptyBorder());
//	}
//	
//	private void buildItem() {
////		builder.appendRow("pref");
////		builder.setColumn(1);
////        int columnCount = builder.getColumnCount();
////        builder.setColumnSpan(columnCount);
////		builder.add(getCheckBox());
////		builder.setColumnSpan(1);
////
////		builder.appendRow("pref");
////		builder.nextLine();
////		builder.setColumn(2);
////		builder.append(configForm.getGUI());
////		builder.append(getCheckBox(),configForm.getGUI());
//		builder.append(getCheckBox());
//		builder.nextLine();
//		builder.append(configForm.getGUI());
//		builder.nextLine();
//	}

	public JCheckBox getCheckBox() {
		return checkBox;
	}
	
	public void setEnabled(boolean enabled) {
		checkBox.setEnabled(enabled);
		configForm.setEnabled(enabled);
//		configForm.getGUI().setVisible(enabled);
//		configForm.setCollapsed(!enabled);
	}

	@Override
	public Option getValue() {
//		Option option = new Option();
		option.putAll(configForm.getValue());
		option.setSelected(checkBox.isSelected());
		return option;
	}

	@Override
	public JComponent getGUI() {
		return panel;
	}

	@Override
	public void setLabelText(String labelText) {
		configForm.setLabelText(labelText);
	}

	@Override
	public void setToolTipText(String toolTipText) {
		checkBox.setToolTipText(toolTipText);
	}

	@Override
	public void setValue(Object value) {
		Option option = (Option) value;
		this.checkBox.setSelected(option.isSelected());
		configForm.setValue(value);
	}
	
}
