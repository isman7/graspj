package eu.brede.graspj.datatypes.spot;

import java.util.ArrayList;
import java.util.Collection;

import eu.brede.graspj.datatypes.fitvariable.FloatFitVariable;

public abstract class AbstractSpot implements Spot {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int frameNr;
	private int spotNr;
	
	protected AbstractSpot (int frameNr, int spotNr) {
		this.frameNr = frameNr;
		this.spotNr = spotNr;
	}
	
	@Override
	public Collection<String> toStringCollection() {
		ArrayList<String> outputStringCollection = new  ArrayList<String>();
		for (FloatFitVariable fitVariable : getFitVariables()) {
			outputStringCollection.addAll(fitVariable.toStringCollection());
		}
		outputStringCollection.add(String.valueOf(frameNr()));
		return outputStringCollection;
	}
	
	public FloatFitVariable getFitVariable(int i) {
		return (FloatFitVariable) getFitVariables().toArray()[i];
	}
	
	public int getNumFitVariables() {
		return getFitVariables().size();
	}
	
	public int frameNr() {
		return frameNr;
	}
	
	public int spotNr() {
		return spotNr;
	}
}
