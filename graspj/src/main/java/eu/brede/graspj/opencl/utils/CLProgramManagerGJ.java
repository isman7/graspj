package eu.brede.graspj.opencl.utils;

import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLProgram;

import eu.brede.common.opencl.utils.CLProgramManager;
import eu.brede.common.util.ResourceTools;
import eu.brede.graspj.configs.Global;

public class CLProgramManagerGJ extends CLProgramManager {

	public CLProgramManagerGJ(CLContext context) {
		super(context);
	}

	@Override
	protected synchronized CLProgram createProgram(String programName) {
		String programSource;
		if(Global.getCLProgramCodes().containsKey(programName)) {
			programSource = Global.getCLProgramCodes().gett(programName).toString();
		}
		else {
			programSource = ResourceTools.getResourceAsString(
					"eu/brede/graspj/opencl/src/clprograms/" + programName);
		}
		/*System.out.println("-------Begin CL Program-------");
		System.out.println(programSource);
		System.out.println("-------End CL Program-------");*/
		CLProgram program =  context.createProgram(programSource).build();
		programs.put(programName, program);
		return program;
	}

}
