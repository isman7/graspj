package eu.brede.graspj.pipeline.producers;

import ij.ImagePlus;
import ij.process.ShortProcessor;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ShortBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractProductionEngine implements ProductionEngine {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final static Logger logger = LoggerFactory.getLogger(AbstractProductionEngine.class);

	public static class ImageDim implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public int frameWidth;
		public int frameHeight;
		public int bytesPerPixel;
		public int framesPerPackage;
		
		public int frameByteSize() {
			return frameWidth*frameHeight*bytesPerPixel; 
		}
	}
	
//	public abstract ImageDim getImageDim();
	
	@Override
	public ImagePlus nextImage() {
		ImageDim dim = getImageDim();
		int imagePixelSize = dim.frameWidth*dim.frameHeight;
		int imageByteSize = imagePixelSize*dim.bytesPerPixel;
	
		ShortBuffer buffer = ShortBuffer.allocate(imagePixelSize);
		try {
			readBytes(imageByteSize,buffer);
		}
		catch (IOException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
		buffer.rewind();
		short[] pixels = new short[imagePixelSize];
		buffer.get(pixels);
		
		ShortProcessor ip = new ShortProcessor(dim.frameWidth,dim.frameHeight,pixels,null);
		return new ImagePlus("", ip);
	}
}
