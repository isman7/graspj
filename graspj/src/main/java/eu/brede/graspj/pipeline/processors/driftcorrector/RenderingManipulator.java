package eu.brede.graspj.pipeline.processors.driftcorrector;

import static com.jogamp.opencl.CLMemory.Mem.READ_ONLY;
import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;
import static com.jogamp.opencl.CLMemory.Mem.WRITE_ONLY;

import java.awt.geom.Point2D;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLKernel;

import eu.brede.common.opencl.utils.CLSystem;
import eu.brede.common.opencl.utils.CLTools;
import eu.brede.common.util.ExceptionTools;
import eu.brede.graspj.datatypes.CLRendering;
import eu.brede.graspj.datatypes.Rendering;
import eu.brede.graspj.opencl.utils.CLSystemGJ;
import eu.brede.graspj.utils.Utils;

/**
 * Provides methods for manipulating Renderings using OpenCL
 * 
 * @author Norman Brede
 *
 */
public class RenderingManipulator {
	private CLSystem cl = CLSystemGJ.getDefault();;
//	private CLCommandQueue queue;
	final static Logger logger = LoggerFactory.getLogger(RenderingManipulator.class);
	
	public RenderingManipulator() {
//		String queueName = String.valueOf(hashCode());
//		queue = cl.pollQueue();
	}

//	public Rendering blur(Rendering rendering) {
//		return stdRenderingIOKernel("blur", rendering);
//	}
	
	
	/**
	 * Divides the Rendering numerator by the Rendering denominator in complex space
	 * 
	 * @param numerator
	 * @param denominator
	 * @return Result of division (numerator/denominator)
	 */
	public Rendering divide(Rendering numerator, Rendering denominator) {
		return stdRenderingIOKernel("divide_complex", numerator, denominator);
	}
	
	/**
	 * Multiplies Rendering a with Rendering b complex space
	 * 
	 * @param a
	 * @param b
	 * @return Result of multiplication (a*b)
	 */
	public Rendering multiply(Rendering a, Rendering b) {
		return stdRenderingIOKernel("complex_multiply", a, b);
	}
	
	public Rendering absolute(Rendering rendering) {
		return stdRenderingIOKernel("absolute", rendering);
	}
	
	public Rendering complexConjugate(Rendering rendering) {
		return stdRenderingIOKernel("complex_conjugate", rendering);
	}
	
	public Rendering divideComplexByN(Rendering rendering) {
		return stdRenderingIOKernel("divide_complex_by_N", rendering);
	}
	
	public Rendering transpose(Rendering rendering) {
		CLCommandQueue queue = cl.pollQueue();
//		return stdRenderingIOKernel("transpose", rendering);
		
		// quick transpose test:
		int width = rendering.getWidth();
		int height = rendering.getHeight();
		
		int GROUP_DIMX = 32;
		int GROUP_DIMY = 2;
//		int PADDING = 0;
		
		// TODO perhaps use padding to avoid bank conflicts? but then truncate required!!
		// increases transpose performance by a factor of 5! (on HD6970 from 10ms to 2ms)
		
		CLKernel kernel = 
			cl.getProgramManager().getProgram("rendering_manipulator.cl")
			.createCLKernel("apple_transpose");
		
//		Rendering outputRendering = new Rendering();
		
		// TODO copy shallow of rendering, create new CLBuffer, perhaps:
		Rendering outputRendering = new CLRendering(
				rendering.getWidth(),rendering.getHeight(),rendering.isComplex());
//		outputRendering.getConfig().setProperty("renderHeight", height+PADDING);
		
		// putArgSize replaced putNullArg?
		kernel
			.putArg(rendering.getCLBuffer(cl))
			.putArg(outputRendering.getCLBuffer(cl))
			.putNullArg(8*GROUP_DIMX*(GROUP_DIMX+1))
			.putArg(width)
			.putArg(height);
		
		
//		long start = System.currentTimeMillis();
		// TODO problem if width or height not divisible by 2
		logger.debug("Enqueing 2D-Kernel: {}",kernel);
		
		queue
			.put2DRangeKernel(kernel, 0, 0,
					width*GROUP_DIMY, // global x
					height/GROUP_DIMX, // global y
					GROUP_DIMX*GROUP_DIMY, 1) //local x, y
			.finish(); // TODO perhaps replace by event check!
		
		logger.debug("Finished {}",kernel);
//		System.out.println("apple_transpose-kernel: " + (System.currentTimeMillis()-start) + "ms");

		// TODO really release all of them? perhaps option
		kernel.release();
		rendering.free();
		cl.returnQueue(queue);

//		outputRendering.getImagePlus().show();
		return outputRendering;
	}
	
