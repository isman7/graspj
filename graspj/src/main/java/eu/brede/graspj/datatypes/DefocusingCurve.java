package eu.brede.graspj.datatypes;

import java.nio.FloatBuffer;

import eu.brede.graspj.utils.Buffers;

import eu.brede.common.config.EnhancedConfig;

public class DefocusingCurve extends EnhancedConfig {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public DefocusingCurve() {
		put("s0", Float.valueOf(0));
		put("g", Float.valueOf(0));
		put("d", Float.valueOf(0));
		put("A", Float.valueOf(0));
		put("B", Float.valueOf(0));
	}
	
	public FloatBuffer asBuffer() {
		FloatBuffer buffer = Buffers.newDirectFloatBuffer(5);
		buffer.put(getFloat("s0"));
		buffer.put(getFloat("g"));
		buffer.put(getFloat("d"));
		buffer.put(getFloat("A"));
		buffer.put(getFloat("B"));
		buffer.rewind();
		return buffer;
	}
	
}
