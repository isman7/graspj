package eu.brede.graspj.gui.views;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import eu.brede.common.io.CSVSaver;
import eu.brede.graspj.datatypes.spot.Spot;
import eu.brede.graspj.datatypes.spotcollection.SpotCollection;
import org.jdesktop.swingx.VerticalLayout;

import eu.brede.common.config.EnhancedConfig;
import eu.brede.common.io.BinaryLoader;
import eu.brede.common.io.BinarySaver;
import eu.brede.common.io.GZip;
import eu.brede.common.io.easy.EasyFileFilter;
import eu.brede.common.io.easy.EasyLoadable;
import eu.brede.common.io.easy.EasyLoader;
import eu.brede.common.io.easy.EasySavable;
import eu.brede.common.io.easy.EasySaver;
import eu.brede.common.pipeline.producers.Producer;
import eu.brede.common.util.GUITools;
import eu.brede.graspj.configs.Global;
import eu.brede.graspj.configs.User;
import eu.brede.graspj.datatypes.AnalysisItem;
import eu.brede.graspj.datatypes.Workflow;
import eu.brede.graspj.gui.RenderingCanvas;
import eu.brede.graspj.gui.WorkflowEditor;
import eu.brede.graspj.gui.configurator.ConfigForm;
import eu.brede.graspj.pipeline.producers.CanUseExistent;
import eu.brede.graspj.utils.GroupManager;
import eu.brede.graspj.utils.GroupManager.Group;

