package eu.brede.graspj.gui.configurator;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

import org.jdesktop.swingx.VerticalLayout;

import eu.brede.common.config.Configurable;
import eu.brede.common.util.GUITools;
import eu.brede.graspj.datatypes.ObjectChoice;

public class ObjectChoiceItem<T> extends JPanel implements ConfigFormItem<ObjectChoice<T>> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JComboBox<T> comboBox;
	private EmbeddedConfigForm configForm;
	private ObjectChoice<T> choice;
//	JPanel panel;
	
	public ObjectChoiceItem(ObjectChoice<T> objectChoice) {
		super(new VerticalLayout());
//		panel = new JPanel(new VerticalLayout());
//		panel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		setChoice(objectChoice);
		

	}
	
	private void setChoice(ObjectChoice<T> objectChoice) {
		this.choice = objectChoice;
		this.comboBox = new JComboBox<T>(choice.getChoices());
		
		if(choice.getChosen()!=null) {
			comboBox.setSelectedItem(choice.getChosen());
		}
		else {			
			comboBox.setSelectedIndex(-1);
		}
		
		add(comboBox);
		updateChoice();
		
		comboBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				updateChoice();
			}
        });
	}
	
	public void setEnabled(boolean enabled) {
		comboBox.setEnabled(enabled);
		if(configForm!=null) {
			configForm.setEnabled(enabled);
		}
	}

	@Override
	public ObjectChoice<T> getValue() {
		updateChosenConfig();
		return choice;
	}
	
	private void updateChosenConfig() {
		if(configForm!=null) {
			((Configurable)choice.getChosen()).setConfig(configForm.getConfig());
		}
	}
	
	private void updateChoice() {
		if(configForm!=null) {
			remove(configForm);
			updateChosenConfig();
			configForm=null;
		}
		choice.setChosen((T)comboBox.getSelectedItem());
		if(choice.getChosen() instanceof Configurable) {
			Configurable configurable = (Configurable) choice.getChosen();
			configForm = new EmbeddedConfigForm(configurable);
			Border dlgBorder = ((JPanel)configForm).getBorder();
			((JPanel)configForm.getGUI()).setBorder(new CompoundBorder( 
					BorderFactory.createLineBorder(Color.GRAY),
					dlgBorder));
			add(configForm);
			GUITools.parentPack(ObjectChoiceItem.this);
//			ObjectChoiceItem.this.repaint();
		}
	}

	@Override
	public JComponent getGUI() {
		return this;
	}

	@Override
	public void setLabelText(String labelText) {
//		panel.setText(labelText);
	}

	@Override
	public void setToolTipText(String toolTipText) {
		super.setToolTipText(toolTipText);
	}

	@Override
	public void setValue(Object value) {
		setChoice((ObjectChoice<T>) value);
	}
	
}
