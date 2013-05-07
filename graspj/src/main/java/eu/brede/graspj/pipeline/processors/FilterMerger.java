package eu.brede.graspj.pipeline.processors;

import eu.brede.common.pipeline.NeedsProduct;
import eu.brede.common.pipeline.Processor;
import eu.brede.graspj.datatypes.AnalysisItem;

public class FilterMerger implements Processor<AnalysisItem>,
	NeedsProduct<AnalysisItem> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	transient AnalysisItem product;
	
	public FilterMerger(AnalysisItem mergedItem) {
		super();
		this.product = mergedItem;
	}
	
	public FilterMerger() {
		super();
	}

	@Override
	public void setProduct(AnalysisItem product) {
		this.product = product;
	}

	@Override
	public void process(AnalysisItem item) {
		product.getMask().append(item.getMask());
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

}
