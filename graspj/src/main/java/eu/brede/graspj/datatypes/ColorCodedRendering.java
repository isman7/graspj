package eu.brede.graspj.datatypes;

import ij.ImagePlus;
import ij.gui.NewImage;
import ij.plugin.ContrastEnhancer;
import ij.process.ColorProcessor;
import ij.process.ImageConverter;

import java.nio.ByteBuffer;
import java.util.Arrays;

import eu.brede.graspj.utils.Buffers;

import eu.brede.graspj.datatypes.bufferholder.BufferHolder;

// TODO does append need to be overridden?
public class ColorCodedRendering extends Rendering {
	
	private BufferHolder<ByteBuffer> hue = new BufferHolder<ByteBuffer>() {
		@Override
		public void clean() {
			getBuffer().rewind();
			while(getBuffer().hasRemaining()) {
				Buffers.putb(getBuffer(), (byte)0);
			}
			getBuffer().rewind();
			cl.returnQueue(cl.pollQueue().putWriteBuffer(getCLBuffer(cl), true)
				.finish());
			return;
		}
	};
	
	public ColorCodedRendering(int width, int height) {
		super(width,height,false);
		getHue().setBuffer(Buffers.newDirectByteBuffer(getNumPixels()));
	}
	
	
	@Override
	public void clean() {
		super.clean();
		hue.clean();
	}

	@Override
	protected ImagePlus getImagePlus() {
		ImagePlus imagePlus = super.getImagePlus();
	    
	    
		new ContrastEnhancer().stretchHistogram(imagePlus.getProcessor(), 0.5);
		ImageConverter ic = new ImageConverter(imagePlus);
		ic.convertToGray8();
		
	    
		ImagePlus ccImagePlus = NewImage.createRGBImage(
	    		imagePlus.getTitle(), getWidth(), getHeight(), 1, NewImage.FILL_BLACK);
	    
	    ColorProcessor cp = (ColorProcessor)ccImagePlus.getProcessor();
	    
	    byte[] fullSaturation = new byte[width*height];
		Arrays.fill(fullSaturation,(byte)255);
		
//		byte[] nullBrightness = new byte[width*height];
		
		cp.setHSB(getHueArray(), fullSaturation, (byte[]) imagePlus.getProcessor().getPixels());
//		cp.setBrightness((FloatProcessor) imagePlus.getProcessor());
		
	    return ccImagePlus;
	}

	private byte[] getHueArray() {
		hue.getBuffer().rewind();
		cl.returnQueue(cl.pollQueue().putReadBuffer(hue.getCLBuffer(cl), true)
			.finish());
		hue.getBuffer().rewind();
		byte[] hueArray = new byte[hue.getBuffer().limit()];
		hue.getBuffer().get(hueArray);
		return hueArray;
	}

	public BufferHolder<ByteBuffer> getHue() {
		return hue;
	}
	
	
}
