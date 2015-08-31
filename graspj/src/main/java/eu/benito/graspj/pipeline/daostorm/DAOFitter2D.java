package eu.benito.graspj.pipeline.daostorm;

import static com.jogamp.opencl.CLMemory.Mem.READ_ONLY;
import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.text.DecimalFormat;
import java.util.concurrent.Callable;

//import Jama.*;

import java.math.*;

import org.apache.commons.math3.special.Erf;
import org.perf4j.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLKernel;

import eu.benito.graspj.configs.daostorm.DAOConfigOptional;
import eu.brede.common.config.EnhancedConfig;
import eu.brede.common.opencl.utils.CLResourceManager;
import eu.brede.common.opencl.utils.CLSystem;
import eu.brede.common.opencl.utils.CLTools;
import eu.brede.graspj.configs.acquisition.AcquisitionConfig;
import eu.brede.graspj.configs.fit.FitConfig;
import eu.brede.graspj.datatypes.AnalysisItem;
import eu.brede.graspj.datatypes.bufferholder.BufferHolder;
import eu.brede.graspj.datatypes.spotcollection.BufferSpotCollection;
import eu.brede.graspj.opencl.utils.CLSystemGJ;
import eu.brede.graspj.pipeline.processors.AbstractAIProcessor;
import eu.brede.graspj.pipeline.processors.finder.SpotFinderJava;
import eu.brede.graspj.utils.Buffers;
import eu.brede.graspj.utils.Utils;

public class DAOFitter2D extends AbstractAIProcessor {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	final static Logger logger = LoggerFactory.getLogger(DAOFitter2D.class);

	private FitConfig config;
	private int packageNr = 0;
	private int stepDAO = 0;
	protected String clProgramFitter = "CLProgramMLE2DFitter";

	public DAOFitter2D() {
		config = new FitConfig();
	}

