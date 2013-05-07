package eu.brede.graspj.gui;

import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
import ij.IJ;
import ij.plugin.PlugIn;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.lang.WordUtils;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jogamp.opencl.CLException.CLDeviceNotAvailableException;
import com.jogamp.opencl.CLException.CLDeviceNotFoundException;
import com.jogamp.opencl.CLException.CLPlatformNotFoundKhrException;

import eu.brede.common.io.XMLSaver;
import eu.brede.common.io.easy.ExtFileChooser;
import eu.brede.common.opencl.utils.CLTools;
import eu.brede.common.opencl.utils.CLTools.CLWrongVersionException;
import eu.brede.common.util.ExceptionTools.OperationCanceledException;
import eu.brede.common.util.StringTools;
import eu.brede.graspj.configs.Global;
import eu.brede.graspj.configs.User;
import eu.brede.graspj.configs.UserConfig;
import eu.brede.graspj.datatypes.CommandRunner;
import eu.brede.graspj.gui.views.AnalysisItemView;
import eu.brede.graspj.gui.views.CmdMenu;
import eu.brede.graspj.gui.views.ConfigView;
import eu.brede.graspj.gui.views.ViewManager;
import eu.brede.graspj.gui.views.WorkflowView;
import eu.brede.graspj.gui.views.WorkflowWizardView;
import eu.brede.graspj.utils.Utils;

public class GraspJ implements PlugIn, CommandRunner {
	
	private ExtFileChooser fileChooser;
	private JFrame frmGraspj;
	private ViewManager viewManager;
	private static ArrayList<GraspJ> instances;
	
	final static Logger logger = LoggerFactory.getLogger(GraspJ.class);