public class AnalysisItemView extends View implements EasySavable,
		EasyLoadable<AnalysisItem> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// private final JTextPane textPane = new JTextPane();
	// private final ConfigForm resultForm = new ConfigForm();
	// private final ConfigForm acqForm = new ConfigForm();
	// private final ConfigForm notesForm = new ConfigForm();
	private final ConfigForm configForm = new ConfigForm(new EnhancedConfig());

	private final EasySaver easySaver = new EasySaver();
	private final EasyLoader<AnalysisItem> easyLoader;// = new EasyLoader<>();

	private AnalysisItem analysisItem;

	public AnalysisItemView(AnalysisItem item) {
		this();
		setAnalysisItem(item);		
	}

	public AnalysisItemView() {
//		this(null);
		initPanel();
//		initViewMenu();
		easyLoader = initEasyLoader();
		initSaveOptions();
		initLoadOptions();
	}

	private EasyLoader<AnalysisItem> initEasyLoader() {
		return new EasyLoader<AnalysisItem>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void easyLoad(AnalysisItem item) {
				setAnalysisItem(item);
			}
		};
	}

	public void refresh() {
		EnhancedConfig config = null;
		if (analysisItem != null) {
			config = configForm.getConfig();
			config.setPrefix(this.getClass().getSimpleName() + ".config.");
			config.put("metaData", analysisItem.getConfig()
					.getMetaData());
			config.put("resultSummary", analysisItem.getResultSummary());
			config.put("acquisitionConfig",
					analysisItem.getAcquisitionConfig());
			EnhancedConfig notes = new EnhancedConfig();
			notes.putAll(analysisItem.getNotes());
			notes.setPrefix(analysisItem.getClass().getSimpleName() + "."
					+ analysisItem.getNotes().getClass().getSimpleName() + ".");
			config.put("notes", notes);
		} else {
			config = new EnhancedConfig();
		}
		configForm.setConfig(config);
		initViewMenu();
	}

	public void setAnalysisItem(AnalysisItem analysisItem) {
		if(analysisItem==this.analysisItem) {
			return;
		}
		clearPreviewGroup();
		this.analysisItem = analysisItem;
		refresh();
	}

	public AnalysisItem getAnalysisItem() {
		return analysisItem;
	}

	private class WorkflowsMenu extends JMenu {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public WorkflowsMenu() {
			super();
		}

		public WorkflowsMenu(HashMap<String, Workflow> workflowMap) {
			this();
			for (Entry<String, Workflow> entry : workflowMap.entrySet()) {
				add(new WorkflowMenuItem(entry.getKey(), entry.getValue()));
			}
		}

		private class WorkflowMenuItem extends JMenuItem {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			private Workflow workflow;

			public WorkflowMenuItem(String name, Workflow workflow) {
				super();
				this.workflow = workflow;
				setText(name);
				init();
			}

			private void init() {
				addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						for (Producer<AnalysisItem> producer : workflow
								.getProducers()) {
							if (producer instanceof CanUseExistent) {
								((CanUseExistent) producer)
										.setAnalysisItem(getAnalysisItem());
							}
						}
						getManager().switchView(
								new WorkflowView(new WorkflowEditor(workflow)));
					}
				});
			}
		}

	}

	private void initPanel() {
		this.removeAll();
		GUITools.makeParentPacking(this);
		GUITools.makeParentPacking(configForm);
		setLayout(new VerticalLayout());
		this.add(configForm);
		configForm.setLabelText(Global.getStringfromBundle("ConfigLabels",
				getClass().getSimpleName() + ".config"));
		// configForm.setLabelText("Result");
		// this.add(acqForm.getGUI());
		// this.add(notesForm.getGUI());
	}

	private void initViewMenu() {
		getViewMenu().removeAll();

		WorkflowsMenu wfBuiltinMenu = new WorkflowsMenu(
				Global.getBuiltinWorkflows(getAnalysisItem()));
		wfBuiltinMenu.setText("builtinWorkflows");
		getViewMenu().addSubmenu(wfBuiltinMenu);
		
		WorkflowsMenu wfFavsMenu = new WorkflowsMenu(User.INSTANCE.getConfig()
				.<HashMap<String, Workflow>> gett("userWorkflowsOnExistent"));
		wfFavsMenu.setText("favoriteWorkflows");

		if (analysisItem != null) {
			WorkflowsMenu wfAttachedMenu = new WorkflowsMenu(analysisItem
					.getNotes().getAttachedWorkflows());
			wfAttachedMenu.setText("attachedWorkflows");

			getViewMenu().addSubmenu(wfAttachedMenu);
		}
		// System.out.println(ResourceBundle.getBundle("Menus").getString("favoriteWorkflows"));

		getViewMenu().addSubmenu(wfFavsMenu);

		// getViewMenu().setText("Result");

		// JMenuItem mntmLiveRenderer = new JMenuItem("Live Renderer");
		// mntmLiveRenderer.addActionListener(new ActionListener() {
		// public void actionPerformed(ActionEvent e) {
		// ProductionPipeline<AnalysisItem,AnalysisItem> pipeline =
		// new ProductionPipeline<>(analysisItem);
		// pipeline.getProducers().add(new UseExistingProducer(analysisItem));
		// pipeline.getConsumers().add(new LiveRenderer());
		// getManager().switchView(new RunningAnalysisView(pipeline).start(),
		// ViewManager.SwitchType.DISPOSE);
		// }
		// });
		// getViewMenu().add(mntmLiveRenderer);
	}

	private void initSaveOptions() {
		easySaver.put(new EasyFileFilter("gjr", "GraspJ Result"),
				new BinarySaver() {

					@Override
					public Serializable getObject() {
						configForm.updateConfig();
						return analysisItem;
					}

				});
		easySaver.put(new EasyFileFilter("gjrz", "GraspJ Result (zipped)"),
				GZip.wrapSaver(new BinarySaver() {

					@Override
					public Serializable getObject() {
						configForm.updateConfig();
						return analysisItem;
					}

				}));
        easySaver.put(new EasyFileFilter("csv", "Localizations"),
                new CSVSaver<Spot, SpotCollection>() {

                    @Override
                    public SpotCollection getObject() {
                        configForm.updateConfig();
                        return analysisItem.getSpots();
                    }

                });
	}

	private void initLoadOptions() {
		easyLoader.put(new EasyFileFilter("gjr", "GraspJ Result"),
				new BinaryLoader<AnalysisItem>());
		easyLoader.put(new EasyFileFilter("gjrz", "GraspJ Result (zipped)"),
				GZip.wrapLoader(new BinaryLoader<AnalysisItem>()));
	}

	@Override
	public EasySaver getEasySaver() {
		return easySaver;
	}

	@Override
	public String toString() {
		String thisName = Global.getStringfromBundle("MenuLabels", getClass()
				.getSimpleName());
		return thisName + " >> "
				+ analysisItem.getConfig().getMetaData().getName("unnamed");
	}

	@Override
	public EasyLoader<AnalysisItem> getEasyLoader() {
		return easyLoader;
	}

	// @Override
	// public AnalysisItem easyLoad(ExtFileChooser fileChooser) {
	// AnalysisItem item = null;
	// try {
	// item = getEasyLoader().showLoadDialog(fileChooser);
	// } catch (ClassNotFoundException | IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// easyLoad(item);
	// return item;
	// }

	@Override
	public void runCommand(String cmd) {
		// TODO Auto-generated method stub

	}
	
	private void clearPreviewGroup() {
		if(analysisItem!=null) {			
			Group group = GroupManager.getGroup(RenderingCanvas.class, "preview"
					+ analysisItem.hashCode());
			if(group!=null) {			
				synchronized (group) {
					group.clear();
				}
			}
		}
	}

	@Override
	public void dispose() {
		clearPreviewGroup();
		super.dispose();
	}

	// @Override
	// public void easyLoad(AnalysisItem item) {
	// if(item!=null) {
	// setAnalysisItem(item);
	// }
	// }

}
