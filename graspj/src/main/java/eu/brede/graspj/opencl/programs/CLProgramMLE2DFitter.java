package eu.brede.graspj.opencl.programs;

public class CLProgramMLE2DFitter extends CLProgramCodeGJ {
	public CLProgramMLE2DFitter() {
		appendFunction("constants.cl");
		appendFunction("swap_ushort.cl");
		appendFunction("median_ushort.cl");
		
		appendFunction("gaussian_cdf.cl");
		appendFunction("newton_raphson_correction_terms.cl");
		appendFunction("likelihood_derivatives.cl");
		appendFunction("CRLB_from_fisher_matrix_4x4.cl");
		
		appendSnippet("fitting/MLE2D_kernel_header.cl");
		appendln("{");
		appendSnippet("fitting/common_first_var_defs.cl");
		appendSnippet("fitting/common_pre_estimation.cl");
		appendSnippet("fitting/MLE2D_NR.cl");
		appendln("}");
	}
}
