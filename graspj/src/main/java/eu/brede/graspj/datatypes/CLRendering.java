package eu.brede.graspj.datatypes;

import java.nio.FloatBuffer;
import java.util.concurrent.Callable;

import com.jogamp.opencl.CLBuffer;

import eu.brede.graspj.utils.Buffers;
import eu.brede.graspj.utils.Utils;

public class CLRendering extends Rendering {
	private int bytesPerPixel = 4;
	
	@SuppressWarnings("unchecked")
	public CLRendering(int width, int height, boolean complex) {
		this.width = width;
		this.height = height;
		this.complex = complex;
		clBuffer = Utils.tryAllocation(new Callable<CLBuffer<FloatBuffer>>() {

			@Override
			public CLBuffer<FloatBuffer> call() throws Exception { 
				return (CLBuffer<FloatBuffer>) 
						cl.getContext().createBuffer(getNumPixels()*bytesPerPixel, flags);
			}
			
		});
	}
	@Override
	public FloatBuffer getBuffer() {
		if(super.getBuffer()==null) {
			int numElements = (int) (getCLBuffer(cl).getCLSize()/bytesPerPixel);
			setCLBuffer(getCLBuffer(cl).cloneWith(
					Buffers.newDirectFloatBuffer(numElements)));
			cl.returnQueue(cl.pollQueue().putReadBuffer(getCLBuffer(cl), true)
				.finish());		
		}
		return super.getBuffer();
	}
	
	
	
	// TODO implement getBuffer() that clones CLBuffer with FloatBuffer
}
