package eu.brede.graspj.pipeline.processors.renderer;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLKernel;

import eu.brede.common.opencl.utils.CLResourceManager;
import eu.brede.common.opencl.utils.CLSystem;
import eu.brede.common.opencl.utils.CLTools;
import eu.brede.common.pipeline.Processor;
import eu.brede.graspj.datatypes.AnalysisItem;
import eu.brede.graspj.opencl.utils.CLSystemGJ;

public class TempShifter implements Processor<AnalysisItem> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void process(AnalysisItem item) {
		CLResourceManager manager = new CLResourceManager();
		CLSystem cl = CLSystemGJ.getDefault();;
		CLCommandQueue queue = cl.pollQueue();

		CLKernel kernel = manager.watch(cl.getProgramManager()
				.getProgram("spot_manipulator.cl")
				.createCLKernel("shift_spots"));

		int spotCount = item.getSpots().getSpotCount();

		// don't watch spots and frames (needed later)
		CLBuffer<FloatBuffer> spotsBuffer = item.getSpots().getSpots()
				.getCLBuffer(cl);
		// System.out.println("whats wrong with you? " +
		// item.getSpots().getSpots().getCLBuffer());
		CLBuffer<IntBuffer> frameNrsBuffer = item.getSpots().getFrameNrs()
				.getCLBuffer(cl);
		CLBuffer<FloatBuffer> shiftBuffer = manager.watch(item.getDrift()
				.getCLBuffer(cl));

		spotsBuffer.getBuffer().rewind();
		frameNrsBuffer.getBuffer().rewind();
		shiftBuffer.getBuffer().rewind();

		// CLBuffer<FloatBuffer> shiftBuffer =
		// pipe.getContext().createFloatBuffer(
		// 2*spotCollection.getFrameCount(), READ_ONLY);
		//
		// shiftBuffer.getBuffer().put(drift);
		// shiftBuffer.getBuffer().rewind();

		queue.putWriteBuffer(spotsBuffer, true)
				.putWriteBuffer(frameNrsBuffer, true)
				.putWriteBuffer(shiftBuffer, true);

		kernel.putArg(spotsBuffer).putArg(frameNrsBuffer).putArg(shiftBuffer);

		int itemsPerExecStep = 32000;
		int localWorkSize = 64;
		CLTools.steppedKernelEnqueue(queue, kernel, spotCount, 0,
				itemsPerExecStep, localWorkSize);
		// pipe.getComputationQueue()
		// .put1DRangeKernel(kernel, 0, spotCount, 0)
		// .finish();

		// TODO DO NOT READ BACK RESULT! shift is supposed to be temporary.
		queue.putReadBuffer(spotsBuffer, true);
		manager.releaseAll();
		cl.returnQueue(queue);
		return;
	}

}
