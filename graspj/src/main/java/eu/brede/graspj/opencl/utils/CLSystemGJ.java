package eu.brede.graspj.opencl.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Callable;

import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLDevice.Type;
import com.jogamp.opencl.CLPlatform;

import eu.brede.common.config.Configurable;
import eu.brede.common.config.PlainConfigurable;
import eu.brede.common.opencl.utils.CLProgramManager;
import eu.brede.common.opencl.utils.CLSystem;
import eu.brede.common.opencl.utils.CLTools;
import eu.brede.graspj.configs.User;
import eu.brede.graspj.datatypes.ObjectChoice;
import eu.brede.graspj.datatypes.Selection;
import eu.brede.graspj.utils.Utils;

public class CLSystemGJ extends CLSystem {

	@Override
	public synchronized CLCommandQueue createQueue(final CLDevice device) {
		return Utils.tryAllocation(new Callable<CLCommandQueue>() {

			@Override
			public CLCommandQueue call() throws Exception {
				return CLSystemGJ.super.createQueue(device);
			}
			
		});
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CLSystemGJ(CLContext context, boolean ownsContext,
			CLDevice defaultDevice) {
		super(context, ownsContext, defaultDevice);
		// TODO Auto-generated constructor stub
	}

	public CLSystemGJ(CLContext context, boolean ownsContext, Type type) {
		super(context, ownsContext, type);
		// TODO Auto-generated constructor stub
	}

	public CLSystemGJ(CLContext context, boolean ownsContext) {
		super(context, ownsContext);
		// TODO Auto-generated constructor stub
	}

	public CLSystemGJ(CLSystem system) {
		super(system);
		// TODO Auto-generated constructor stub
	}

	public CLSystemGJ(ObjectChoice<Configurable> oc) {
		this(createContextFromObjectChoice(oc),true);
	}

	@Override
	protected CLProgramManager newCLProgramManager(CLContext context) {
		return new CLProgramManagerGJ(context);
	}
	
	public static CLSystem createWithSimpleGuess() { 
		 return Utils.tryAllocation(new Callable<CLSystem>() {

				@Override
				public CLSystem call() throws Exception {
					return new CLSystemGJ(CLContext.create(getSimpleGuessDevice()),true);
				}
				
			});
	}
	
	public static CLContext createContextFromObjectChoice(ObjectChoice<Configurable> oc) {
		CLPlatform platform = CLTools.getPlatform(oc.getChosen().toString());
		Selection<String> deviceSelection = 
				oc.getChosen().getConfig().gett("devices");
		if(deviceSelection.getSelected().isEmpty()) {
			return Utils.tryAllocation(new Callable<CLContext>() {

				@Override
				public CLContext call() throws Exception {
					return CLContext.create(getSimpleGuessDevice());
				}
				
			});
			// TODO log warning
		}
		final Set<CLDevice> deviceSet = CLTools.getDevices(
				new HashSet<>(deviceSelection.getSelected()), platform);
		return Utils.tryAllocation(new Callable<CLContext>() {

			@Override
			public CLContext call() throws Exception {
				return CLContext.create(deviceSet.toArray(new CLDevice[0]));
			}
			
		});	
				
	}
	
	public static ObjectChoice<Configurable> createObjectChoice(CLSystem cl) {
		Vector<Configurable> choices = new Vector<>();
		Configurable chosen = null;
		for(final CLPlatform platform : CLPlatform.listCLPlatforms()) {
			Configurable platformConfig = new PlainConfigurable() {
				@Override
				public String toString() {
					return platform.getName();
				}
			};
			
			if(cl!=null) {
				String thisPlatformName = platform.getName();
				String systemPlatformName = cl.getContext().getPlatform().getName(); 
				if(systemPlatformName.equals(thisPlatformName)) {
					chosen = platformConfig;
					platformConfig.getConfig().put("devices", new Selection<String>(
							CLTools.getDeviceNames(platform.listCLDevices()),
							CLTools.getDeviceNames(cl.getContext().getDevices())));
				}
			}
			else {
				platformConfig.getConfig().put("devices", new Selection<String>(
						CLTools.getDeviceNames(platform.listCLDevices())));
			}
			
			choices.add(platformConfig);
		}
		
		ObjectChoice<Configurable> objectChoice = new ObjectChoice<>(choices);
		if(chosen!=null) {
			objectChoice.setChosen(chosen);
		}
		
		return objectChoice;
	}
	
	public static ObjectChoice<Configurable> createObjectChoice() {
		return createObjectChoice(null);
	}
	
	public static CLSystem get(Configurable configurable) {
		CLSystem cl = configurable.getConfig().gett("clSystem");
		if(cl==null) {
			cl = getDefault();
		}
		return cl;
	}
	
	public static CLSystem getDefault() {
		return User.INSTANCE.getConfig().gett("clSystem");
	}

}
