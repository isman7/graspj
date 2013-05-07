package eu.brede.graspj.opencl.programs;

import java.util.HashMap;
import java.util.Map;

public class CLProgramGaussianCC3DRenderer extends CLProgramCodeGJ {
	public CLProgramGaussianCC3DRenderer() {
		
		Map<String,String> replacements = new HashMap<String,String>();
		
		appendFunction("constants.cl");
		
		appendFunction("gaussian_cdf.cl");
		
		replacements.clear();
		replacements.put("additional_kernel_arguments", 
				getSnippet("rendering/gaussianCC3D_add_kernel_args.cl"));
		
		appendln(getSnippet("rendering/common_gaussian_kernel_header.cl"),replacements);
		appendln("{");
		
		appendSnippet("rendering/color_coding.cl");
		
		replacements.clear();
		replacements.put("update_image", 
				getSnippet("rendering/gaussianCC3D_update_image.cl"));
		
		appendln(getSnippet("rendering/common_gaussian.cl"),replacements);
		appendln("}");
	}
}
