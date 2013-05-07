package eu.brede.graspj.datatypes;

import java.util.List;

import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.brede.common.config.EnhancedConfig;
import eu.brede.common.pipeline.Handler;
import eu.brede.common.pipeline.Processor;

public class ReportingHandler extends Handler<AnalysisItem> {
	
	final static Logger logger = LoggerFactory.getLogger(ReportingHandler.class);
	
	private StopWatch stopWatch = new Slf4JStopWatch(logger);
	private EnhancedConfig report = new EnhancedConfig();

	public ReportingHandler(List<Processor<AnalysisItem>> processors) {
		super(processors);
		report.setPrefix(this.getClass().getSimpleName() + ".");
	}

//	@Override
//	protected void use(AnalysisItem item) throws InterruptedException {
//		super.use(item);
//	}

	@Override
	protected void useWithProcessor(AnalysisItem item,
			Processor<AnalysisItem> processor) {
		
		stopWatch.start();
		super.useWithProcessor(item, processor);
		String processorName = processor.getClass().getSimpleName();
		stopWatch.stop(processorName);
		report.put(processorName, stopWatch.getElapsedTime());
	}
	
	public EnhancedConfig getReport() {
		return report;
	}

}
