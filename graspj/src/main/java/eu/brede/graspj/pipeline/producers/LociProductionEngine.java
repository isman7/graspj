package eu.brede.graspj.pipeline.producers;

import ij.ImagePlus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ShortBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import loci.formats.FormatException;
import loci.plugins.BF;
import eu.brede.common.config.Configurable;
import eu.brede.common.config.EnhancedConfig;
import eu.brede.graspj.configs.acquisition.AcquisitionConfig;
import eu.brede.graspj.utils.Utils;

public class LociProductionEngine extends AbstractProductionEngine
	implements ProductionEngine, Configurable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private EnhancedConfig config;
	private ImagePlusProductionEngine impProductionEngine;
	
	final static Logger logger = LoggerFactory.getLogger(LociProductionEngine.class);
	
	public LociProductionEngine() {
//		config = new EnhancedConfig();
//		config.initMetaData();
//		config.setProperty("file", new File(""));
		impProductionEngine = new ImagePlusProductionEngine();
		
		config = new EnhancedConfig() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public EnhancedConfig getRequiredDefault() {
				EnhancedConfig requiredConfig = super.getRequiredDefault();
				requiredConfig.setPrefix("LociProductionEngine.");
				
				requiredConfig.put("file", new File(""));
				return requiredConfig;
			}
			
		};
		
		config.ensureCompliance();
	}

	@Override
	public int readBytes(int numBytes, ShortBuffer packageBuffer)
			throws FileNotFoundException, IOException {
		
		int actuallyRead = -1;
		actuallyRead = impProductionEngine.readBytes(numBytes, packageBuffer);
		
		if(actuallyRead<=0) {
			
			try {
				impProductionEngine.getConfig().put("stack", reReadLoci());
			}
			catch (FormatException e) {
				logger.error(e.getMessage(), e);
				e.printStackTrace();
				Utils.showError(e);
			}

		}
		
		
		return actuallyRead;
	}
	
	private ImagePlus reReadLoci() throws FormatException, IOException {
		return BF.openImagePlus(config.<File>gett("file").getAbsolutePath())[0];
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
		// how to close source?
	}

	@Override
	public void reset() {
		impProductionEngine.reset();
	}

	@Override
	public ImageDim getImageDim() {
		return impProductionEngine.getImageDim();
	}

	@Override
	public int framesAvailable() {
		return impProductionEngine.framesAvailable();
	}
	

}
