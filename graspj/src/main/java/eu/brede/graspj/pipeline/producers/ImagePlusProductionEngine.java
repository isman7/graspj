package eu.brede.graspj.pipeline.producers;

import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.process.ShortProcessor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ShortBuffer;

import eu.brede.common.config.Configurable;
import eu.brede.common.config.EnhancedConfig;
import eu.brede.graspj.configs.acquisition.AcquisitionConfig;
import eu.brede.graspj.utils.Utils;

public class ImagePlusProductionEngine extends AbstractProductionEngine
	implements ProductionEngine, Configurable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// make all private?
	EnhancedConfig config;
	transient ImagePlus imp;
	int currentSlice = 1;
	
	public ImagePlusProductionEngine() {
		this(new ImagePlus());
	}
	
	public ImagePlusProductionEngine(ImagePlus imp) {
//		config = new EnhancedConfig();
//		config.setProperty("stack", imp);
		
		config = new EnhancedConfig() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public EnhancedConfig getRequiredDefault() {
				EnhancedConfig requiredConfig = super.getRequiredDefault();
				requiredConfig.setPrefix("ImagePlusProductionEngine.");
				requiredConfig.put("stack", new ImagePlus());
				return requiredConfig;
			}
			
		};
		
		config.ensureCompliance();
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		imp = config.gett("stack");
		String impTitle = "";
		if(imp!=null) {
			impTitle = imp.getTitle();
		}
		config.put("stack", impTitle);
		out.defaultWriteObject();
		config.put("stack", imp);
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		ImagePlus imp = WindowManager.getImage(config.<String>gett("stack"));
		if(imp==null) {
			imp = new ImagePlus();
		}
		config.put("stack", imp);
	}

	@Override
	public int readBytes(int numBytes, ShortBuffer packageBuffer)
			throws FileNotFoundException, IOException {
		
//		imp = config.gett("stack");
		
		int bytesPerPixel = getImp().getBytesPerPixel();
		int height = getImp().getHeight();
		int width = getImp().getWidth();
		
		int bytesPerSlice = width*height*bytesPerPixel;
		
		int numSlices = numBytes/bytesPerSlice;
		int actuallyRead = 0;
		ImageStack stack = getImp().getImageStack();
		while((numSlices > 0) && (currentSlice <= stack.getSize())) {
			short[] pixels = (short[]) ((ShortProcessor)stack.
					getProcessor(currentSlice)).getPixels();
			
			packageBuffer.put(pixels);
			numSlices--;
			currentSlice++;
			actuallyRead+=bytesPerSlice;
			
		}

		if(actuallyRead<=0) {
			actuallyRead = -1;
		}
		
		return actuallyRead;
	}

	@Override
	public void setAcquisitionConfig(AcquisitionConfig config) {
		// not needed
	}

	@Override
	public EnhancedConfig getConfig() {
		return config;
	}

	@Override
	public void setConfig(EnhancedConfig config) {
		this.config = config;
	}

	@Override
	public String toString() {
		return Utils.componentToString(this);
	}

	@Override
	public void release() {
		// close ImagePlus here?
	}

	@Override
	public void reset() {
		currentSlice = 1;
	}

	@Override
	public ImageDim getImageDim() {
		ImageDim dim = new ImageDim();
		dim.frameWidth = getImp().getWidth();
		dim.frameHeight = getImp().getHeight();
		dim.bytesPerPixel = getImp().getBitDepth()/8;
		return dim;
	}

	@Override
	public int framesAvailable() {
		return getImp().getImageStackSize();
	}
	
	public ImagePlus getImp() {
		imp = config.gett("stack");
		return imp;
	}
	

}
