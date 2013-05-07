package eu.brede.common.opencl.utils;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLException;
import com.jogamp.opencl.CLException.CLDeviceNotAvailableException;
import com.jogamp.opencl.CLException.CLDeviceNotFoundException;
import com.jogamp.opencl.CLException.CLPlatformNotFoundKhrException;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLPlatform;
import com.jogamp.opencl.CLVersion;

public class CLTools {
	final static Logger logger = LoggerFactory.getLogger(CLTools.class);

	public static CLDevice getDevice(String deviceName, CLPlatform platform) {
		for (CLDevice device : platform.listCLDevices()) {
			if (deviceName.equals(device.getName())) {
				return device;
			}
		}
		return null;
	}

	public static Set<CLDevice> getDevices(Set<String> deviceNames,
			CLPlatform platform) {
		Set<CLDevice> devices = new HashSet<>();
		for (CLDevice device : platform.listCLDevices()) {
			if (deviceNames.contains(device.getName())) {
				devices.add(device);
			}
		}
		return devices;
	}

	public static CLPlatform getPlatform(String platformName) {
		for (CLPlatform platform : CLPlatform.listCLPlatforms()) {
			if (platformName.equals(platform.getName())) {
				return platform;
			}
		}
		return null;
	}

	public static Set<String> getDeviceNames(CLDevice... devices) {
		Set<String> set = new HashSet<>();
		for (CLDevice device : devices) {
			set.add(device.getName());
		}
		return set;
	}

	// private static CLCommandQueue queue;

	public static void steppedKernelEnqueue(CLCommandQueue queue,
			CLKernel kernel, int itemCount, int offset, int itemsPerExecStep,
			int localWorkSize, boolean truncate) {

		// final CLCommandQueue queue = CLPipe.INSTANCE.getQueue();

		int execSteps = (int) Math.ceil((double) itemCount / itemsPerExecStep);

		int itemsRemaining = itemCount;

		for (int execStep = 0; execStep <= execSteps; execStep++) {

			// int stepOffset = offset + execStep * itemsPerExecStep;
			int stepOffset = itemCount - itemsRemaining;
			int stepItemCount = itemsPerExecStep;

			// if (stepOffset + stepItemCount > itemCount) {
			if (stepItemCount > itemsRemaining) {
				// stepItemCount = itemCount - stepOffset;
				stepItemCount = itemsRemaining;

				if (execStep >= execSteps) {
					localWorkSize = 0;
				} else {
					int sscr = (int) Math.floor(stepItemCount / localWorkSize);
					stepItemCount = sscr * localWorkSize;
				}

				if (truncate) {
					execStep++;
				}
			}

			// try {
			// Thread.sleep(1,100);
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// System.out.print(".");
			// System.out.println(kernel.name + ": step: " + (execStep+1) +
			// " stepSpotOffset: " + stepSpotOffset + " stepSpotCount: " +
			// stepSpotCount);
			int numExceptions = 0;
			int maxExceptions = 5;

			if (stepItemCount > 0) {

				// TODO sloppy fix for CL_INVALID_WORK_GROUP_SIZE error:

				while (numExceptions <= maxExceptions) {
					try {
						logger.debug(
								"Enqueing {} with Offset {} and ItemCount {}",
								new Object[] { kernel, stepOffset,
										stepItemCount });
						queue.put1DRangeKernel(kernel, stepOffset,
//								stepItemCount, localWorkSize).flush();
								stepItemCount, localWorkSize).finish();
						logger.debug("Finished {}",kernel);
						break;
					}
					catch (CLException e) {
						logger.warn("Execution of {} failed", kernel);
						logger.warn(e.getMessage(),e);
						if (logger.isDebugEnabled()) {
							e.printStackTrace();
						}
						if (numExceptions < maxExceptions) {
							logger.warn("Retrying... (attempt {} of {})",
									(numExceptions + 1), maxExceptions);
							numExceptions++;
						}
						else {
							if(numExceptions == maxExceptions) {								
								localWorkSize = 0;
								logger.warn("Final attempt with different approach...");
							}
							else {
								numExceptions++;
								// Permanent fail, re-throw e, caller has to deal with it!
								throw e;
							}
						}
					}
				}
			}
			queue.finish();

			itemsRemaining = itemsRemaining - stepItemCount;
			// System.out.println(kernel.name + ": step: " + (execStep+1) +
			// " done");

		}
		// System.out.println();
	}

	public static void steppedKernelEnqueue(CLCommandQueue queue,
			CLKernel fitKernel, int itemCount, int itemOffset,
			int itemsPerExecStep, int localWorkSize) {

		// default: don't truncate!
		steppedKernelEnqueue(queue, fitKernel, itemCount, itemOffset,
				itemsPerExecStep, localWorkSize, false);
	}
	
	public static class CLWrongVersionException extends CLException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public final CLVersion requiredVersion;
		public final CLVersion foundVersion;
		
		public CLWrongVersionException(CLVersion foundVersion,
				CLVersion requiredVersion) {
			super("found version: " + foundVersion + ", required version: "
					+ requiredVersion);
			this.foundVersion = foundVersion;
			this.requiredVersion = requiredVersion;
		}

	}

	public static boolean availability(CLVersion minVersion) throws UnsatisfiedLinkError,
			CLPlatformNotFoundKhrException, CLDeviceNotFoundException,
			CLDeviceNotAvailableException, CLWrongVersionException {

		boolean platformExists = false;
		boolean deviceExists = false;
		boolean deviceAvailable = false;
		CLVersion foundVersion = null;

		CLPlatform[] platforms = CLPlatform.listCLPlatforms();

		for (CLPlatform platform : platforms) {
			platformExists = true;
			try {
				CLDevice[] devices = platform.listCLDevices();

				for (CLDevice device : devices) {
					deviceExists = true;
					try {						
						if (device.isAvailable()) {
							deviceAvailable = true;
							foundVersion = device.getVersion();
							if(device.getVersion().isAtLeast(minVersion)) {
								return true;								
							}
						}
					}
					catch (Exception e) {
						// ignore all exceptions at this level to allow trying all devices
					}
				}
			}
			catch (Exception e) {
				// ignore all exceptions at this level to allow trying all platforms
			}
		}

		if (platformExists) {
			if (deviceExists) {
				if(deviceAvailable) {
					throw new CLWrongVersionException(foundVersion,minVersion);
				}
				else {					
					throw new CLDeviceNotAvailableException(
							"CLDevices were found, but none was available");
				}
			}
			else {
				throw new CLDeviceNotFoundException(
						"CLPlatforms were found, but no device existed");
			}
		}
		else {
			throw new CLPlatformNotFoundKhrException(
					"The native JOCL library was loaded successfully, but no platform was found");
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}

}
