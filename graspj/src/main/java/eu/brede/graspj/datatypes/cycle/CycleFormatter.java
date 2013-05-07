package eu.brede.graspj.datatypes.cycle;

import java.text.ParseException;

import javax.swing.JFormattedTextField.AbstractFormatter;

public class CycleFormatter extends AbstractFormatter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Cycle stringToValue(String pattern) throws ParseException {
		Cycle cycle = new Cycle();
		cycle.clear();
		boolean activationFlag = false;
		boolean multiFlag = false;
		String multi = "";
		for(int i=0; i<pattern.length(); i++) {
			Character c = pattern.charAt(i);
			String s = pattern.substring(i, i+1);
			if(Character.isDigit(c)) {
				if(multiFlag) {
					multi += s;
				}
				else {
					cycle.add(new CycleEntry(
						Integer.valueOf(s),
						activationFlag));
				}
			}
			else if(s.equals("[")) {
				activationFlag = true;
			}
			else if(s.equals("]")) {
				activationFlag = false;
			}
			else if(s.equals("{")) {
				multiFlag = true;
			}
			else if(s.equals("}")) {
				multiFlag = false;
				int repetitions = Integer.valueOf(multi)-1;
				multi = "";
				CycleEntry entry = cycle.get(cycle.size()-1);
				for(int r=0; r<repetitions; r++) {
					cycle.add(new CycleEntry(entry));
				}
			}
			else {
				throw new Error("Illegal Cycle-String character: " + c);
			}
		}
		return cycle;
	}

	@Override
	public String valueToString(Object value) throws ParseException {
		// TODO implement conversion here to make independent of toString method?
		return (value==null)?"":value.toString();
	}

}
