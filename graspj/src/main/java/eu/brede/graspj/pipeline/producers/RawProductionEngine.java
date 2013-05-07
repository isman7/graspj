package eu.brede.graspj.pipeline.producers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import eu.brede.common.config.Configurable;
import eu.brede.common.config.EnhancedConfig;
import eu.brede.graspj.configs.acquisition.AcquisitionConfig;
import eu.brede.graspj.utils.Utils;

public class RawProductionEngine extends AbstractProductionEngine implements
		ProductionEngine, Configurable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private EnhancedConfig config;
	private transient InputStream stream;

	public RawProductionEngine() {
		// config = new EnhancedConfig();
		// // config.initMetaData();
		// config.setProperty("file", new File(""));

		config = new EnhancedConfig() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public EnhancedConfig getRequiredDefault() {
				EnhancedConfig requiredConfig = super.getRequiredDefault();
				requiredConfig.setPrefix("RawProductionEngine.");

				requiredConfig.put("file", new File(""));
				requiredConfig.put("frameWidth", new Integer(256));
				requiredConfig.put("frameHeight", new Integer(256));
				requiredConfig.put("bytesPerPixel", new Integer(2));
				return requiredConfig;
			}

		};

		config.ensureCompliance();
	}

	@Override
	public int readBytes(int numBytes, ShortBuffer packageBuffer)
			throws FileNotFoundException, IOException {

		byte[] readBytes = new byte[(int) numBytes];

		int actuallyRead = -1;

		if (stream == null) {
			initStream();
		}
		// TODO perhaps check first if data is available (because
		// stream.read() is blocking!)
		actuallyRead = stream.read(readBytes);
		if (actuallyRead > 0) {
			ByteBuffer tempByteBuffer = ByteBuffer.wrap(readBytes);
			tempByteBuffer.rewind();
			tempByteBuffer.limit(actuallyRead);
			packageBuffer.put(tempByteBuffer.asShortBuffer());
			tempByteBuffer.clear();
			tempByteBuffer = null;
		}

		return actuallyRead;
	}

	private void initStream() throws FileNotFoundException {
		stream = new FileInputStream(config.<File> gett("file"));
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
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void reset() {
		release();
		stream = null;
	}

	@Override
	public ImageDim getImageDim() {
		ImageDim dim = new ImageDim();
		dim.frameWidth = getConfig().gett("frameWidth");
		dim.frameHeight = getConfig().gett("frameHeight");
		dim.bytesPerPixel = getConfig().gett("bytesPerPixel");
		return dim;
	}

	@Override
	public int framesAvailable() {
		int numFrames = 0;
		try {
			if (stream == null) {
				initStream();
			}
			numFrames = stream.available()/getImageDim().frameByteSize();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// log warning instead
		}
		return numFrames;
	}

}
