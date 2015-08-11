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

import eu.benito.graspj.configs.daostorm.DAOConfig;
import eu.brede.common.config.EnhancedConfig;
import eu.brede.common.opencl.utils.CLResourceManager;
import eu.brede.common.opencl.utils.CLSystem;
import eu.brede.common.opencl.utils.CLTools;
import eu.brede.graspj.configs.acquisition.AcquisitionConfig;
import eu.brede.graspj.configs.finding.FindConfig;
import eu.brede.graspj.configs.fit.FitConfig;
import eu.brede.graspj.datatypes.AnalysisItem;
import eu.brede.graspj.datatypes.bufferholder.BufferHolder;
import eu.brede.graspj.datatypes.spotcollection.BufferSpotCollection;
import eu.brede.graspj.opencl.utils.CLSystemGJ;
import eu.brede.graspj.pipeline.processors.AbstractAIProcessor;
import eu.brede.graspj.pipeline.processors.finder.SpotFinderJava;
import eu.brede.graspj.utils.Buffers;
import eu.brede.graspj.utils.Utils;

public class DAOStorm extends AbstractAIProcessor {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	final static Logger logger = LoggerFactory.getLogger(DAOStorm.class);

	private DAOConfig config;
	private int stepDAO = 0;
	public DAOStorm() {
		config = new DAOConfig();
	}
	
	//private SpotFinderDAO finder = new SpotFinderDAO();
	
	@Override
	public void process(AnalysisItem item) {
		
		if (stepDAO < (2-1)) {
			/* Buffer order:
			 * 
			 * x, y, z, I, B, sx, sy, sz, sI, sB
			 * 
			 * and frameNr is packageNr in this case. 
			 * 
			 * CSV order: 
			 * 
			 * x, sx, y, sy, z, sz, I, sI, B, sB, frameNr
			 * 
			 */
			/*CLResourceManager manager = new CLResourceManager();
			final CLSystem cl = CLSystemGJ.getDefault();;
			CLCommandQueue queue = cl.pollQueue();

			BufferHolder<ShortBuffer> candidates = item.getNotes().gett(
					"candidates");*/
			
			BufferHolder<ShortBuffer> frameBufferHolder = item.getNotes()
					.<BufferHolder<ShortBuffer>> gett("frameBuffer");
			ShortBuffer imageBuffer = frameBufferHolder.getBuffer();
			//System.out.println(imageBuffer.capacity());
			short[][] imageArray = Buff2Mat(imageBuffer, 
											item.getAcquisitionConfig().getDimensions().frameWidth
											);
			
			float[][] spotsArray = Buff2Mat(item.getSpots().getSpots().getBuffer(), 10);
			
			/*float pixel_size = item.getConfig().getFloat("pixelSize"); 
			float offset_x   = item.getConfig().getFloat("offsetX"); 
			float offset_y   = item.getConfig().getFloat("offsetY"); */


			short[][] residArray = calcResidual(imageArray, 
												spotsArray, 
												item.getAcquisitionConfig().getFloat("pixelSize"), 
												0, 0);
			
			//ShortBuffer residBuff = Mat2Buff(residArray);
			
			frameBufferHolder.setBuffer(Mat2Buff(residArray));
			
			item.getNotes().put("frameBuffer", frameBufferHolder);
			
			
			//item.getNotes().put("candidates", this.candidates);
			//item.getSpots().appendSpotCollection(oldSpots);
			String DAOSpotsKey = "spotsDAO" + stepDAO;
			item.getNotes().put(DAOSpotsKey, new BufferSpotCollection(item.getSpots()));
			
			stepDAO++;
			
		} else if (stepDAO == (2-1)) {
			
			for (int j = 0; j < stepDAO; j++){
				String key = "spotsDAO" + j;  
				item.getSpots().appendSpotCollection((BufferSpotCollection) item.getNotes().get(key));
			}
			stepDAO = 0;
		
		}
		
		return;
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

		short[][] subsArray = new short[imageArr.length][imageArr[0].length];
		for (int i = 0; i < spotsArr.length; i++){
			short[][] newGaussMat = calcGaussian(spotsArr[i], imageArr.length, imageArr[0].length, pixel_size, offset_x, offset_y);
			
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

	@Override
	public DAOConfig getConfig() {
		return config;
	}

	@Override
	public void setConfig(EnhancedConfig config) {
		this.config = (DAOConfig) config;
	}

	
	
	

}
