package eu.brede.graspj.gui.configurator;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JLabel;

import eu.brede.common.util.AutoPackAdapter;
import eu.brede.common.util.AutoPackAdapter.AutoPackType;
import eu.brede.common.util.StringTools;
import java.awt.BorderLayout;

import javax.swing.SwingConstants;

public class NotConfigurable extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JLabel lblNotConfigurable = new JLabel("/*${className}*/ is not configurable");
	
	private NotConfigurable() {
		setLayout(new BorderLayout(0, 0));
		lblNotConfigurable.setHorizontalAlignment(SwingConstants.CENTER);
		add(lblNotConfigurable);
		setVisible(true);
//		setBorder(BorderFactory.createLineBorder(Color.red));
		setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
		this.addComponentListener(new AutoPackAdapter(this,AutoPackType.ALL));
	}
	
	public NotConfigurable(String replacement) {
		this();
		lblNotConfigurable.setText(StringTools.replaceToken(lblNotConfigurable.getText(),
				"className", replacement));
		this.setPreferredSize(getPreferredSize());
		this.repaint();
	}
	
	public NotConfigurable(Object object) {
		this(object.getClass().toString());
	}
}
