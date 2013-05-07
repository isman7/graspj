package eu.brede.graspj.pipeline.processors.renderer;

import com.jogamp.opencl.CLKernel;

import eu.brede.graspj.datatypes.Rendering;

public class Gaussian2D extends CommonGaussian {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected Rendering newRendering() {
		int renderWidth = getConfig().getInt("renderWidth");
		int renderHeight = getConfig().getInt("renderHeight");
		boolean complex = getConfig().getBoolean("complex");
		
		return new Rendering(renderWidth,renderHeight,complex);
	}

	@Override
	protected CLKernel newKernel() {
		return cl.getProgramManager().getProgram("CLProgramGaussian2DRenderer")
				.createCLKernel("render_spots");
	}

	@Override
	protected void putSpecificKernelArgs(CLKernel kernel) {
		// nothing to do here
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}
