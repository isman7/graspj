package eu.brede.graspj.pipeline.processors.filterer;

import static com.jogamp.opencl.CLMemory.Mem.READ_ONLY;
import static com.jogamp.opencl.CLMemory.Mem.WRITE_ONLY;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLKernel;

import eu.brede.common.config.EnhancedConfig;
import eu.brede.common.opencl.utils.CLResourceManager;
import eu.brede.common.opencl.utils.CLSystem;
import eu.brede.common.opencl.utils.CLTools;
import eu.brede.graspj.configs.filtering.FitFilterConfig;
import eu.brede.graspj.datatypes.AnalysisItem;
import eu.brede.graspj.datatypes.filtermask.FilterMask;
import eu.brede.graspj.datatypes.spotcollection.BufferSpotCollection;
import eu.brede.graspj.opencl.utils.CLSystemGJ;
import eu.brede.graspj.utils.Utils;

public class SpotFitFilterer extends SpotFilterer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	final static Logger logger = LoggerFactory.getLogger(SpotFitFilterer.class);

	private FitFilterConfig config;

	public SpotFitFilterer(FitFilterConfig config) {
		super();
		this.config = config;
	}

	public SpotFitFilterer() {
		super();
		this.config = new FitFilterConfig();
	}

	@Override
	protected FilterMask calcMask(final AnalysisItem item) {
		final CLSystem cl = CLSystemGJ.getDefault();;
		CLResourceManager manager = new CLResourceManager();
		CLCommandQueue queue = cl.pollQueue();

		final BufferSpotCollection spotCollection = item.getSpots();

		CLKernel kernel = manager.watch(cl.getProgramManager().getProgram("spot_filter.cl")
				.createCLKernel("filter_spots"));

		CLBuffer<FloatBuffer> minimaCL = manager.watch(Utils
				.tryAllocation(new Callable<CLBuffer<FloatBuffer>>() {

					@Override
					public CLBuffer<FloatBuffer> call() throws Exception {
						return cl.getContext().createBuffer(
								getConfig().getRangeBuffers().getMin(),
								READ_ONLY);
					}

				}));

		CLBuffer<FloatBuffer> maximaCL = manager.watch(Utils
				.tryAllocation(new Callable<CLBuffer<FloatBuffer>>() {

					@Override
					public CLBuffer<FloatBuffer> call() throws Exception {
						return cl.getContext().createFloatBuffer(
								getConfig().getRangeBuffers().getMax().limit(),
								READ_ONLY);
					}

				}));

		
		maximaCL.getBuffer().put(getConfig().getRangeBuffers().getMax());
		maximaCL.getBuffer().rewind();

		CLBuffer<ByteBuffer> maskCL = manager.watch(Utils
				.tryAllocation(new Callable<CLBuffer<ByteBuffer>>() {

					@Override
					public CLBuffer<ByteBuffer> call() throws Exception {
						return cl.getContext()
								.createByteBuffer(spotCollection.getSpotCount(), WRITE_ONLY);
					}

				})); 
				

		CLBuffer<FloatBuffer> spotsCL = manager.watch(Utils
				.tryAllocation(new Callable<CLBuffer<FloatBuffer>>() {

					@Override
					public CLBuffer<FloatBuffer> call() throws Exception {
						return item.getSpots()
								.getSpots().getCLBuffer(cl);
					}

				}));  
				

		queue.putWriteBuffer(minimaCL, true).putWriteBuffer(maximaCL, true)
				.putWriteBuffer(maskCL, true).putWriteBuffer(spotsCL, true);

		kernel.putArg(minimaCL).putArg(maximaCL).putArg(spotsCL).putArg(maskCL);

		int itemsPerExecStep = 262144;
		int localWorkSize = 64;
		
		int spotCount = spotCollection.getSpotCount();
		
		CLTools.steppedKernelEnqueue(queue, kernel, spotCount, 0,
				itemsPerExecStep, localWorkSize);
		
//		queue.put1DRangeKernel(kernel, 0, spotCollection.getSpotCount(), 0);
		queue.finish();

		maskCL.getBuffer().rewind();
		spotsCL.getBuffer().rewind();
		queue.putReadBuffer(maskCL, true);

		// save to spots or is that done automatically?
		maskCL.getBuffer().rewind();

		FilterMask newMask = new FilterMask();
		newMask.setBuffer(maskCL.getBuffer());

		int numSpots = newMask.countPositives();
		logger.info("Spots after filtering: {}", numSpots);

		manager.releaseAll();
		cl.returnQueue(queue);
		return newMask;
	}

	@Override
	public FitFilterConfig getConfig() {
		return config;
	}

	@Override
	public void setConfig(EnhancedConfig config) {
		// TODO Auto-generated method stub
		this.config = (FitFilterConfig) config;
	}

}
