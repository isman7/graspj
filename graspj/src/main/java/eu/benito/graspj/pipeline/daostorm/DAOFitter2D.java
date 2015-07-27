package eu.benito.graspj.pipeline.daostorm;

import static com.jogamp.opencl.CLMemory.Mem.READ_ONLY;
import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.text.DecimalFormat;
import java.util.concurrent.Callable;
import java.util.Arrays;

import Jama.*;

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

public class DAOFitter2D extends AbstractAIProcessor {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	final static Logger logger = LoggerFactory.getLogger(DAOFitter2D.class);

	private FitConfig config;
	private int packageNr = 0;
	protected String clProgramFitter = "CLProgramMLE2DFitter";

	public DAOFitter2D() {
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
		
		System.out.println("Package " + packageNr +  " completed, fitted " + spotCount + " spots");		
		//System.out.println(item.getSpots().getSpots().getBuffer().get());
		//Trying to print all the data in the buffer
		
		//FloatBuffer bufferSpots = item.getSpots().getSpots().getBuffer();
		/*int ii = 0;
		while (bufferSpots.hasRemaining() && (ii<20)){
			System.out.println(bufferSpots.get());
			ii++;
		}*/
		
		/* Buffer order:
		 * 
		 * x, y, z, sx, sy, sz, I, B, sI, sB
		 * 
		 * and frameNr is packageNr in this case. 
		 * 
		 * CSV order: 
		 * 
		 * x, sx, y, sy, z, sz, I, sI, B, sB, frameNr
		 * 
		 */
		
		BufferHolder<ShortBuffer> frameBufferHolder = item.getNotes().gett("frameBuffer");
		ShortBuffer imageBuffer = frameBufferHolder.getBuffer();
		//System.out.println(imageBuffer.capacity());
		short[][] imageArray = Buff2Mat(imageBuffer, 256);
		
		float[][] spotsArray = Buff2Mat(item.getSpots().getSpots().getBuffer(), 10);
		
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
	
	
	private float[][] Buff2Mat(FloatBuffer buffer, int jj){
		int ii = buffer.capacity()/jj;
		float[][] matrix = new float[ii][jj];
		for (int i=0; i<ii; i++){
			for (int j=0; j<jj; j++){
				matrix[i][j] = buffer.get(); 
			}
		}
		
		return matrix;	
		
	}
	
	private short[][] Buff2Mat(ShortBuffer buffer, int jj){
		int ii = buffer.capacity()/jj;
		short[][] matrix = new short[ii][jj];
		for (int i=0; i<ii; i++){
			for (int j=0; j<jj; j++){
				matrix[i][j] = buffer.get(); 
			}
		}
		
		return matrix;	
		
	}
	
	private short[][] fitAndSubs(short[][] imageArr, float[][] spotsArr){
		//Matrix subsMat = new Matrix(imageArr.length, imageArr[0].length);
		Matrix gaussMat = new Matrix(imageArr.length, imageArr[0].length);
		/*double[][] imageDouble = new double[imageArr.length][imageArr[0].length];
		for(int i = 0; i < imageArr.length; i++)
	    {
	        for(int j = 0; j < imageArr[0].length; j++) {
	        	Short value = new Short(imageArr[i][j]);
	        	imageDouble[i][j] = value.doubleValue();
	        }
	    }
		
		Matrix imageMat = new Matrix(imageDouble);*/
		
		for (int i = 0; i < spotsArr.length; i++){
			
			gaussMat = gaussMat.plus(calcGaussian(spotsArr[i], imageArr.length, imageArr[0].length));
		}
		
		double[][] gaussArr = gaussMat.getArray();
		
		short[][] subsArray = new short[imageArr.length][imageArr[0].length];
		return subsArray;
	}
	
	private Matrix calcGaussian(float[] gaussData, int width, int heigth){
		Matrix calcGauss = new Matrix(width, heigth);
		/* Buffer order:
		 * x, y, z, sx, sy, sz, I, B, sI, sB */
		float x  = gaussData[0];
		float y  = gaussData[1];
//		float z  = gaussData[2];
		float sx = gaussData[3];
		float sy = gaussData[4];
//		float sz = gaussData[5];
		float I  = gaussData[6];
		float B  = gaussData[7];
		float sI = gaussData[8];
		float sB = gaussData[9];
		
		if ((sx == Float.NaN) || (sy == Float.NaN)){
			return calcGauss;
		}
		
		if ((sx > 100) || (sy > 100)){
			return calcGauss;
		}
		
		for (int i=0; i<width; i++){
			for (int j=0; j<heigth; j++){
				double gaussianValue = 0;
				calcGauss.set(i, j, calcGauss.get(i, j) + gaussianValue);
			}
		}
		
		
		
		return calcGauss;
	}

}