	@Override
	public void run(String arg) {

		logger.info("GraspJ started");

		// Java2.setSystemLookAndFeel();

		frmGraspj.setVisible(true);
		frmGraspj.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		frmGraspj.setResizable(false);
		frmGraspj.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent e) {
				logger.info("userConfig saved");
//				BinarySaver saver = new BinarySaver() {
				final UserConfig userConfig = User.INSTANCE.getConfig();
				XMLSaver<UserConfig> saver = new XMLSaver<UserConfig>() {

					@Override
					public UserConfig getObject() {
						return userConfig;
					}

				};
				try {
					saver.saveTo(new File("graspj.config"));
				} catch (IOException exception) {
					// TODO Auto-generated catch block
					exception.printStackTrace();
				}

				if(logger.isDebugEnabled()) {
					for (LinkedHashSet<String> set : StringTools.missingKeys
							.values()) {
						System.out.println();
						System.out.println();
						System.out.println("BEGIN NEW BUNDLE");
						System.out.println();
						System.out.println();

						for (String key : set) {
							String[] splitKey = key.split("\\.");
							String lastSplit = "";
							if (splitKey.length > 0) {
								lastSplit = splitKey[splitKey.length - 1];
							} else {
								lastSplit = key;
							}
							System.out.println(key
									+ " = "
									+ WordUtils.capitalize(StringTools
											.splitCamelCase(lastSplit)));
						}
					}
				}
				super.windowClosed(e);
				logger.info("GraspJ closed");
			}

		});
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
//		IJ.showMessage("test");
//		Java2.setSystemLookAndFeel();
		try {
			(new GraspJ()).run(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the application.
	 * 
	 * @wbp.parser.entryPoint
	 */
	public GraspJ() {
		if (instances == null) {
			instances = new ArrayList<>();
		}
		instances.add(this);
		
		
		if(Utils.JAVA_VERSION < Global.MIN_JAVA_VERSION) {
			Map<String,String> replacements = new HashMap<String,String>();
			replacements.put("MIN_JAVA_VERSION", String.valueOf(Global.MIN_JAVA_VERSION));
			replacements.put("JAVA_VERSION", String.valueOf(Utils.JAVA_VERSION));
			Utils.showMessage("GraspJ.tooLowJava", replacements);
			logger.warn("Java version too low (running {}, required {}",
					Utils.JAVA_VERSION, Global.MIN_JAVA_VERSION);
		}
		
		boolean availability = false;
		final Map<String,String> replacements = new HashMap<String,String>();
		try {
			availability = CLTools.availability(Global.MIN_CL_VERSION);
		}
		catch (UnsatisfiedLinkError e) {
			logger.error(e.getMessage(), e);
			Utils.showError("CLTools.availability.noNativeLib", e);
		}
		catch (CLPlatformNotFoundKhrException e) {
			logger.error(e.getMessage(), e);
			Utils.showError("CLTools.availability.noPlatform", e);
		}
		catch (CLDeviceNotFoundException e) {
			logger.error(e.getMessage(), e);
			Utils.showError("CLTools.availability.noDevice", e);
		}
		catch (CLDeviceNotAvailableException e) {
			logger.error(e.getMessage(), e);
			Utils.showError("CLTools.availability.noAvailableDevice", e);
		}
		catch (CLWrongVersionException e) {
			replacements.put("requiredVersion", e.requiredVersion.toString());
			replacements.put("foundVersion", e.foundVersion.toString());
			logger.error(e.getMessage(), e);
			Utils.showError("CLTools.availability.wrongCLVersion", replacements, e);
		}
		catch (Exception e) {
			logger.error(e.getMessage(), e);
			Utils.showError("CLTools.availability.other", e);
		}
		finally {
			if(!availability) {
				// close GraspJ
			}
		}
		
		
		UncaughtExceptionHandler eh = new UncaughtExceptionHandler() {
			
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				logger.error(e.getMessage(), e);
//				replacements.put("errorMsg", e.toString());
				Utils.showError("GraspJ.unhandledException", e);
//				Utils.showError(e);
				e.printStackTrace();
			}
		};
		Thread.setDefaultUncaughtExceptionHandler(eh);
		
//		if(!availability) {
//			replacements.put("errorMsg", "Unknown OpenCL-error");
//			logger.error("Unknown OpenCL-error");
//			Utils.errorIJ("CLTools.availability.unknown", replacements);
//		}
		
		// Java2.setSystemLookAndFeel();
		try {
			UIManager
					.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
			// UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");

		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LookAndFeelAddons.contribute(new W7TaskPaneAddon());
		// UIManager.put("Panel.background", Color.WHITE);

		// UIManager.
		ToolTipManager.sharedInstance().setDismissDelay(15000);
		// ToolTipManager.sharedInstance().setReshowDelay(time in ms);
		// ToolTipManager.sharedInstance().setInitialDelay(time in ms);

		initialize();
//		CLPipe.INSTANCE.getContext();
		// viewManager.switchView(new ConsoleView());
	}

	public static ArrayList<GraspJ> getInstances() {
		return new ArrayList<GraspJ>(instances);
	}
	
	public JFrame getMainFrame() {
		return frmGraspj;
	}

//	public static UserConfig getUserConfig() {
////		if (!(userConfig instanceof UserConfig)) {
//		if (userConfig == null) {
//			File userConfigFile = new File("graspj.config");
//			// if(userConfigFile.exists()) {
//			// BinaryLoader<UserConfig> loader = new BinaryLoader<>();
//			// userConfig = loader.loadFrom(userConfigFile);
//			// }
//			// else {
//			// userConfig = new UserConfig();
//			// }
//			try {
////				BinaryLoader<UserConfig> loader = new BinaryLoader<>();
//				XMLLoader<UserConfig> loader = new XMLLoader<>();
//				userConfig = loader.loadFrom(userConfigFile);
//			} catch (Exception e) {
//				e.printStackTrace();
//				userConfig = new UserConfig();
//			}
//		}
//		return userConfig;
//	}

	public ViewManager getViewManager() {
		return viewManager;
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		fileChooser = new ExtFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(false);
		frmGraspj = new JFrame();

		viewManager = new ViewManager(frmGraspj);
		viewManager.getSupportedViews().add(AnalysisItemView.class);
		viewManager.getSupportedViews().add(WorkflowView.class);
		viewManager.getSupportedViews().add(ConfigView.class);
		viewManager.getSupportedViews().add(WorkflowWizardView.class);

		frmGraspj.setTitle(Global.getStringfromBundle("MenuLabels",
				"GraspJ.title"));
		frmGraspj.setBounds(100, 100, 450, 300);
		frmGraspj.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JMenuBar menuBar = new JMenuBar();
		frmGraspj.setJMenuBar(menuBar);

		CmdMenu mnFile = new CmdMenu(this, "mnFile");
		menuBar.add(mnFile);
		mnFile.addCmdMenuItem("workflowWizard");
		mnFile.addCmdMenuItem("emptyWorkflow");
		mnFile.addSeparator();
		mnFile.addCmdMenuItem("openWorkflow");
		mnFile.addCmdMenuItem("openResult");
		mnFile.addSeparator();
		mnFile.addCmdMenuItem("saveAs");
		mnFile.addSeparator();
		mnFile.addCmdMenuItem("settings");
		mnFile.addCmdMenuItem("about");

		menuBar.add(viewManager.getViewsMenu());
	}

	@Override
	public void runCommand(String cmd) {
		switch (cmd) {
		case "workflowWizard":
			WorkflowWizardView workflowWizardView = new WorkflowWizardView();
			viewManager.switchView(workflowWizardView);
			break;
		case "emptyWorkflow":
			WorkflowView workflowView = new WorkflowView(new WorkflowEditor());
			viewManager.switchView(workflowView);
			break;
		case "openWorkflow":
			try {
				viewManager.openDialog(new WorkflowView());
			}
			catch (ClassNotFoundException | ClassCastException | IOException e) {
				logger.error(e.getMessage(), e);
				Utils.showError(e);
			}
			catch (OperationCanceledException e) {
				// do nothing
			}
			break;
		case "openResult":
			try {
				viewManager.openDialog(new AnalysisItemView());
			}
			catch (ClassNotFoundException | ClassCastException | IOException e) {
				logger.error(e.getMessage(), e);
				IJ.error(e.getMessage(), e.toString());
			}
			catch (OperationCanceledException e) {
				// do nothing
			}
			break;
		case "saveAs":
			try {
				viewManager.saveDialog();
			}
			catch (IOException e) {
				logger.error(e.getMessage(), e);
				Utils.showError(e);
			}
			catch (OperationCanceledException e) {
				// do nothing
			}
			break;
		case "settings":
			viewManager.switchView(new ConfigView(User.INSTANCE.getConfig()));
			break;
		case "about":
			final Map<String, String> replacements = new HashMap<>();
			replacements.put("version", Global.GraspJ_VERSION.toString());
			Global.INSTANCE.getExecutor().submit(new Runnable() {
				
				@Override
				public void run() {
					Utils.showMessage("GraspJ.about", replacements);
					
				}
			});
			
			break;
		}
	}

}
