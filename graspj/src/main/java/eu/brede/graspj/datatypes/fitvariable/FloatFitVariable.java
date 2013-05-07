package eu.brede.graspj.datatypes.fitvariable;

import java.io.Serializable;

import eu.brede.common.io.Stringifiable;


public interface FloatFitVariable extends Stringifiable,Serializable
{
	public float value();
	public float uncertainty();
	boolean isWithin(int nSigma, double meanX);
	public void setValue(float value);
}
