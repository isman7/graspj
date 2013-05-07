package eu.brede.graspj.opencl.programs;

import java.util.HashMap;
import java.util.Map;

public class CLProgramGaussian2DRenderer extends CLProgramCodeGJ {
	public CLProgramGaussian2DRenderer() {
		
		Map<String,String> replacements = new HashMap<String,String>();
		
		appendFunction("constants.cl");
		
		appendFunction("gaussian_cdf.cl");
		
		appendSnippet("rendering/common_gaussian_kernel_header.cl");
		appendln("{");
		replacements.clear();
		replacements.put("update_image", getSnippet("rendering/gaussian2D_update_image.cl"));
		appendln(getSnippet("rendering/common_gaussian.cl"),replacements);
		appendln("}");
	}
}
