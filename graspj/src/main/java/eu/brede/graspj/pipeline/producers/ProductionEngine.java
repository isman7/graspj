package eu.brede.graspj.pipeline.producers;

import ij.ImagePlus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ShortBuffer;

import eu.brede.graspj.configs.acquisition.AcquisitionConfig;
import eu.brede.graspj.pipeline.producers.AbstractProductionEngine.ImageDim;

public interface ProductionEngine extends Serializable {
	public int readBytes(int numBytes, ShortBuffer buffer)
			throws FileNotFoundException, IOException;

	public void setAcquisitionConfig(AcquisitionConfig config);

	public ImageDim getImageDim();

	public ImagePlus nextImage();

	public void reset();

	public void release();

	// only a guess!
	public int framesAvailable();
}
