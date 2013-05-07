package eu.brede.graspj.datatypes.spot;

import java.util.ArrayList;
import java.util.Collection;

import eu.brede.graspj.datatypes.fitvariable.FloatFitVariable;
import eu.brede.graspj.datatypes.fitvariable.SimpleFloatFitVariable;

public class SimpleSpot extends AbstractSpot {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected FloatFitVariable x, y, z, intensity, background;
	transient private int fitDimension = 5;

	public SimpleSpot(float[] spotArray, int frameNr, int spotNr) {
		super(frameNr, spotNr);
		this.x = new SimpleFloatFitVariable(spotArray[0],
				spotArray[0 + fitDimension]);
		this.y = new SimpleFloatFitVariable(spotArray[1],
				spotArray[1 + fitDimension]);
		this.z = new SimpleFloatFitVariable(spotArray[2],
				spotArray[2 + fitDimension]);
		this.intensity = new SimpleFloatFitVariable(spotArray[3],
				spotArray[3 + fitDimension]);
		this.background = new SimpleFloatFitVariable(spotArray[4],
				spotArray[4 + fitDimension]);
	}

	public SimpleSpot(float[] values, float[] uncertainties, int frameNr,
			int spotNr) {
		super(frameNr, spotNr);
		this.x = new SimpleFloatFitVariable(values[0], uncertainties[0]);
		this.y = new SimpleFloatFitVariable(values[1], uncertainties[1]);
		this.z = new SimpleFloatFitVariable(values[2], uncertainties[2]);
		this.intensity = new SimpleFloatFitVariable(values[3], uncertainties[3]);
		this.background = new SimpleFloatFitVariable(values[4],
				uncertainties[4]);
	}

	public SimpleSpot(FloatFitVariable[] fit_variables, int frameNr, int spotNr) {
		super(frameNr, spotNr);
		this.x = fit_variables[0];
		this.y = fit_variables[1];
		this.z = fit_variables[2];
		this.intensity = fit_variables[3];
		this.background = fit_variables[4];
	}

	public SimpleSpot(Spot spot) {
		super(spot.frameNr(), spot.spotNr());
		this.x = spot.x();
		this.y = spot.y();
		this.z = spot.z();
		this.intensity = spot.intensity();
		this.background = spot.background();
	}

	@Override
	public FloatFitVariable x() {
		return x;
	}

	@Override
	public FloatFitVariable y() {
		return y;
	}

	@Override
	public FloatFitVariable z() {
		return z;
	}

	@Override
	public FloatFitVariable intensity() {
		return intensity;
	}

	@Override
	public FloatFitVariable background() {
		return background;
	}

	@Override
	public FloatFitVariable getFitVariable(int i) {
		FloatFitVariable returnFitVariable;
		switch (i) {
		case 0:
			returnFitVariable = x();
			break;
		case 1:
			returnFitVariable = y();
			break;
		case 2:
			returnFitVariable = z();
			break;
		case 3:
			returnFitVariable = intensity();
			break;
		case 4:
			returnFitVariable = background();
			break;
		default:
			returnFitVariable = null;
			break;
		}
		return returnFitVariable;
	}

	public int getNumFitVariables() {
		return 5;
	}

	@Override
	public Collection<FloatFitVariable> getFitVariables() {
		ArrayList<FloatFitVariable> fitVariables = new ArrayList<FloatFitVariable>();
		for (int i = 0; i < getNumFitVariables(); i++)
			fitVariables.add(getFitVariable(i));
		return fitVariables;
	}

}
