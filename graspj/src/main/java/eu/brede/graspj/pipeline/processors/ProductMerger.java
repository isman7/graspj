package eu.brede.graspj.pipeline.processors;

import eu.brede.common.pipeline.NeedsProduct;
import eu.brede.common.pipeline.Processor;
import eu.brede.graspj.datatypes.AnalysisItem;

public class ProductMerger implements Processor<AnalysisItem>,
	NeedsProduct<AnalysisItem> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	 transient AnalysisItem mergedItem;
	
	public ProductMerger(AnalysisItem mergedItem) {
		super();
		this.mergedItem = mergedItem;
	}
	
	public ProductMerger() {
		super();
	}

	@Override
	public void setProduct(AnalysisItem product) {
		mergedItem = product;
	}

	@Override
	public void process(AnalysisItem item) {
		mergedItem.append(item);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

}
