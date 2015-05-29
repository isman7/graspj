package eu.brede.graspj.configs.acquisition;

import ij.ImagePlus;

import java.io.Serializable;

import loci.formats.IFormatReader;
import eu.brede.common.config.EnhancedConfig;
import eu.brede.graspj.datatypes.cycle.Cycle;
import eu.brede.graspj.pipeline.producers.AbstractProductionEngine.ImageDim;


public class AcquisitionConfig extends EnhancedConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ImageDim dim;

	public AcquisitionConfig() {
		super();
		dim = new ImageDim();
		dim.frameHeight = 256;
		dim.frameWidth = 256;
		dim.bytesPerPixel = 2;
		dim.framesPerPackage = 1024;
//		ensureCompliance();
//		put("metaData", new MetaData());
//		put("frameWidth", new Integer(256));
//		put("frameHeight", new Integer(256));
//		put("numFrames", new Integer(512));
//		put("bytesPerPixel", new Integer(2));
//		put("pixelSize", new Float(157.0));
//		put("countConversion", new Float(0.41f));
//		put("frameCycle", new Cycle());
	}
	
	public AcquisitionConfig (ImagePlus imgPlus) {
		this();
		getMetaData().setName(imgPlus.getTitle());
//		put("frameWidth", imgPlus.getWidth());
//		put("frameHeight", imgPlus.getHeight());
//		put("numFrames", imgPlus.getNSlices());
		dim.frameWidth = imgPlus.getWidth();
		dim.frameHeight = imgPlus.getHeight();
		dim.bytesPerPixel = imgPlus.getBytesPerPixel();
		
	}
	
	public AcquisitionConfig(IFormatReader reader) {
		this();
		put("frameWidth", reader.getSizeX());
		put("frameHeight", reader.getSizeY());
//		put("numFrames", reader.getImageCount());
	}
	
//	public long getFrameByteSize() {
//		return getProduct("frameWidth","frameHeight","bytesPerPixel").longValue();
//	}
	
	@Override
	public EnhancedConfig getRequiredDefault() {
		EnhancedConfig requiredConfig = super.getRequiredDefault();
		requiredConfig.setPrefix("AcquisitionConfig.");
		
//		requiredConfig.put("metaData", new MetaData());
//		requiredConfig.put("frameWidth", new Integer(256));
//		requiredConfig.put("frameHeight", new Integer(256));
//		requiredConfig.put("numFrames", new Integer(512));
//		requiredConfig.put("bytesPerPixel", new Integer(2));
		requiredConfig.put("pixelSize", new Float(157.0));
		requiredConfig.put("countConversion", new Float(0.41f));
        requiredConfig.put("countOffset", new Integer(0));
		requiredConfig.put("frameCycle", new Cycle());
		requiredConfig.put("framesPerPackage", new Integer(1024));
		return requiredConfig;
	}
	
	
	
	public void setDimensions(ImageDim dim) {
//		put("frameWidth", dim.frameWidth);
//		put("frameHeight", dim.frameHeight);
//		put("bytesPerPixel", dim.bytesPerPixel);
		this.dim = dim;
	}
	
	public ImageDim getDimensions() {
		return dim;
	}

}