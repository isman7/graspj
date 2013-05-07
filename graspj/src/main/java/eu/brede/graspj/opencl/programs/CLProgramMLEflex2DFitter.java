package eu.brede.graspj.opencl.programs;

public class CLProgramMLEflex2DFitter extends CLProgramCodeGJ {
	public CLProgramMLEflex2DFitter() {
		appendFunction("constants.cl");
		appendFunction("swap_ushort.cl");
		appendFunction("median_ushort.cl");
		
		appendFunction("gaussian_cdf.cl");
		appendFunction("newton_raphson_correction_terms.cl");
		appendFunction("likelihood_derivatives.cl");
		appendFunction("likelihood_derivatives_3Dext.cl");
//		appendFunction("psfmodel_derivatives_sigma.cl");
		appendFunction("inverse_matrix_5x5.cl");
		
		appendSnippet("fitting/MLE2D_kernel_header.cl");
		appendln("{");
		appendSnippet("fitting/common_first_var_defs.cl");
		appendSnippet("fitting/common_pre_estimation.cl");
//		appendSnippet("fitting/MLEflex2D_NR.cl");
		appendSnippet("fitting/MLE2D_elliptical_NR.cl");
		appendln("}");
	}
}
