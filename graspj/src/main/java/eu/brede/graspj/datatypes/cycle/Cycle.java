package eu.brede.graspj.datatypes.cycle;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class Cycle extends ArrayList<CycleEntry> {	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Cycle() {
		// default Cycle: one color, all frames are acquisition frames
		super();
		this.add(new CycleEntry(1,false));
	}
	
	public Cycle(String pattern) throws ParseException {
		this(new CycleFormatter().stringToValue(pattern));
	}

	public Cycle(Collection<? extends CycleEntry> c) {
		super(c);
		// TODO Auto-generated constructor stub
	}
	
//	public Cycle(String pattern) {
//		super();
//		boolean activationFlag = false;
//		for(int i=0; i<pattern.length(); i++) {
//			Character c = pattern.charAt(i);
//			String s = pattern.substring(i, i+1);
//			if(Character.isDigit(c)) {
//				this.add(new CycleEntry(
//						Integer.valueOf(s),
//						activationFlag));
//			}
//			else if(s.equals("[")) {
//				activationFlag = true;
//			}
//			else if(s.equals("]")) {
//				activationFlag = false;
//			}
//			else {
//				throw new Error("Illegal Cycle-String character: " + c);
//			}
//		}
//		
//	}

	public int getCycleEntryNr(int frameNr) {
		return frameNr % this.size();
	}

	public boolean isActivation(int frameNr) {
		return get(getCycleEntryNr(frameNr)).isActivation();
	}
	
	public boolean isAcquisition(int frameNr) {
		return !isActivation(frameNr);
	}
	
	public int getColor(int frameNr) {
		return get(getCycleEntryNr(frameNr)).getColor();
	}
	
	public boolean isFirstAfterActivation(int frameNr) {
		return get(getCycleEntryNr(frameNr-1)).isActivation();
	}

	@Override
	public String toString() {
		String string = "";
		for(CycleEntry entry : this) {
			if(entry.isActivation()) {
				string += "[" + String.valueOf(entry.getColor()) + "]";
			}
			else {
				string += String.valueOf(entry.getColor());
			}
		}
		return string;
	}
	
	public ArrayList<Integer> getColors() {
		ArrayList<Integer> colors = new ArrayList<Integer>();
		for(CycleEntry entry : this) {
			if(!colors.contains(entry.getColor())) {
				colors.add(entry.getColor());
			}
		}
		Collections.sort(colors);
		return colors;
	}
	
}