	public Rendering window(Rendering rendering) {
		return stdRenderingIOKernel("hanning_window", rendering);
	}
	
	public Rendering FFT(Rendering rendering) {
		return FFTRadix(rendering, 2);
	}
	
	public Rendering iFFT(Rendering rendering) {
		rendering = complexConjugate(rendering);
		rendering = FFT(rendering);
		rendering = complexConjugate(rendering);
		rendering = divideComplexByN(rendering);
		return rendering;
	}
	
	public Point2D.Float phaseCorrelate(Rendering baseRendering, Rendering shiftedRendering) {
		
		int width = baseRendering.getWidth();
		int height = baseRendering.getHeight();

		Rendering complexConjugateOfFFTofBase = complexConjugate(FFT(window(baseRendering)));
		Rendering FFTofShiftedRendering = FFT(window(shiftedRendering));
		Rendering product = multiply(FFTofShiftedRendering,complexConjugateOfFFTofBase);
		Rendering copyOfProduct = new Rendering(product);
		Rendering R = divide(product,absolute(copyOfProduct));
		Rendering r = iFFT(R);
		Point2D.Float shift = quickFindPeak(r);
		
		// convert circular shift to regular shift
		if(shift.x > width/2)
			shift.x = shift.x-width;
		
		if(shift.y > height/2)
			shift.y = shift.y-height;
		
		shift.x = -shift.x;
		shift.y = -shift.y;
		
		// For debugging
//		if((Math.abs(shift.x)>10.0f)||(Math.abs(shift.y)>10.0f)) {
//			copyOfBaseRendering.getImagePlus().show();
//			copyOfShiftedRendering.getImagePlus().show();
//			r.getImagePlus().show();
//			copyOfBaseRendering.getCLBuffer().release();
//			copyOfShiftedRendering.getCLBuffer().release();
//		}
		logger.info("shift px: " + shift);
		r.getCLBuffer(cl).release();
		return shift;
	}
	
//	replaced by quickFindPeak
//	private Point2D.Float findPeak(Rendering rendering) {
//		int width = rendering.getWidth();
//		int height = rendering.getHeight();
//		int boxtRadius = 2;
//		
//		CLBuffer<FloatBuffer> result = pipe.getContext().createFloatBuffer(2, READ_WRITE);
//		
//		CLKernel kernel = 
//			pipe.getProgram("rendering_manipulator.cl")
//			.createCLKernel("find_peak");
//		
//		kernel
//			.putArg(rendering.getCLBuffer())
//			.putArg(result)
//			.putArg(width)
//			.putArg(height)
//			.putArg(boxtRadius);
//		long start = System.currentTimeMillis();
//		pipe.getComputationQueue()
//			.put1DRangeKernel(kernel, 0, 1, 1)
//			.finish();
//		System.out.println("peak finding: " + (System.currentTimeMillis()-start) + "ms");
//		pipe.getTransferQueue().putReadBuffer(result, true);
//		
//		result.release();
//		kernel.release();
//		
//		return new Point2D.Float(
//					result.getBuffer().get(0),
//					result.getBuffer().get(1));
//	}
	
