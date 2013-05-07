package eu.brede.graspj.gui.configurator;

import eu.brede.graspj.datatypes.range.RangeComparable;

public class RangeItem extends CollapsableItemContainer {
	

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RangeItem(RangeComparable<? extends Number> range) {
		super();
		setValue(range);
	}

	@Override
	public RangeComparable<?> getValue() {
		return new RangeComparable((Comparable)get("Range.min"),(Comparable)get("Range.max"));
	}

	@Override
	public void setValue(Object value) {
		RangeComparable<? extends Number> range = (RangeComparable<? extends Number>) value;
		put("Range.min",range.getMin());
		put("Range.max",range.getMax());
	}

}
