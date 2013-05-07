package eu.brede.graspj.datatypes;

import java.nio.FloatBuffer;

import eu.brede.graspj.datatypes.bufferholder.BufferHolder;

/**
 * Holds the drift information in nm, frame wise.
 * 
 * Format: (frame0_driftX, frame0_driftY, frame1_driftX...)
 * 
 * @author Norman
 *
 */
public class Drift extends BufferHolder<FloatBuffer> {

	public Drift() {
		super();
	}

	public Drift(BufferHolder<FloatBuffer> bh) {
		super(bh);
	}
	
	public float[][] asArrays() {
		getBuffer().rewind();
		int numFrames = getBuffer().limit()/2;
		float[] xDrift = new float[numFrames];
		float[] yDrift = new float[numFrames];
		
		for(int i=0;i<numFrames;i++) {
			xDrift[i] = getBuffer().get();
			yDrift[i] = getBuffer().get();
		}
		
		getBuffer().rewind();
		return new float[][] { xDrift, yDrift };
	}

}
