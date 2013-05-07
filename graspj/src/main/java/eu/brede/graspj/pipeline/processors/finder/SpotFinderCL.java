package eu.brede.graspj.pipeline.processors.finder;

import static com.jogamp.opencl.CLMemory.Mem.READ_ONLY;
import static com.jogamp.opencl.CLMemory.Mem.WRITE_ONLY;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.concurrent.Callable;

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
import eu.brede.common.util.DeepCopy;
import eu.brede.graspj.configs.finding.FindConfig;
import eu.brede.graspj.datatypes.AnalysisItem;
import eu.brede.graspj.datatypes.bufferholder.BufferHolder;
import eu.brede.graspj.datatypes.spotcollection.BufferSpotCollection;
import eu.brede.graspj.opencl.utils.CLSystemGJ;
import eu.brede.graspj.pipeline.processors.AbstractAIProcessor;
import eu.brede.graspj.utils.Utils;

public class SpotFinderCL extends AbstractAIProcessor {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected BufferHolder<ShortBuffer> candidates = new BufferHolder<ShortBuffer>();
	private FindConfig config;

	final static Logger logger = LoggerFactory.getLogger(SpotFinderCL.class);

	public SpotFinderCL() {
		super();
		config = new FindConfig();
	}

	public SpotFinderCL(SpotFinderCL finder) {
		config = DeepCopy.copy(finder.getConfig());
	}

	@Override
	public void process(AnalysisItem item) {
		super.process(item);
		// TODO this needs major cleanup!
		CLResourceManager manager = new CLResourceManager();
		final CLSystem cl = CLSystemGJ.getDefault();;
		CLCommandQueue queue = cl.pollQueue();

		ShortBuffer frameBuffer = item.getNotes()
				.<BufferHolder<ShortBuffer>> gett("frameBuffer").getBuffer();

		long frameByteSize = item.getAcquisitionConfig().getDimensions().frameByteSize();
		int bytesPerShort = 2;
		int framesPerPackage = (int) Math
				.floor(((double) (bytesPerShort * frameBuffer.limit()))
						/ frameByteSize);

		// don't watch on purpose
		CLBuffer<ShortBuffer> counts = cl.getContext().createShortBuffer(
				framesPerPackage, WRITE_ONLY);

		int maxSpotsPerFrame = getConfig().getInt("maxSpotsPerFrame");
		int maxSpotsPerPackage = maxSpotsPerFrame * framesPerPackage;

		int parametersPerCandidate = 3;
		int candidatesBufferSize = maxSpotsPerPackage * parametersPerCandidate;

		int frameWidth = item.getAcquisitionConfig().getDimensions().frameWidth;
		int frameHeight = item.getAcquisitionConfig().getDimensions().frameHeight;

		CLBuffer<ShortBuffer> candidates = manager.watch(cl.getContext()
				.createShortBuffer(candidatesBufferSize, WRITE_ONLY));

		// don't watch that on purpose! (will be needed for fitting)
		final CLBuffer<ShortBuffer> frameBufferCL = item.getNotes()
				.<BufferHolder<ShortBuffer>> gett("frameBuffer")
				.getCLBuffer(cl);

		CLBuffer<ByteBuffer> maskBuffer = manager.watch(Utils
				.tryAllocation(new Callable<CLBuffer<ByteBuffer>>() {

					@Override
					public CLBuffer<ByteBuffer> call() throws Exception {
						return cl.getContext().createByteBuffer(
								frameBufferCL.getCLCapacity(), Mem.READ_WRITE);
					}

				}));  

		maskBuffer.getBuffer().put(new byte[frameBufferCL.getCLCapacity()]);
		maskBuffer.getBuffer().rewind();
		queue.putWriteBuffer(maskBuffer, true);

		frameBufferCL.getBuffer().rewind();

		queue.putWriteBuffer(frameBufferCL, true);// .finish();

		CLKernel findKernel = manager.watch(cl.getProgramManager().getProgram("spot_finder.cl")
				.createCLKernel("find_spots"));

		findKernel.putArg(getConfig().getInt("threshold"))
				.putArg(getConfig().getInt("maxSpotsPerFrame"))
				.putArg(getConfig().getInt("boxRadius")).putArg(frameWidth)
				// TODO frame width
				.putArg(frameHeight)
				// TODO frame height
				.putArg(frameBufferCL).putArg(maskBuffer).putArg(candidates)
				.putArg(counts);

		int framesPerExecStep = 1024;
		int localWorkSize = 64;
		int frameOffset = 0;

		CLTools.steppedKernelEnqueue(queue, findKernel, framesPerPackage,
				frameOffset, framesPerExecStep, localWorkSize, false);

		queue.putReadBuffer(candidates, true).putReadBuffer(counts, true);

		maskBuffer.getBuffer().clear();
		maskBuffer.release();

		item.setSpots(new BufferSpotCollection());
		item.getSpots().getCounts().setCLBuffer(counts);

		short[] candidatesArray = new short[candidatesBufferSize];
		candidates.getBuffer().get(candidatesArray, 0, candidatesArray.length);
		candidates.getBuffer().clear();
		candidates.release();

		short[] countsArray = new short[framesPerPackage];
		counts.getBuffer().get(countsArray, 0, countsArray.length);

		int numSpots = item.getSpots().getSpotCount();

		logger.info("Spots found: {}", numSpots);

		int trimmedCandidatesBufferSize = Math.max(1, numSpots
				* parametersPerCandidate);
		if (trimmedCandidatesBufferSize == 0)
			trimmedCandidatesBufferSize = 1;

		// don't watch that on purpose! (will be needed for fitting)
		CLBuffer<ShortBuffer> trimmedCandidates = cl.getContext()
				.createShortBuffer(trimmedCandidatesBufferSize, READ_ONLY);

		// TODO is this right? compare to declaration of countsArray
		int framesInThisPackage = item.getSpots().getFrameCount();

		trimmedCandidates.getBuffer().rewind();

		for (int frameNr = 0; frameNr < framesInThisPackage; frameNr++) {

			int srcOffsetNew = maxSpotsPerFrame * frameNr
					* parametersPerCandidate;
			int shortsToCopy = countsArray[frameNr] * parametersPerCandidate;
			trimmedCandidates.getBuffer().put(candidatesArray, srcOffsetNew,
					shortsToCopy);

		}
		trimmedCandidates.getBuffer().rewind();
		queue.putWriteBuffer(trimmedCandidates, true);
		queue.finish();

		this.candidates.setCLBuffer(trimmedCandidates);

		item.getNotes().put("candidates", this.candidates);

		manager.releaseAll();
		cl.returnQueue(queue);
		return;
	}

	@Override
	public FindConfig getConfig() {
		return config;
	}

	@Override
	public void setConfig(EnhancedConfig config) {
		this.config = (FindConfig) config; // TODO wrap instead!
	}

}
