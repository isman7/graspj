package eu.brede.graspj.datatypes.spot;

import java.io.Serializable;
import java.util.Collection;

import eu.brede.common.io.Stringifiable;
import eu.brede.graspj.datatypes.fitvariable.FloatFitVariable;



public interface Spot extends Stringifiable,Serializable
{
	public FloatFitVariable x();
	public FloatFitVariable y();
	public FloatFitVariable z();
	public FloatFitVariable intensity();
	public FloatFitVariable background();
	public int frameNr();
	public int spotNr();
	public Collection<FloatFitVariable> getFitVariables();
	public FloatFitVariable getFitVariable(int i);
	public int getNumFitVariables();
}
