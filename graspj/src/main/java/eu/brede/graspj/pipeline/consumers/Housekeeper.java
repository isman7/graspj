package eu.brede.graspj.pipeline.consumers;

import java.io.Serializable;

import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.brede.common.pipeline.consumers.AbstractConsumer;
import eu.brede.graspj.datatypes.AnalysisItem;

public class Housekeeper extends AbstractConsumer<AnalysisItem>
		implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	final static Logger logger = LoggerFactory.getLogger(Housekeeper.class);
	int counter = 0;
	
	public Housekeeper() {
	}

	@Override
	protected void use(AnalysisItem item) throws InterruptedException {
		counter++;
		if(counter>=10) {			
			runGC();
			counter = 0;
		}
	}
	
	private void runGC() {
		Slf4JStopWatch stopWatch = new Slf4JStopWatch(logger);
		System.gc();
		stopWatch.stop("GarbargeCollection");
	}

	@Override
	protected void finish() {
		runGC();
		super.finish();
	}

}
