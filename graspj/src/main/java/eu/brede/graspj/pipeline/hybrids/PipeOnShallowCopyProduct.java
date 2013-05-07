package eu.brede.graspj.pipeline.hybrids;

import java.io.Serializable;

import eu.brede.common.pipeline.NeedsProduct;
import eu.brede.common.pipeline.hybrids.ConsumptionPipeline;
import eu.brede.common.pipeline.hybrids.ProductionPipeline;
import eu.brede.graspj.datatypes.AnalysisItem;

public class PipeOnShallowCopyProduct extends ConsumptionPipeline<AnalysisItem,AnalysisItem>
	implements NeedsProduct<AnalysisItem>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PipeOnShallowCopyProduct() {
		super(new ShallowCopier(), 
				new ProductionPipeline<AnalysisItem,AnalysisItem>());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setProduct(AnalysisItem product) {
		if(pipeline instanceof NeedsProduct<?>) {
			((NeedsProduct<AnalysisItem>)pipeline).setProduct(product.shallowCopy());
		}
	}

}
