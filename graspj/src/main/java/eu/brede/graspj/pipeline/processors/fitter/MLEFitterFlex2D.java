package eu.brede.graspj.pipeline.processors.fitter;

public class MLEFitterFlex2D extends MLEFitter2D {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MLEFitterFlex2D() {
		super();
		clProgramFitter = "CLProgramMLEflex2DFitter";
	}
}
