package eu.brede.graspj.opencl.programs;

import eu.brede.common.opencl.programs.CLProgramCode;
import eu.brede.common.util.ResourceTools;

public class CLProgramCodeGJ extends CLProgramCode {
	
	private String baseDir = "eu/brede/graspj/opencl/src/";
	
	public CLProgramCodeGJ appendGJResource(String resourceURI) {
		appendln(ResourceTools.getResourceAsString(baseDir + resourceURI));
		return this;
	}
	
	public CLProgramCodeGJ appendSnippet(String snippetURI) {
		appendln(ResourceTools.getResourceAsString(baseDir + "snippets/" + snippetURI));
		return this;
	}
	
	public CLProgramCodeGJ appendFunction(String functionURI) {
		appendln(ResourceTools.getResourceAsString(baseDir + "functions/" + functionURI));
		return this;
	}
	
	
	public String getSnippet(String snippetURI) {
		return ResourceTools.getResourceAsString(baseDir + "snippets/" + snippetURI);
	}
	
	public String getFunction(String functionURI) {
		return ResourceTools.getResourceAsString(baseDir + "functions/" + functionURI);
	}
}
