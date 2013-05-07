package eu.brede.graspj.datatypes.fitvariable;

public class SimpleFloatFitVariable extends AbstractFitVariable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private float value;
	private float uncertainty;

	public SimpleFloatFitVariable(float value, float uncertainty) {
		this.value = value;
		this.uncertainty = uncertainty;
	}

	@Override
	public float value() {
		return value;
	}

	@Override
	public float uncertainty() {
		return uncertainty;
	}

	@Override
	public boolean isWithin(int nSigma, double comparedValue) {
		return Math.abs(comparedValue - value()) <= nSigma * uncertainty();
	}

	@Override
	public void setValue(float value) {
		this.value = value;
	}

}
