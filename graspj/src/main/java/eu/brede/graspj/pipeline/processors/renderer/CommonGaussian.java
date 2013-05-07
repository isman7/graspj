package eu.brede.graspj.pipeline.processors.renderer;

import static com.jogamp.opencl.CLMemory.Mem.READ_ONLY;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.perf4j.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLKernel;

import eu.brede.common.opencl.utils.CLResourceManager;
import eu.brede.common.opencl.utils.CLSystem;
import eu.brede.common.opencl.utils.CLTools;
import eu.brede.common.util.ExceptionTools;
import eu.brede.graspj.datatypes.AnalysisItem;
import eu.brede.graspj.datatypes.Option;
import eu.brede.graspj.datatypes.Rendering;
import eu.brede.graspj.opencl.utils.CLSystemGJ;

public abstract class CommonGaussian extends SpotRenderer {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected abstract Rendering newRendering();
	protected abstract CLKernel newKernel();
	protected abstract void putSpecificKernelArgs(CLKernel kernel);
	
//	protected transient CLCommandQueue queue;// = CLPipe.INSTANCE.getQueue();
	protected transient CLSystem cl;
	
	final static Logger logger = LoggerFactory.getLogger(CommonGaussian.class);
	
	
	
	@Override
	public void process(final AnalysisItem item) {
		if(cl==null) {
//			cl = CLSystem.get(this);
			cl = CLSystemGJ.getDefault();;
		}
//		if(queue==null) {
//			queue = cl.pollQueue();
//		}
		final CLCommandQueue queue = cl.pollQueue();
		CLResourceManager manager = new CLResourceManager();
		
		int renderWidth = getConfig().getInt("renderWidth");
		int renderHeight = getConfig().getInt("renderHeight");
//		boolean complex = getConfig().getBoolean("complex");
		
		if((rendering==null)
			|| (rendering.getWidth() != renderWidth) 
			|| (rendering.getHeight() != renderHeight)) {
			
			rendering = newRendering();
			rendering.clean();
		}
		else {
			// TODO clean here? add spots vs replace
			rendering.clean();
		}
		
		item.lock();
		
		if(item.getSpots().getSpotCount()==0) {
			logger.warn("spotCount is zero, no rendering possible");
			return;
		}
		
		
		Option applyMask = getConfig().gett("applyMask");
		
		if(applyMask.isSelected() && !item.getMask().holdsBuffer()) {
			logger.warn("no mask available, applyMask set to false");
			applyMask.setSelected(false);
		}
		
		boolean applyDrift=false;
		CLBuffer<IntBuffer> frameNrsBuffer = manager.watch( 
				item.getSpots().getFrameNrs().getCLBuffer(cl));
		
		CLBuffer<FloatBuffer> shiftBuffer;
		if(item.getDrift().holdsBuffer()) {
			applyDrift = true;
			
			shiftBuffer = item.getDrift().getCLBuffer(cl);
			
			frameNrsBuffer.getBuffer().rewind();
			shiftBuffer.getBuffer().rewind();
			
			queue.putWriteBuffer(frameNrsBuffer, true)
				.putWriteBuffer(shiftBuffer, true);
			
		}
		else {
			shiftBuffer = cl.getContext().createFloatBuffer(1, READ_ONLY);
		}
		manager.watch(shiftBuffer);
		
		// TODO sloppy Exception fix
		ExceptionTools.tryNTimes(3, new Runnable() {
			
			@Override
			public void run() {
				item.getSpots().getSpots().getCLBuffer(cl).getBuffer().rewind();
				queue.putWriteBuffer(item.getSpots().getSpots().getCLBuffer(cl), true);
			}
		});
		
		
		CLKernel kernel = manager.watch(newKernel());
	
    	
    	CLBuffer<?> maskBuffer = null;
    	if(applyMask.isSelected()) {	
    		maskBuffer = item.getMask().getCLBuffer(cl);
    		maskBuffer.getBuffer().rewind();
    	}
    	if(!applyMask.isSelected()) { // TODO was "else" before, solve mask problem in a nicer way!
    		maskBuffer = cl.getContext().createByteBuffer(1, READ_ONLY);
    	}
    	manager.watch(maskBuffer);
    	
    		
    	
    	queue
		.putWriteBuffer(maskBuffer, true).finish();
    	

    	// default 1 for compatibility
    	int spotsPerWorker = getConfig().get("spotsPerWorker",(int)1);
//    	spotsPerWorker = 1;
    	
    	Option specificChannel = applyMask.gett("specificChannel");
    	
		kernel
    		.putArg(getConfig().getInt("renderWidth"))
    		.putArg(getConfig().getInt("renderHeight"))
    		.putArg(getConfig().getFloat("offsetX"))
    		.putArg(getConfig().getFloat("offsetY"))
    		.putArg(getConfig().getFloat("pixelSize"))
    		.putArg(getConfig().getFloat("intensity"))
    		.putArg(fitDimension)
    		.putArg(getConfig().getBoolean("complex")?1:0)
    		.putArg(applyMask.isSelected()?1:0)
    		.putArg(specificChannel.isSelected()?1:0)
    		.putArg(specificChannel.get("channelNr",(int)1))
    		.putArg(maskBuffer)
    		
    		.putArg(applyDrift?1:0)
    		.putArg(shiftBuffer)
    		.putArg(frameNrsBuffer)
    		
    		.putArg(spotsPerWorker)
    		.putArg(manager.watch(item.getSpots().getSpots().getCLBuffer(cl)))
    		// Watch rendering?
    		.putArg(rendering.getCLBuffer(cl));
    		
    		
		putSpecificKernelArgs(kernel);
    		
    	int firstFrame = getConfig().getInt("firstFrame");
    	int lastFrame = getConfig().getInt("lastFrame");
    	lastFrame = Math.min(lastFrame, item.getSpots().getFrameCount()-1);
    	int spotOffset = firstFrame==0?0:
    			item.getSpots().getCuCounts().getBuffer().get(firstFrame-1);
    	
    	// TODO: spotCount wrong if not full frame range covered
    	int spotCount = item.getSpots().getSpotCount();
    	
    	spotCount = spotCount / spotsPerWorker; // TODO avoid loosing spots!
    	
    	
    	// TODO make config parameters
    	int spotsPerExecStep = 128000;
    	int localWorkSize = 64;
    	
    	if(spotCount>0) {
    		StopWatch stopWatch = new StopWatch();
    		
    		CLTools.steppedKernelEnqueue(queue,kernel, spotCount, spotOffset,
        			spotsPerExecStep, localWorkSize, true);
    		
    		stopWatch.stop();
    		
    		logger.info("Rendering took {}ms for {} spots",stopWatch.getElapsedTime(),spotCount);
    	}
    	
//    	afterKernelExecution();
    	
    	manager.releaseAll();
    	item.unlock();
    	cl.returnQueue(queue);
    	if(getConfig().get("showImage", false)) {
    		rendering.getImagePlus(this).show();    		
    	}
    	return;
	}

//	@Override
//	protected void finalize() throws Throwable {
//		if(queue!=null) {
//			if(!queue.isReleased()) {
//				cl.returnQueue(queue);
//			}
//		}
//		super.finalize();
//	}

	
}
