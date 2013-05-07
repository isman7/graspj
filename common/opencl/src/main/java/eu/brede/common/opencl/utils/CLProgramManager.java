package eu.brede.common.opencl.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLProgram;
import com.jogamp.opencl.CLResource;


public abstract class CLProgramManager {
		
	protected HashMap<String,CLProgram> programs;
	protected CLContext context;
	
	public CLProgramManager(CLContext context) {
		this.context = context;
		programs = new HashMap<>();
	}
	
	public CLProgram getProgram(String programName) {
		if(!programs.containsKey(programName)) {
//			createProgramFromResource(programName);
			createProgram(programName);
		}
		return programs.get(programName);
	}
	
	protected abstract CLProgram createProgram(String programName);
	
	public void releaseAll() {
		synchronized(programs) {
			for(CLProgram program : programs.values()) {
				releaseOrSkip(program);
			}
			programs.clear();
		}
	}
	
	public void releaseAllExcept(String... dontReleaseThose) {
		List<String> blacklist = Arrays.asList(dontReleaseThose);
		synchronized(programs) {
			for (Iterator<String> iter = programs.keySet().iterator(); iter.hasNext(); ) {
				String programName = iter.next();
				if(!blacklist.contains(programName)) {
					releaseOrSkip(programs.get(programName));
					iter.remove();
				}
			}
		}
	}
	
	public void release(String programName) {
		releaseOrSkip(programs.get(programName));
		synchronized(programs) {			
			programs.remove(programName);
		}
	}

	
	public void releaseAllExcept(CLProgram... dontReleaseThose) {
		List<CLProgram> blacklist = Arrays.asList(dontReleaseThose);
		synchronized(programs) {
			for (Iterator<CLProgram> iter = programs.values().iterator(); iter.hasNext(); ) {
				CLProgram program = iter.next();
				if(!blacklist.contains(program)) {
					releaseOrSkip(program);
					iter.remove();
				}
			}
		}
	}
	
	public void release(CLProgram program) {
		releaseOrSkip(program);
		synchronized(programs) {			
			for (Iterator<CLProgram> iter = programs.values().iterator(); iter.hasNext(); ) {
				CLProgram curProgram = iter.next();
				if(program.equals(curProgram)) {
					iter.remove();
				}
			}
		}
	}
	
	private void releaseOrSkip(CLResource resource) {
		try {			
			if(!resource.isReleased()) {
				resource.release();
			}
		}
		catch(Exception e) {
			// ignore all problems on release (TODO remove print?)
			e.printStackTrace();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		releaseAll();
		super.finalize();
	}
	
}
