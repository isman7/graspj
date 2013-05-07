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

import eu.brede.common.config.EnhancedConfig;
import eu.brede.common.opencl.utils.CLResourceManager;
import eu.brede.common.opencl.utils.CLSystem;
import eu.brede.common.opencl.utils.CLTools;
import eu.brede.graspj.configs.fit.FitConfig;
import eu.brede.graspj.datatypes.AnalysisItem;
import eu.brede.graspj.datatypes.bufferholder.BufferHolder;
import eu.brede.graspj.opencl.utils.CLSystemGJ;
import eu.brede.graspj.pipeline.processors.AbstractAIProcessor;
import eu.brede.graspj.utils.Utils;

public class MLEFitter2D extends AbstractAIProcessor {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	final static Logger logger = LoggerFactory.getLogger(MLEFitter2D.class);

	private FitConfig config;
	private int packageNr = 0;
	protected String clProgramFitter = "CLProgramMLE2DFitter";

	public MLEFitter2D() {
		config = new FitConfig();
	}

	@Override
	public void process(AnalysisItem item) {
		CLResourceManager manager = new CLResourceManager();
		final CLSystem cl = CLSystemGJ.getDefault();;
		CLCommandQueue queue = cl.pollQueue();

		BufferHolder<ShortBuffer> candidates = item.getNotes().gett(
				"candidates");
		CLBuffer<ShortBuffer> frameBuffer = manager.watch(item.getNotes()
				.<BufferHolder<ShortBuffer>> gett("frameBuffer")
				.getCLBuffer(cl));

		int spotCount = item.getSpots().getSpotCount();

		// TEMP FIX, why required? For Buffer creation?!
		if (spotCount == 0) {
			spotCount = 1;
		}

		int valuesPerDimension = 2;
		final int spotBufferSize = spotCount * getConfig().getInt("fitDimension")
				* valuesPerDimension;

		CLBuffer<FloatBuffer> spots = manager.watch(Utils
				.tryAllocation(new Callable<CLBuffer<FloatBuffer>>() {

					@Override
					public CLBuffer<FloatBuffer> call() throws Exception {
						return cl.getContext()
								.createFloatBuffer(spotBufferSize, READ_WRITE);
					}

				}));

		CLKernel fitKernel = null;// clPipe.getProgram("GraspJ_peak_fitting.cl").createCLKernel("graspj");

		fitKernel = manager.watch(cl.getProgramManager().getProgram(clProgramFitter)
				.createCLKernel("fit_spots"));

		fitKernel
				.putArg(item.getAcquisitionConfig().getFloat("pixelSize"))
				.putArg(getConfig().getFloat("sigmaPSF"))
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

		spotCount = item.getSpots().getSpotCount();
		logger.info("Spots fitted: {}", spotCount);

		item.getSpots().rewindAllBuffers();

		// try to free direct and CL memory
		item.getNotes().<BufferHolder<ShortBuffer>> gett("frameBuffer").free();
		// item.getAcquisition().getFrameBuffer().free();
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
