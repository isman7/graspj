package eu.brede.graspj.opencl.programs;

public class CLProgramMLE3DFitter extends CLProgramCodeGJ {
	public CLProgramMLE3DFitter() {
		appendFunction("constants.cl");
		appendFunction("swap_ushort.cl");
		appendFunction("median_ushort.cl");
		
		appendFunction("gaussian_cdf.cl");
		appendFunction("newton_raphson_correction_terms.cl");
		appendFunction("likelihood_derivatives.cl");
		appendFunction("likelihood_derivatives_3Dext.cl");
		appendFunction("inverse_matrix_5x5.cl");
		
		appendSnippet("fitting/MLE3D_kernel_header.cl");
		appendln("{");
		appendSnippet("fitting/common_first_var_defs.cl");
		appendSnippet("fitting/MLE3D_second_var_defs.cl");
		appendSnippet("fitting/common_pre_estimation.cl");
		appendSnippet("fitting/MLE3D_NR.cl");
		appendln("}");
//		System.out.println();
//		System.out.println();
//		System.out.println(this);
//		System.out.println();
//		System.out.println();
	}
}
