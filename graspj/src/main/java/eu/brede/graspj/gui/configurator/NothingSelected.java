package eu.brede.graspj.gui.configurator;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import eu.brede.common.util.AutoPackAdapter;
import eu.brede.common.util.AutoPackAdapter.AutoPackType;
import eu.brede.graspj.configs.Global;

public class NothingSelected extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NothingSelected() {
		setLayout(new BorderLayout(0, 0));

		JLabel lblNotConfigurable = new JLabel(Global.getStringfromBundle(
				"Messages", "WorkflowEditor.NothingSelected.text"));

		lblNotConfigurable.setHorizontalAlignment(SwingConstants.CENTER);
		add(lblNotConfigurable);
		setVisible(true);
		// setBorder(BorderFactory.createLineBorder(Color.red));
		setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		this.addComponentListener(new AutoPackAdapter(this, AutoPackType.ALL));
	}
}
