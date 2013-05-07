package eu.brede.graspj.pipeline.processors;

import eu.brede.common.config.Configurable;
import eu.brede.common.pipeline.Processor;
import eu.brede.graspj.datatypes.AnalysisItem;

public abstract class AbstractAIProcessor implements Processor<AnalysisItem>, Configurable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	
	@Override
	public void process(AnalysisItem item) {
//		System.out.println(this.getClass().getSimpleName() 
//				+ " now runs on package " 
//				+ item.getNotes().<Integer>gett("packageNr"));
	}



	@Override
	public String toString() {
		String string = this.getClass().getSimpleName();
		if(getConfig().getMetaData().getName()!="") {
			string += " - " + getConfig().getMetaData().getName();
		}
		return string;
	}
	
}