	@Override
	public void process(AnalysisItem item) {
		CLResourceManager manager = new CLResourceManager();
		final CLSystem cl = CLSystemGJ.getDefault();;
		CLCommandQueue queue = cl.pollQueue();

		DAOConfigOptional daoConfigOpt = (DAOConfigOptional) getConfig().get("daoConfigOpt");
		
		BufferHolder<ShortBuffer> candidates = item.getNotes().gett(
				"candidates");
		BufferHolder<ShortBuffer> frameBufferHolder = item.getNotes()
		.<BufferHolder<ShortBuffer>> gett("frameBuffer");
		CLBuffer<ShortBuffer> frameBuffer = manager.watch(frameBufferHolder.getCLBuffer(cl));

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
		
		//System.out.println("Package " + packageNr +  " completed, fitted " + spotCount + " spots");		
		
		item.getSpots().rewindAllBuffers();
		
		if (stepDAO < (daoConfigOpt.getInt("iterations")-1)){
			stepDAO++;
		}else if (stepDAO == (daoConfigOpt.getInt("iterations")-1)){
			// try to free direct and CL memory
			item.getNotes().<BufferHolder<ShortBuffer>> gett("frameBuffer").free();
			// item.getAcquisition().getFrameBuffer().free();
			candidates.free();
	
			// remove candidates from notes, because it is no longer valid
			item.getNotes().remove("candidates");
			item.getNotes().remove("frameBuffer");
	
			// TODO don't call gc to often!
			// System.gc();
			stepDAO = 0;
			packageNr++;
		} 
		
		manager.releaseAll();
		cl.returnQueue(queue);
		
		
		
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
	
	private ShortBuffer Mat2Buff(short[][] imageMat){
		int cap = imageMat.length * imageMat[0].length;
		ShortBuffer imageBuffer = Buffers.newDirectShortBuffer(cap);
		for (int ii=0; ii<imageMat.length; ii++){
			for (int jj=0; jj<imageMat[0].length; jj++){
				imageBuffer.put(imageMat[ii][jj]);
			}
		}
		return imageBuffer;
	}
	
	
	private short[][] calcResidual(short[][] imageArr, float[][] spotsArr, float pixel_size, float offset_x, float offset_y){
		//Matrix subsMat = new Matrix(imageArr.length, imageArr[0].length);
		//short[][] gaussMat = new short[imageArr.length][imageArr[0].length];
		/*double[][] imageDouble = new double[imageArr.length][imageArr[0].length];
		for(int i = 0; i < imageArr.length; i++)
	    {
	        for(int j = 0; j < imageArr[0].length; j++) {
	        	Short value = new Short(imageArr[i][j]);
	        	imageDouble[i][j] = value.doubleValue();
	        }
	    }
		
		Matrix imageMat = new Matrix(imageDouble);*/
		short[][] subsArray = new short[imageArr.length][imageArr[0].length];
		for (int i = 0; i < spotsArr.length; i++){
			short[][] newGaussMat = calcGaussian(spotsArr[i], imageArr.length, imageArr[0].length, pixel_size, offset_x, offset_y);
			//System.out.println(imageArr.length + "; " + imageArr[0].length + "; ");
			for(int ii = 0; ii < imageArr.length; ii++) {
		        for(int jj = 0; jj < imageArr[0].length; jj++) {
		        	// The substraction must be negative, so int appears... 
		        	subsArray[ii][jj] = (short) (imageArr[ii][jj] - newGaussMat[ii][jj]);
		        }
		    }
		}
		
		return subsArray;
	}
	
	private short[][] calcGaussian(float[] gaussData, int width, int height, float pixel_size, float offset_x, float offset_y){
		short[][] calcGauss = new short[width][height];
		/* Buffer order:
		 * x, y, z, I, B, sx, sy, sz, sI, sB */
		float x  = gaussData[0];
		float y  = gaussData[1];
//		float z  = gaussData[2];
		float I  = gaussData[3];
		float B  = gaussData[4];
		float sx = gaussData[5];
		float sy = gaussData[6];
//		float sz = gaussData[7];
		float sI = gaussData[8];
		float sB = gaussData[9];
		
		if ((sx == Float.NaN) || (sy == Float.NaN)){
			return calcGauss;
		}
		
		if ((sx > 100) || (sy > 100)){
			return calcGauss;
		}

		
		float sx_px=(sx/pixel_size);
		float sy_px=(sy/pixel_size);
		int box_radius = (int) Math.ceil(3*Math.max(sx_px, sy_px));
		float sx_sqrt2 =  sx_px * (float) Math.sqrt(2);
		float sy_sqrt2 =  sy_px * (float) Math.sqrt(2);
		
		float center_x = ((x+offset_x)/pixel_size);
		float center_y = ((y+offset_y)/pixel_size);
		
		int start_x = (int) Math.max(Math.floor(center_x-box_radius),0);
		int end_x   = (int) Math.min(Math.ceil(center_x+box_radius),width-1);
		int start_y = (int) Math.max(Math.floor(center_y-box_radius),0);
		int end_y   = (int) Math.min(Math.ceil(center_y+box_radius),height-1);

		
		double x_tx_p12, x_tx_m12, y_ty_p12, y_ty_m12;
		double dE_x, dE_y;
		
		for (int i=start_y; i<end_y; i++){
			for (int j=start_x; j< end_x; j++){
				double gaussianValue = 0;
				
				/*x_tx_p12 = f_i_ti_p12(j,center_x);
				x_tx_m12 = f_i_ti_m12(j,center_x);
				
				y_ty_p12 = f_i_ti_p12(i,center_y);
				y_ty_m12 = f_i_ti_m12(i,center_y);
				
				dE_x = f_dE_i(x_tx_p12,x_tx_m12,sx_sqrt2);
				dE_y = f_dE_i(y_ty_p12,y_ty_m12,sy_sqrt2);*/
				
				dE_x = f_dE_i(j, center_x ,sx);
				dE_y = f_dE_i(i, center_y ,sy);				
				
				gaussianValue = ((I/2)-B)*dE_y*dE_x; 
				calcGauss[i][j] = (short) gaussianValue;
			}
		}
		
		
		
		return calcGauss;
	}
	
/*	private double f_dE_i(double i_ti_p12, double i_ti_m12, double s_sqrt2)
	{ 
		return 0.5d * (  Erf.erf(i_ti_p12/s_sqrt2) - Erf.erf(i_ti_m12/s_sqrt2));
	}*/
	
	private double f_dE_i(double i, double ti, double s)
	{ 
		return Math.exp(-Math.pow((i-ti)/(2*s), 2));
	}
	
	private double f_i_ti_p12 (float i, float ti)
	{
		return i-ti+0.5d;
	}
	
	private double f_i_ti_m12 (float i, float ti)
	{
		return i-ti-0.5d;
	}

	
	
	

}
