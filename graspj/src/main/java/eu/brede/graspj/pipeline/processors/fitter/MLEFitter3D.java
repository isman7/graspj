package eu.brede.graspj.pipeline.processors.fitter;

import static com.jogamp.opencl.CLMemory.Mem.READ_ONLY;
import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.text.DecimalFormat;
import java.util.concurrent.Callable;

import org.perf4j.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLMemory.Mem;

import eu.brede.common.config.EnhancedConfig;
import eu.brede.common.opencl.utils.CLResourceManager;
import eu.brede.common.opencl.utils.CLSystem;
import eu.brede.common.opencl.utils.CLTools;
import eu.brede.graspj.configs.fit.FitConfig;
import eu.brede.graspj.configs.fit.MLE3DConfig;
import eu.brede.graspj.datatypes.AnalysisItem;
import eu.brede.graspj.datatypes.DefocusingCurve;
import eu.brede.graspj.datatypes.bufferholder.BufferHolder;
import eu.brede.graspj.opencl.utils.CLSystemGJ;
import eu.brede.graspj.pipeline.processors.AbstractAIProcessor;
import eu.brede.graspj.utils.Utils;

public class MLEFitter3D extends AbstractAIProcessor {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	final static Logger logger = LoggerFactory.getLogger(MLEFitter3D.class);

	private FitConfig config;
	private int packageNr = 0;

	public MLEFitter3D() {
		config = new MLE3DConfig();
	}

	@Override
	public void process(AnalysisItem item) {
		super.process(item);

		CLResourceManager manager = new CLResourceManager();
		final CLSystem cl = CLSystemGJ.getDefault();;
		CLCommandQueue queue = cl.pollQueue();

		BufferHolder<ShortBuffer> candidates = item.getNotes().gett(
				"candidates");
		CLBuffer<ShortBuffer> frameBuffer = manager.watch(item.getNotes()
				.<BufferHolder<ShortBuffer>> gett("frameBuffer")
				.getCLBuffer(cl));

		int spotCount = item.getSpots().getSpotCount();
		int valuesPerDimension = 2;
		final int spotBufferSize = spotCount * getConfig().getInt("fitDimension")
				* valuesPerDimension;

		// TODO really watch this?
		CLBuffer<FloatBuffer> spots = manager.watch(Utils
				.tryAllocation(new Callable<CLBuffer<FloatBuffer>>() {

					@Override
					public CLBuffer<FloatBuffer> call() throws Exception {
						return cl.getContext()
								.createFloatBuffer(spotBufferSize, READ_WRITE);
					}

				}));

		CLKernel fitKernel = manager
				.watch(cl.getProgramManager()
						.getProgram("CLProgramMLE3DFitter")
						.createCLKernel("fit_spots"));

		CLBuffer<FloatBuffer> dfcX = manager.watch(Utils
				.tryAllocation(new Callable<CLBuffer<FloatBuffer>>() {

					@Override
					public CLBuffer<FloatBuffer> call() throws Exception {
						return cl.getContext()
								.createBuffer(
										getConfig().<DefocusingCurve> gett("dfCurveX")
												.asBuffer(), Mem.READ_ONLY);
					}

				}));

		CLBuffer<FloatBuffer> dfcY = manager.watch(Utils
				.tryAllocation(new Callable<CLBuffer<FloatBuffer>>() {

					@Override
					public CLBuffer<FloatBuffer> call() throws Exception {
						return cl.getContext()
								.createBuffer(
										getConfig().<DefocusingCurve> gett("dfCurveY")
												.asBuffer(), Mem.READ_ONLY);
					}

				})); 

		queue.putWriteBuffer(dfcX, true);
		queue.putWriteBuffer(dfcY, true);

        fitKernel
                .putArg(item.getAcquisitionConfig().getFloat("pixelSize"))
                .putArg(dfcX).putArg(dfcY)
                .putArg(item.getAcquisitionConfig().getFloat("countConversion"))
                .putArg(item.getAcquisitionConfig().getInt("countOffset"))
                .putArg(getConfig().getInt("fitDimension"))
                .putArg(getConfig().getInt("iterations"))
                .putArg(getConfig().getInt("boxRadius"))
                .putArg(item.getAcquisitionConfig().getDimensions().frameWidth)
                .putArg(item.getAcquisitionConfig().getDimensions().frameHeight)
                .putArg(frameBuffer)
                .putArg(manager.watch(candidates.getCLBuffer(cl)))
                .putArg(spots);

		int spotsPerExecStep = 16000;
		int localWorkSize = 64;
		int spotOffset = 0;

		StopWatch stopWatch = new StopWatch();

		CLTools.steppedKernelEnqueue(queue, fitKernel, spotCount, spotOffset,
				spotsPerExecStep, localWorkSize);

		stopWatch.stop();

		double execTime = Math.max(1, stopWatch.getElapsedTime());
		double speed = (spotCount * 1000) / execTime;
		DecimalFormat df = new DecimalFormat("#");

		logger.info("Package {} completed fitting with {} spots/s", packageNr,
				df.format(speed));

		queue.putReadBuffer(spots, true);

		item.getSpots().getSpots().setCLBuffer(spots);

		// try to free direct and CL memory
		item.getNotes().<BufferHolder<ShortBuffer>> gett("frameBuffer").free();
		candidates.free();

		// remove candidates from notes, because it is no longer valid
		item.getNotes().remove("candidates");
		item.getNotes().remove("frameBuffer");

		// TODO don't call gc to often!
		// System.gc();

		manager.releaseAll();
		cl.returnQueue(queue);
		packageNr++;
		return;
	}

	@Override
	public FitConfig getConfig() {
		return config;
	}

	@Override
	public void setConfig(EnhancedConfig config) {
		this.config = (FitConfig) config; // TODO wrap instead
	}

}
