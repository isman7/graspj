package eu.brede.graspj.datatypes.spot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import eu.brede.graspj.datatypes.fitvariable.FloatFitVariable;
import eu.brede.graspj.datatypes.fitvariable.SimpleFloatFitVariable;

public class ArraySpot extends AbstractSpot {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private FloatFitVariable[] fitVariables = new FloatFitVariable[5];

	public ArraySpot(float[] values, float[] uncertainties, int frameNr,
			int spotNr) {
		super(frameNr, spotNr);
		for (int i = 0; i < 5; i++)
			fitVariables[i] = new SimpleFloatFitVariable(values[i],
					uncertainties[i]);
	}

	public ArraySpot(FloatFitVariable[] fitVariables, int frameNr, int spotNr) {
		super(frameNr, spotNr);
		this.fitVariables = fitVariables;
	}

	@Override
	public FloatFitVariable x() {
		return fitVariables[0];
	}

	@Override
	public FloatFitVariable y() {
		return fitVariables[1];
	}

	@Override
	public FloatFitVariable z() {
		return fitVariables[2];
	}

	@Override
	public FloatFitVariable intensity() {
		return fitVariables[3];
	}

	@Override
	public FloatFitVariable background() {
		return fitVariables[4];
	}

	@Override
	public FloatFitVariable getFitVariable(int i) {
		// TODO check for size?
		return fitVariables[i];
	}

	@Override
	public Collection<String> toStringCollection() {
		ArrayList<String> outputStringCollection = new ArrayList<String>();
		for (FloatFitVariable fitVariable : fitVariables) {
			outputStringCollection.addAll(fitVariable.toStringCollection());
		}
		return outputStringCollection;
	}

	@Override
	public Collection<FloatFitVariable> getFitVariables() {

		return Arrays.asList(fitVariables);
	}

}
