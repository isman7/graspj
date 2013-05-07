package eu.brede.graspj.datatypes.cycle;

import java.io.Serializable;

public class CycleEntry implements Serializable{
//	public int getColor();
//	public boolean isActivation();
//	public boolean isAcquisition();
//	public boolean isFirstAfterActivation();
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int color;
	private boolean activation;
	
	public CycleEntry() {
		
	}
	
	public CycleEntry(int color, boolean activation) {
		this.color = color;
		this.activation = activation;
	}
	
	// Copy Constructor
	public CycleEntry(CycleEntry entry) {
		this.color = entry.getColor();
		this.activation = entry.isActivation();
	}
	
	public int getColor() {
		return color;
	}
	public void setColor(int color) {
		this.color = color;
	}
	public boolean isActivation() {
		return activation;
	}
	public void setActivation(boolean activation) {
		this.activation = activation;
	}
	
	@Override
	public String toString() {
		
		String string = "";
		if(isActivation()) {
			string += "[" + String.valueOf(getColor()) + "]";
		}
		else {
			string += String.valueOf(getColor());
		}
		return string;
	}
}