	private Point2D.Float quickFindPeak(Rendering rendering) {
		
		CLCommandQueue queue = cl.pollQueue();
//		rendering.getImagePlus().show();
		int width = rendering.getWidth();
		int height = rendering.getHeight();
		int boxRadius = 2;
		int elementsPerWorkItem = 64;
		int workItems = width*height/elementsPerWorkItem;
		
		CLKernel firstKernel = 
				cl.getProgramManager().getProgram("rendering_manipulator.cl")
			.createCLKernel("first_quick_find_peak");
		
		CLKernel kernel = 
				cl.getProgramManager().getProgram("rendering_manipulator.cl")
			.createCLKernel("quick_find_peak");
		
		
		
		CLBuffer<?> inValues,inIndices,outValues,outIndices;
		final int fWorkItems = workItems;
		outValues = Utils.tryAllocation(new Callable<CLBuffer<?>>() {

			@Override
			public CLBuffer<?> call() throws Exception {
				return cl.getContext().createBuffer(
						4*fWorkItems, READ_WRITE);
			}
			
		}); 
				
				
		outIndices = Utils.tryAllocation(new Callable<CLBuffer<?>>() {

			@Override
			public CLBuffer<?> call() throws Exception {
				return cl.getContext().createBuffer(
						4*fWorkItems, READ_WRITE);
			}
			
		});  
				
		
		firstKernel
			.putArg(rendering.getCLBuffer(cl))
			.putArg(outValues)
			.putArg(outIndices)
			.putArg(elementsPerWorkItem);
		
		
		int itemsPerExecStep = 16384;
		int localWorkSize = 64;
		
		CLTools.steppedKernelEnqueue(queue, firstKernel, workItems, 0,
				itemsPerExecStep, localWorkSize);
		
//		clQueue
//			.put1DRangeKernel(firstKernel, 0, workItems, 0)
//			.finish();
		
		firstKernel.release();
		
		while(workItems>1) {
			if(workItems>elementsPerWorkItem) {
				workItems /= elementsPerWorkItem;
			}
			else {
				elementsPerWorkItem=workItems;
				workItems = 1;
			}
			
			inValues = outValues;
			inIndices = outIndices;
			
			final int finalWorkItems = workItems;
			outValues = Utils.tryAllocation(new Callable<CLBuffer<?>>() {

				@Override
				public CLBuffer<?> call() throws Exception {
					return cl.getContext().createBuffer(
							4*finalWorkItems, READ_WRITE);
				}
				
			}); 
					
					
			outIndices = Utils.tryAllocation(new Callable<CLBuffer<?>>() {

				@Override
				public CLBuffer<?> call() throws Exception {
					return cl.getContext().createBuffer(
							4*finalWorkItems, READ_WRITE);
				}
				
			});  
			
			kernel.rewind();
			kernel
				.putArg(inValues)
				.putArg(inIndices)
				.putArg(outValues)
				.putArg(outIndices)
				.putArg(elementsPerWorkItem);
			
			CLTools.steppedKernelEnqueue(queue, kernel, workItems, 0,
					itemsPerExecStep, localWorkSize);
			
//			clQueue
//				.put1DRangeKernel(kernel, 0, workItems, 0)
//				.finish();
			
			inValues.release();
			inIndices.release();
		}
		
		outValues.release();
		kernel.release();
		
		CLKernel refineKernel = 
				cl.getProgramManager().getProgram("rendering_manipulator.cl")
			.createCLKernel("refine_peak");
		
		CLBuffer<FloatBuffer> result = Utils
				.tryAllocation(new Callable<CLBuffer<FloatBuffer>>() {

					@Override
					public CLBuffer<FloatBuffer> call() throws Exception {
						return cl.getContext().createFloatBuffer(2, WRITE_ONLY);
					}

				});
				
				
		
		refineKernel
			.putArg(rendering.getCLBuffer(cl))
			.putArg(outIndices)
			.putArg(result)
			.putArg(width)
			.putArg(height)
			.putArg(boxRadius);
		
		logger.debug("Enqueing {}",refineKernel);
		queue
			.put1DRangeKernel(refineKernel, 0, 1, 0)
			.finish();
		logger.debug("Finished {}",refineKernel);
		
		
		queue.putReadBuffer(result, true);
		
		outIndices.release();
		result.release();
		refineKernel.release();
		
		cl.returnQueue(queue);

		return new Point2D.Float(
					result.getBuffer().get(0),
					result.getBuffer().get(1));
	}

