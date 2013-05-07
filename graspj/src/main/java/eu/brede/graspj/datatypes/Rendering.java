package eu.brede.graspj.datatypes;

import ij.ImagePlus;
import ij.gui.NewImage;
import ij.measure.Calibration;
import ij.process.ImageProcessor;

import java.nio.FloatBuffer;

import eu.brede.graspj.utils.Buffers;

import eu.brede.graspj.datatypes.bufferholder.BufferHolder;
import eu.brede.graspj.pipeline.processors.renderer.SpotRenderer;

// TODO perhaps make Rendering hold the BufferHolder instead of extending/being one
// the way it is now, more specific Renderings (like ColorCodedeRendering) are quite
// messed up.
public class Rendering extends BufferHolder<FloatBuffer> {
	int width;
	int height;
	boolean complex;
	// TODO make fields final?
	Rendering() {
		// this constructor should probably be removed!
	}
	
	public Rendering(int width, int height, boolean complex) {
		super();
		this.width = width;
		this.height = height;
		this.complex = complex;
		setBuffer(Buffers.newDirectFloatBuffer(getNumPixels()));
//		clean();
	}
	
	public Rendering(Rendering rendering) {
		super(rendering);
		this.width = rendering.width;
		this.height = rendering.height;
		this.complex = rendering.complex;
	}
	
	int getNumPixels() {
		int complexFactor =complex?2:1;
		return complexFactor*getWidth()*getHeight();
	}

	public Rendering(int width, int height) {
		this(width,height,false);
	}

	@Override
	public void clean() {
		getBuffer().rewind();
		while(getBuffer().hasRemaining()) {
			Buffers.putf(getBuffer(), 0f);
		}
		getBuffer().rewind();
		cl.returnQueue(cl.pollQueue().putWriteBuffer(getCLBuffer(cl), true)
			.finish());
		return;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public boolean isComplex() {
		return complex;
	}
	
	private float[] getPixels() {
		// TODO in the future check if buffer existent, create otherwise
		getBuffer().rewind();
		cl.returnQueue(cl.pollQueue().putReadBuffer(getCLBuffer(cl), true)
			.finish());
		float[] pixels = new float[getNumPixels()];
		getBuffer().rewind();
		getBuffer().get(pixels);
		getBuffer().rewind();
		return pixels;
	}
	
	protected ImagePlus getImagePlus() {
		
		float[] pixels = getPixels();
		if(isComplex()) {
			float[] newPixels = new float[pixels.length/2];
			for(int i=0; i<newPixels.length; i++) {
				newPixels[i] = pixels[2*i];
			}
			pixels = newPixels;
		}
		
		String imageTitle = "";//config.getMetaData().getName();
	    ImagePlus imagePlus = NewImage.createFloatImage(imageTitle, getWidth(), getHeight(), 1, NewImage.FILL_BLACK);
	    ImageProcessor ip = imagePlus.getProcessor();
	    ip.setPixels(pixels);
//	    Calibration calibration = new Calibration();
//	    calibration.setUnit("nm");
//	    calibration.pixelHeight = getConfig().getFloat("pixelSize");
//	    calibration.pixelWidth = calibration.pixelHeight;
//	    imagePlus.setCalibration(calibration);
	    imagePlus.setTitle("GraspJ rendering");
	    return imagePlus;
	}
	
	public ImagePlus getImagePlus(SpotRenderer renderer) {
		
		ImagePlus imagePlus = getImagePlus();
	    Calibration calibration = new Calibration();
	    calibration.setUnit("nm");
	    calibration.pixelHeight = renderer.getConfig().getFloat("pixelSize");
	    calibration.pixelWidth = calibration.pixelHeight;
	    imagePlus.setCalibration(calibration);
	    imagePlus.setTitle(renderer.getConfig().getMetaData().getName() 
	    		+ " - " + imagePlus.getTitle());
	    return imagePlus;
	}
	
}
