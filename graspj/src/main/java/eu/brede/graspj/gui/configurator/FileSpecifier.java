package eu.brede.graspj.gui.configurator;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;

import org.ciscavate.cjwizard.CustomWizardComponent;

import eu.brede.common.gui.FileDrop;

public class FileSpecifier extends JPanel implements ConfigFormItem<File>, CustomWizardComponent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JTextField textField;
	private JButton btnBrowse;
	final JFileChooser chooser = new JFileChooser();

	/**
	 * Create the panel.
	 */
	public FileSpecifier() {

		
		
		textField = new JTextField();
		textField.setColumns(30);
		
//		Border defaultBorder = BorderFactory.createCompoundBorder(
//				BorderFactory.createEmptyBorder(2,2,2,2),
//				textField.getBorder());
//		
//		Border dropBorder = BorderFactory.createCompoundBorder(
//				BorderFactory.createMatteBorder(2, 2, 2, 2, new Color(0f,0f, 1f, 0.25f)),
//				textField.getBorder());
//		
//		textField.setBorder(defaultBorder);
		
		Border dropBorder = 
				new MatteBorder(textField.getBorder().getBorderInsets(textField), 
						new Color(0f,0f, 1f, 0.25f));
		
		
		
		
		
//		new FileDrop( System.out, textField, /*dragBorder,*/ new FileDrop.Listener() {
		new FileDrop( System.out, textField, dropBorder, new FileDrop.Listener() {
			public void filesDropped( java.io.File[] files ) {
				if(files.length>0) {
					textField.setText(files[0].getAbsolutePath());
				}
			}   // end filesDropped
		}); // end FileDrop.Listener
		
//		textField.getDocument().addDocumentListener(new DocumentListener() {
//			public void changedUpdate(DocumentEvent e) {
//				resizeTextField();
//			}
//			public void removeUpdate(DocumentEvent e) {
//				resizeTextField();
//			}
//			public void insertUpdate(DocumentEvent e) {
//				resizeTextField();
//			}
//
//			public void resizeTextField() {
//				int pathLength  = textField.getText().length();
//				pathLength = Math.min(40, pathLength);
//				pathLength = Math.max(pathLength, 20);
//				textField.setColumns(pathLength);
//				FileSpecifier.this.revalidate();
//			}
//		});
		
		add(textField);
		
//		textField.setText(chooser.getSelectedFile().getAbsolutePath());

		btnBrowse = new JButton("Browse");
		btnBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int returnVal = chooser.showDialog(chooser, "Select");
				
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					textField.setText(chooser.getSelectedFile().getAbsolutePath());
				}
			}
		});
		add(btnBrowse);

	}
	
	public FileSpecifier(File file) {
		this();
		setFile(file);
	}
	
	private void setFile(File file) {
		chooser.setSelectedFile(file);
		textField.setText(chooser.getSelectedFile().getAbsolutePath());
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		textField.setEnabled(enabled);
		btnBrowse.setEnabled(enabled);
	}

	@Override
	public File getValue() {
		File file = new File(textField.getText());
		
		chooser.setSelectedFile(file);
		
		// TODO was this better?
//		if(file.isFile()) {
//			chooser.setSelectedFile(file);
//		}
//		else {
//			file = chooser.getSelectedFile();
//			textField.setText(file.getAbsolutePath());
//		}
		return file;
	}

	@Override
	public JComponent getGUI() {
		return this;
	}

	@Override
	public void setLabelText(String labelText) {
		// no label
	}

	@Override
	public void setToolTipText(String text) {
		btnBrowse.setToolTipText(text);
		textField.setToolTipText(text);
		super.setToolTipText(text);
	}

	@Override
	public void setValue(Object value) {
		setFile((File)value);
	}

	
}
