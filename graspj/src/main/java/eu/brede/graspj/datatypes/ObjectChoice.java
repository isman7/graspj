package eu.brede.graspj.datatypes;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Vector;

public class ObjectChoice<T> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final Vector<T> choices; 
	private T chosen;
	
	public ObjectChoice(Vector<T> choices) {
		this.choices = choices;
	}
	
	@SafeVarargs
	public ObjectChoice(T... choices) {
		this.choices = new Vector<T>(Arrays.asList(choices));
	}
	
	public ObjectChoice() {
		this(new Vector<T>());
	}
	
	public Vector<T> getChoices() {
		return choices;
	}
	
	public T getChosen() {
		return chosen;
	}
	
	public boolean setChosen(T choice) {
		if(choices.contains(choice)) {
			this.chosen = choice;
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean setChosenByClass(Class<? extends T> clazz) {
		for(T choice : getChoices()) {
			if(clazz.isAssignableFrom(choice.getClass())) {
				setChosen(choice);
				return true;
			}
		}
		return false;
	}
	
	public boolean addChosen(T choice) {
		if(choices.contains(choice)) {
			this.chosen = choice;
			return true;
		}
		else {
			choices.add(choice);
			this.chosen = choice;
			return false;
		}
	}
	
}
