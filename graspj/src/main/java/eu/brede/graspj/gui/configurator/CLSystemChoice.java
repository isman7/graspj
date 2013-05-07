package eu.brede.graspj.gui.configurator;

import javax.swing.JComponent;

import eu.brede.common.config.Configurable;
import eu.brede.common.opencl.utils.CLSystem;
import eu.brede.graspj.opencl.utils.CLSystemGJ;

public class CLSystemChoice implements ConfigFormItem<CLSystem> {
	
	private ObjectChoiceItem<Configurable> oci;
	
	public CLSystemChoice(CLSystem cl) {
		super();
		oci = new ObjectChoiceItem<>(CLSystemGJ.createObjectChoice(cl));
	}

	@Override
	public CLSystem getValue() {
//		Selection<String> deviceSelection = oci.getValue().getChosen()
//				.getConfig().gett("devices");
//		if (deviceSelection.getSelected().isEmpty()) {
//			oci.setValue(CLSystemGJ.createObjectChoice(CLSystemGJ
//					.createWithSimpleGuess()));
//		}
		return new CLSystemGJ(oci.getValue());
	}

	@Override
	public JComponent getGUI() {
		return oci;
	}

	@Override
	public void setLabelText(String labelText) {
		oci.setLabelText(labelText);
	}

	@Override
	public void setToolTipText(String toolTipText) {
		oci.setToolTipText(toolTipText);
	}

	@Override
	public void setEnabled(boolean enabled) {
		oci.setEnabled(enabled);
	}

	@Override
	public void setValue(Object value) {
		CLSystem cl = (CLSystem) value;
		oci.setValue(CLSystemGJ.createObjectChoice(cl));
	}
	

	
}
