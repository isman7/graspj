package eu.brede.graspj.gui.configurator;

import eu.brede.graspj.datatypes.fitvariable.FitVariable;

public class FitVariableItem extends CollapsableItemContainer {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	FitVariableItem(FitVariable<?> fitVariable) {
		super(true);
		setValue(fitVariable);
	}
	
	
	@Override
	public FitVariable<?> getValue() {
		// TODO instead use original fitVariable?
		return new FitVariable(get("FitVariable.value"),get("FitVariable.uncertainty"));
	}

	@Override
	public void setValue(Object value) {
		FitVariable<?> fitVariable = (FitVariable<?>) value;
		put("FitVariable.value",fitVariable.getValue());
		put("FitVariable.uncertainty",fitVariable.getUncertainty());
	}
	
//	private static LinkedHashMap<String,Object> fitVariableToMap(FitVariable<?> fitVariable) {
//		LinkedHashMap<String,Object> map = new LinkedHashMap<String,Object>();
//		map.put("value", fitVariable.getValue());
//		map.put("uncertainty", fitVariable.getUncertainty());
//		return map;
//	}
	
	
}
