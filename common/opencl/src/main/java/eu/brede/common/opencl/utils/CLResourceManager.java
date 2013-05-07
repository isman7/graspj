package eu.brede.common.opencl.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.jogamp.opencl.CLResource;


public class CLResourceManager {
		
	private Set<CLResource> resources; 
	
	public CLResourceManager() {
		resources = Collections.synchronizedSet(new HashSet<CLResource>());
	}
	
	public <R extends CLResource> R watch(R resource) {
		synchronized(resources) {			
			resources.add(resource);
		}
		return resource;
	}
	
	public void releaseAll() {
		synchronized(resources) {
			for(CLResource resource : resources) {
				releaseOrSkip(resource);
			}
			resources.clear();
		}
	}
	
	public void releaseAllExcept(CLResource... dontReleaseThose) {
		List<CLResource> blacklist = Arrays.asList(dontReleaseThose);
		synchronized(resources) {
			for (Iterator<CLResource> iter = resources.iterator(); iter.hasNext(); ) {
				CLResource resource = iter.next();
				if(!blacklist.contains(resource)) {
					releaseOrSkip(resource);
					iter.remove();
				}
			}
		}
	}
	
	public void release(CLResource resource) {
		releaseOrSkip(resource);
		synchronized(resources) {			
			resources.remove(resource);
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
