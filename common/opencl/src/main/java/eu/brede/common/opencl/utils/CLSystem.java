package eu.brede.common.opencl.utils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLDevice.Type;
import com.jogamp.opencl.CLPlatform;



public abstract class CLSystem implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final boolean ownsContext;
	private transient CLResourceManager resourceManager;
	private transient CLProgramManager programManager;
	private transient CLContext context;
	private transient CLDevice defaultDevice;
	private transient Queue<CLCommandQueue> queueQueue;
	
	public CLSystem(CLContext context, boolean ownsContext, CLDevice defaultDevice) {
		this.ownsContext = ownsContext;
		this.context = context;
		this.defaultDevice = defaultDevice;
		this.queueQueue = new ConcurrentLinkedQueue<>();
		initResourceManager();
		initProgramManager();
	}
	
	public CLSystem(CLContext context, boolean ownsContext) {
		this(context, ownsContext, context.getMaxFlopsDevice());
	}
	
	public CLSystem(CLContext context, boolean ownsContext, Type type) {
		this(context, ownsContext, context.getMaxFlopsDevice(type));
	}
	
	public CLSystem(CLSystem system) {
		this(system.getContext(),false,system.getDefaultDevice());
	}

	public CLDevice getDefaultDevice() {
		return defaultDevice;
	}

//	public CLSystem(ObjectChoice<Configurable> oc) {
//		this(createContextFromObjectChoice(oc),true);
//	}
	
//	public static CLSystem createWithSimpleGuess() { 
//		return new CLSystem(CLContext.create(getSimpleGuessDevice()),true);
//	}
	
	protected static CLDevice getSimpleGuessDevice() {
		CLDevice device = null;
		for(CLPlatform platform : CLPlatform.listCLPlatforms()) {
			device = platform.getMaxFlopsDevice(Type.GPU);
			if(device!=null) {
				return device;
			}
		}
		for(CLPlatform platform : CLPlatform.listCLPlatforms()) {
			device = platform.getMaxFlopsDevice(Type.CPU);
			if(device!=null) {
				return device;
			}
		}
		return device;
	}
	
//	public static CLContext createContextFromObjectChoice(ObjectChoice<Configurable> oc) {
//		CLPlatform platform = CLTools.getPlatform(oc.getChosen().toString());
//		Selection<String> deviceSelection = 
//				oc.getChosen().getConfig().gett("devices");
//		Set<CLDevice> deviceSet = CLTools.getDevices(
//				new HashSet<>(deviceSelection.getSelected()), platform);
//		return CLContext.create(deviceSet.toArray(new CLDevice[0]));
//	}

	public CLContext getContext() {
		return context;
	}
	
	public CLCommandQueue createQueueOnFastest(Type type) {
		return createQueue(context.getMaxFlopsDevice(type));
	}
	
	// Synchronized: just create one queue at a time!
	public synchronized CLCommandQueue createQueue(CLDevice device) {
		return resourceManager.watch(device.createCommandQueue());
	}
	
	private CLCommandQueue createQueueOnDefault() {
		return createQueue(defaultDevice);
	}
	
	public synchronized CLCommandQueue pollQueue() {
		CLCommandQueue queue = queueQueue.poll();
		if(queue==null) {
			queue = createQueueOnDefault();
		}
		return queue;
	}
	
	public synchronized void returnQueue(CLCommandQueue queue) {
		// TODO add validity checks? (!=null, !isReleased(), right device...)
		queueQueue.offer(queue);
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		String platformName = context.getPlatform().getName();
		Set<String> deviceNames = new HashSet<>();
		for(CLDevice device : context.getDevices()) {
			deviceNames.add(device.getName());
		}
		String defaultDeviceName = defaultDevice.getName();
		
		oos.defaultWriteObject();
		oos.writeObject(platformName);
		oos.writeObject(deviceNames);
		oos.writeObject(defaultDeviceName);
	}
	
	private void readObject(ObjectInputStream ois) 
			throws ClassNotFoundException, IOException {
		
		ois.defaultReadObject();
		String platformName = (String) ois.readObject();
		@SuppressWarnings("unchecked")
		Set<String> deviceNames = (Set<String>) ois.readObject();
		String defaultDeviceName = (String) ois.readObject();
		
		
		CLPlatform platform = CLTools.getPlatform(platformName);
		
		defaultDevice = CLTools.getDevice(defaultDeviceName, platform);

		// make sure defaultDevice is in context only once
		deviceNames.remove(defaultDeviceName);
		Set<CLDevice> devices = CLTools.getDevices(deviceNames,platform);
		devices.add(defaultDevice);		
		
		context = CLContext.create(devices.toArray(new CLDevice[0]));
		
		queueQueue = new ConcurrentLinkedQueue<>();
		initResourceManager();
		initProgramManager();
	}

	private void initResourceManager() {
		resourceManager = new CLResourceManager();
		if(ownsContext) {
			resourceManager.watch(context);
		}
	}
	
	private void initProgramManager() {
//		programManager = new CLProgramManager(context);
		programManager = newCLProgramManager(context);
		if(ownsContext) {
			resourceManager.watch(context);
		}
	}
	
	protected abstract CLProgramManager newCLProgramManager(CLContext context);

	public CLResourceManager getResourceManager() {
		return resourceManager;
	}
	
	public CLProgramManager getProgramManager() {
		return programManager;
	}

	public void refresh() {
		CLDevice[] devices = getContext().getDevices();
		getProgramManager().releaseAll();
		getResourceManager().releaseAll();
		queueQueue.clear();
		this.context = CLContext.create(devices);
		initResourceManager();
		initProgramManager();
	}

}
