package eu.brede.graspj.configs.rendering;

import java.awt.geom.Point2D;
import java.io.Serializable;

import eu.brede.common.config.EnhancedConfig;
import eu.brede.common.config.MetaData;
import eu.brede.graspj.configs.acquisition.AcquisitionConfig;
import eu.brede.graspj.datatypes.Option;


public class RenderConfig extends EnhancedConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RenderConfig() {
		super();
//		ensureCompliance();
//		put("metaData", new MetaData());
//		put("renderWidth", new Integer(2048));
//		put("renderHeight", new Integer(2048));
//		put("pixelSize", new Float(19.625));
//		put("offsetX", new Float(0));
//		put("offsetY", new Float(0));
//		put("firstFrame", new Integer(0));
//		put("lastFrame", Integer.MAX_VALUE);
//		put("spotsPerWorker", new Integer(1));
////		put("renderMethod", EnumRenderMethods.GAUSS_2D);
//		put("intensity", new Float(10));
//		put("bytesPerPixel", new Integer(4));
//		put("complex", new Boolean(false));
	}
	
	
	
//	@Override
//	protected Object readResolve() throws ObjectStreamException {
//		clearProperty("of0fsetY");
//		ensureCompliance();
//		return this;
//	}
	
//	public RenderConfig(EnhancedConfig config) {
//		super(config);
//	}



	@Override
	public EnhancedConfig getRequiredDefault() {
		EnhancedConfig requiredConfig = super.getRequiredDefault();
		requiredConfig.setPrefix("RenderConfig.");
		
		requiredConfig.put("metaData", new MetaData());
		requiredConfig.put("renderWidth", new Integer(2048));
		requiredConfig.put("renderHeight", new Integer(2048));
		requiredConfig.put("pixelSize", new Float(19.625));
		requiredConfig.put("offsetX", new Float(0));
		requiredConfig.put("offsetY", new Float(0));
		requiredConfig.put("firstFrame", new Integer(0));
		requiredConfig.put("lastFrame", Integer.MAX_VALUE);
		requiredConfig.put("spotsPerWorker", new Integer(1));
//		requiredConfig.put("renderMethod", EnumRenderMethods.GAUSS_2D);
		requiredConfig.put("intensity", new Float(1));
		requiredConfig.put("bytesPerPixel", new Integer(4));
		requiredConfig.put("complex", new Boolean(false));
		requiredConfig.put("showImage", new Boolean(false));
		
		
		Option specificChannel = new Option();
		specificChannel.put("channelNr", new Integer(1));
		
		Option applyMask = new Option();
		applyMask.put("specificChannel", specificChannel);
		applyMask.setSelected(true);
		
		requiredConfig.put("applyMask", applyMask);
		
		
		return requiredConfig;
	}
	
	public float calcPixelSize(AcquisitionConfig acquisitionConfig) {
		int frameWidth = acquisitionConfig.getDimensions().frameWidth;
		float pixelSize = acquisitionConfig.getFloat("pixelSize");
		return (pixelSize*frameWidth)/getInt("renderWidth");
	}
	
	public void updateOffset(Point2D.Float center) {
		float pixelSize = getFloat("pixelSize");
		float width = getInt("renderWidth")*pixelSize;
		float height = getInt("renderHeight")*pixelSize;
		float offsetX = center.x-width/2;
		float offsetY = center.y-height/2;
		put("offsetX", -offsetX);
		put("offsetY", -offsetY);
		return;
	}

	public Point2D.Float calcPosNM(int pX, int pY) {
		float pixelSize = getFloat("pixelSize");
		float nmX = (pX*pixelSize) - getFloat("offsetX");
		float nmY = (pY*pixelSize) - getFloat("offsetY");
		return new Point2D.Float(nmX,nmY);
	}

	public double getRenderedArea() {
		return getProduct("renderWidth","renderHeight","pixelSize","pixelSize");
	}
	
	public double calcRenderedFraction(RenderConfig origConfig) {
		if(origConfig==this) {
			return 1.0;
		}
		return getRenderedArea()/origConfig.getRenderedArea();
	}

	public int getNumPixels() {
		int complexFactor = getBoolean("complex")?2:1;
		return complexFactor*getProduct("renderWidth","renderHeight").intValue();
	}
	
	
}
