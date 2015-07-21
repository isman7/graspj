package eu.brede.graspj.pipeline.processors.renderer;

import java.nio.ByteBuffer;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLProgram;

import eu.brede.common.config.EnhancedConfig;
import eu.brede.graspj.configs.rendering.RenderConfig;
import eu.brede.graspj.datatypes.ColorCodedRendering;
import eu.brede.graspj.datatypes.Rendering;
import eu.brede.graspj.datatypes.range.RangeComparable;
import eu.brede.graspj.opencl.programs.CLProgramCodeGJ;
import eu.brede.graspj.opencl.programs.CLProgramGaussianCC3DRenderer;

public class GaussianColorCoded3D extends CommonGaussian {
	
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GaussianColorCoded3D() {
		getConfig().put("hue", new RangeComparable<Float>(0f, 212f));
		getConfig().put("z", new RangeComparable<Float>(-500f, 500f));
	}
	
	@Override
	public RenderConfig getConfig() {
		// TODO Auto-generated method stub
		return super.getConfig();
	}

	@Override
	public void setConfig(EnhancedConfig config) {
		// TODO Auto-generated method stub
		super.setConfig(config);
	}

	@Override
	public ColorCodedRendering getRendering() {
		// TODO Auto-generated method stub
		return (ColorCodedRendering) rendering;
	}

	@Override
	protected Rendering newRendering() {
		int renderWidth = getConfig().getInt("renderWidth");
		int renderHeight = getConfig().getInt("renderHeight");
		
		return new ColorCodedRendering(renderWidth,renderHeight);
	}

	@Override
	protected CLKernel newKernel() {
		CLProgram cl3D = cl.getProgramManager().getProgram("CLProgramGaussianCC3DRenderer");
		//System.out.println(cl3D.getSource());
		return cl3D.createCLKernel("render_spots");
	}

	@Override
	protected void putSpecificKernelArgs(CLKernel kernel) {
		CLBuffer<ByteBuffer> hueBuffer = getRendering().getHue().getCLBuffer(cl);
		kernel
			.putArg(hueBuffer)
			.putArg(getConfig().<RangeComparable<Float>>gett("hue").getMin())
			.putArg(getConfig().<RangeComparable<Float>>gett("hue").getMax())
			.putArg(getConfig().<RangeComparable<Float>>gett("z").getMin())
			.putArg(getConfig().<RangeComparable<Float>>gett("z").getMax());
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

}