	private Rendering FFTRadix(Rendering rendering, int radix) {
		if(radix!=2) {
			throw new Error("currently not supported radix: " + radix);
		}
		CLCommandQueue queue = cl.pollQueue();
		int width = rendering.getWidth();
		int height = rendering.getHeight();
		long globalWorkSize = (width/radix)*height;
		CLKernel kernel = 
				cl.getProgramManager().getProgram("rendering_manipulator.cl")
			.createCLKernel("fft_radix" + String.valueOf(radix));
//		long start = System.currentTimeMillis();
//		long totalC = 0L;
//		long totalA = 0L;
//		long totalT = 0L;
		int iterations = (int) Math.round(Math.log(width)/Math.log(radix));
		for(int j=0;j<2;j++) {
			for(int i=0; i<iterations;i++) {
				int p = (int)Math.pow(radix,i);
				
//				Rendering tempRendering = new Rendering(rendering);
//				long startA = System.nanoTime();
				Rendering tempRendering = new CLRendering(rendering.getWidth(), 
						rendering.getHeight(), rendering.isComplex());
//				totalA+=System.nanoTime()-startA;
				
				kernel.rewind();
				
				kernel
					.putArg(rendering.getCLBuffer(cl))
					.putArg(tempRendering.getCLBuffer(cl))
					.putArg((int)globalWorkSize)
					.putArg(p);
				
//				System.out.println("huh? " + (System.currentTimeMillis()-start) + "ms");
//				long startC = System.nanoTime();
				int itemsPerExecStep = 524288;
				int localWorkSize = 64;
				
				CLTools.steppedKernelEnqueue(queue, kernel, (int)globalWorkSize, 0,
						itemsPerExecStep, localWorkSize);
//				clQueue
//					.put1DRangeKernel(kernel, 0, globalWorkSize, 0)
//					.finish();
//				totalC+=System.nanoTime()-startC;
				
				rendering.free();
				rendering=tempRendering;
				
			}
//			long startT = System.currentTimeMillis();
			rendering = transpose(rendering);
//			totalT+=System.currentTimeMillis()-startT;
		}
		
//		System.out.println("FFT " + (System.currentTimeMillis()-start)
//				+ "ms (comp: " + (totalC/1e6) + "ms, alloc: " + (totalA/1e6) + "ms, trans: " + totalT + "ms)");
		
		kernel.release();
		cl.returnQueue(queue);
		return rendering;
	}

	private Rendering stdRenderingIOKernel(String kernelName, Rendering... inputRenderings) {
		final CLCommandQueue queue = cl.pollQueue();
		// TODO precondition check
		final int width = inputRenderings[0].getWidth();
		final int height = inputRenderings[0].getHeight();
		final CLKernel kernel = 
				cl.getProgramManager().getProgram("rendering_manipulator.cl")
			.createCLKernel(kernelName);
//		Rendering outputRendering = new Rendering();
		// TODO copy shallow of rendering, create new CLBuffer, perhaps:
//		Rendering outputRendering = new Rendering(inputRenderings[0]);
		Rendering outputRendering = new CLRendering(
				inputRenderings[0].getWidth(),
				inputRenderings[0].getHeight(),
				inputRenderings[0].isComplex());
		
		for(Rendering inputRendering : inputRenderings) {
			kernel.putArg(inputRendering.getCLBuffer(cl));
//			inputRendering.getImagePlus();
		}
		kernel.putArg(outputRendering.getCLBuffer(cl));
//		long start = System.currentTimeMillis();
		
		ExceptionTools.tryNTimes(5, new Runnable() {
			
			@Override
			public void run() {
				logger.debug("Enqueing 2D-Kernel: {}",kernel);
				queue
				.put2DRangeKernel(kernel, 0, 0, width, height, 8, 8)
				.finish();
				logger.debug("Finished {}",kernel);
			}
		});
		 // TODO perhaps replace by event check!
//		System.out.println(kernelName + "-kernel: " + (System.currentTimeMillis()-start) + "ms");
		
		// TODO really release all of them? perhaps option
		for(Rendering inputRendering : inputRenderings) {
//			inputRendering.getCLBuffer().getBuffer().clear();
			inputRendering.free();
		}
		kernel.release();
		cl.returnQueue(queue);
		return outputRendering;
	}

//	@Override
//	protected void finalize() throws Throwable {
//		cl.returnQueue(queue);
//		super.finalize();
//	}
}
