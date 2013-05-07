package eu.brede.graspj.gui.views;

import ij.IJ;

import java.io.Serializable;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.jdesktop.swingx.VerticalLayout;

import eu.brede.common.io.BinaryLoader;
import eu.brede.common.io.BinarySaver;
import eu.brede.common.io.GZip;
import eu.brede.common.io.Saver;
import eu.brede.common.io.StringLoader;
import eu.brede.common.io.XMLLoader;
import eu.brede.common.io.XMLSaver;
import eu.brede.common.io.easy.EasyFileFilter;
import eu.brede.common.io.easy.EasyLoadable;
import eu.brede.common.io.easy.EasyLoader;
import eu.brede.common.io.easy.EasySavable;
import eu.brede.common.io.easy.EasySaver;
import eu.brede.common.opencl.utils.CLSystem;
import eu.brede.common.util.DeepCopy;
import eu.brede.graspj.configs.Global;
import eu.brede.graspj.configs.User;
import eu.brede.graspj.datatypes.AnalysisItem;
import eu.brede.graspj.datatypes.CommandRunner;
import eu.brede.graspj.datatypes.ProductionWorkflow;
import eu.brede.graspj.datatypes.Selection;
import eu.brede.graspj.datatypes.Workflow;
import eu.brede.graspj.datatypes.WorkflowMap;
import eu.brede.graspj.gui.WorkflowEditor;
import eu.brede.graspj.gui.configurator.SelectListBox;
import eu.brede.graspj.gui.configurator.TextFieldItem;
import eu.brede.graspj.opencl.utils.CLSystemGJ;
import eu.brede.graspj.pipeline.consumers.Housekeeper;
import eu.brede.graspj.pipeline.consumers.ViewRefresher;
import eu.brede.graspj.utils.Utils;

public class WorkflowView extends View 
	implements EasySavable, EasyLoadable<Workflow>, CommandRunner {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private WorkflowEditor workflowEditor;
	/**
	 * @wbp.nonvisual location=70,89
	 */
//	private final ExecutorService executor = new ReportingExecutorService();
	private final EasySaver easySaver = new EasySaver();
	private final EasyLoader<Workflow> easyLoader;// = new EasyLoader<>();
	
	public WorkflowView(WorkflowEditor workflowEditor) {
		this.workflowEditor = workflowEditor;
		easyLoader = initEasyLoader();
		initViewMenu();
		initPanel();
		initSaveOptions();
		initLoadOptions();
	}
	
	public WorkflowView() {
		this(new WorkflowEditor());
	}
	
	public WorkflowEditor getWorkflowEditor() {
		return workflowEditor;
	}
	
	private EasyLoader<Workflow> initEasyLoader() {
		return new EasyLoader<Workflow>() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void easyLoad(Workflow item) {
				getWorkflowEditor().setPipeline(item);
			}
		};
	}
	
	private void initViewMenu() {
		getViewMenu().addCmdMenuItem("runWorkflow");
		getViewMenu().addCmdMenuItem("addToUserWorkflowsOnExistent");
		getViewMenu().addCmdMenuItem("addToResultWorkflows");
	}
	
	
	@Override
	public void runCommand(String cmd) {
		String bundleKeyBase = "WorkflowView.CmdMenu.";
		switch(cmd) {
			case "runWorkflow":
				runWorkflow();
				break;
			case "addToUserWorkflowsOnExistent":
				String questionBundleKey = 
					"WorkflowView.CmdMenu.addToUserWorkflowsOnExistent.enterName";
				String name = IJ.getString(
						Global.getStringfromBundle("MenuLabels", questionBundleKey),
						"unnamed");
				if(name!="") {					
					User.INSTANCE.getConfig().<WorkflowMap>gett("userWorkflowsOnExistent")
					.put(name, workflowEditor.getPipeline());
				}
				break;
			case "addToResultWorkflows":
				bundleKeyBase += "addToResultWorkflows.";
				Collection<AnalysisItem> items = Utils.getOpenAnalysisItems();
				if(items.size()<=0) {
					String chooseErrorTitle = 
							bundleKeyBase + "chooseItems.error.title";
					String chooseErrorMessage = 
							bundleKeyBase + "chooseItems.error.message";
					JOptionPane.showMessageDialog(null,
							Global.getStringfromBundle("MenuLabels", chooseErrorMessage),
							Global.getStringfromBundle("MenuLabels", chooseErrorTitle),
						    JOptionPane.ERROR_MESSAGE);
					break;
				}
				SelectListBox<AnalysisItem> itemSelector = 
					new SelectListBox<>(new Selection<>(items));
				
				TextFieldItem nameField = new TextFieldItem("unnamed");
				 
				String chooseEnterNameBundleKey = 
						bundleKeyBase + "chooseItems.enterName";
				
				String chooseSelectResultBundleKey = 
						bundleKeyBase + "chooseItems.selectResult";
				
				String chooseItemsTitleBundleKey = 
						bundleKeyBase + "chooseItems.title";
					
				final JComponent[] inputs = new JComponent[] {
					new JLabel(Global.getStringfromBundle("MenuLabels", chooseEnterNameBundleKey)),
					nameField.getGUI(),
					new JLabel(Global.getStringfromBundle("MenuLabels", chooseSelectResultBundleKey)),
					itemSelector.getGUI()
				};
				int answer = JOptionPane.showConfirmDialog(null, inputs, 
						Global.getStringfromBundle("MenuLabels", chooseItemsTitleBundleKey),
						JOptionPane.OK_CANCEL_OPTION);
				if(answer==JOptionPane.OK_OPTION) {
					Workflow pipe = workflowEditor.getPipeline();
					for(AnalysisItem item : itemSelector.getValue().getSelected()) {
						item.getNotes().getAttachedWorkflows().put(
								nameField.getValue(), pipe);
					}
				}
				break;
		}
	}

	private void runWorkflow() {
		// execute in different thread and observe execution!
		
		// try to clean clSystem
		CLSystem cl = CLSystemGJ.getDefault();
		cl.refresh();
		System.gc();

		// production pipeline needed.... fix elsewhere?
		ProductionWorkflow workflow = DeepCopy.copy((ProductionWorkflow)
				WorkflowView.this.getWorkflowEditor().getPipeline());
		
		AnalysisItem product = workflow.getProduct();
		if(product != null) {
			product.getNotes().put("workflow", DeepCopy.copy(workflow));
		}
		RunningAnalysisView runningAnalysisView = new RunningAnalysisView(workflow);
		workflow.getConsumers().add(new ViewRefresher(runningAnalysisView));
		workflow.getConsumers().add(new Housekeeper());
//		@SuppressWarnings("unchecked")
//		final ProductionWorkflow pipeline = 
//				new ProductionWorkflow(
//						(ProductionWorkflow)
//						WorkflowView.this.getWorkflowEditor().getPipeline());
		
//		AnalysisItem finalItem = new AnalysisItem();
//		// TODO configure finalItem
//		finalItem.setAcquisition(new AcquisitionData(new AcquisitionConfig()));
//		finalItem.setSpots(new BufferSpotCollection((int)5000,(int)10));
		
//		pipeline.setProduct(finalItem);
		getManager().switchView(runningAnalysisView.start());
	}
	
	
	
	private void initPanel() {
		this.removeAll();
		setLayout(new VerticalLayout());
		this.add(workflowEditor);
//		this.add(textPane);
//		this.add(acqForm.getGUI());
//		this.add(notesForm.getGUI());
	}

	private void initSaveOptions() {
		easySaver.put(new EasyFileFilter("gjw","GraspJ Workflow"),
				new BinarySaver() {
					@Override
					public Serializable getObject() {
						return getWorkflowEditor().getPipeline();
					}
			
		});
		easySaver.put(new EasyFileFilter("gjwx","GraspJ Workflow XML"),
				new XMLSaver<Workflow>() {
					@Override
					public Workflow getObject() {
						return getWorkflowEditor().getPipeline();
					}
		});
		Saver<Workflow> saver = new XMLSaver<Workflow>() {
			@Override
			public Workflow getObject() {
				return getWorkflowEditor().getPipeline();
			}
		};
		easySaver.put(new EasyFileFilter("gjwxz","GraspJ Workflow XML (zipped)"),
				GZip.wrapSaver(saver));
	}
	
	private void initLoadOptions() {
		easyLoader.put(new EasyFileFilter("gjw","GraspJ Workflow"),
				new BinaryLoader<Workflow>());
		easyLoader.put(new EasyFileFilter("gjwx","GraspJ Workflow XML"),
				new XMLLoader<Workflow>());
		easyLoader.put(new EasyFileFilter("gjwxz","GraspJ Workflow XML (zipped)"),
				new XMLLoader<Workflow>(GZip.wrapLoader(new StringLoader())));
	}
	
	@Override
	public EasySaver getEasySaver() {
		return easySaver;
	}

	@Override
	public EasyLoader<Workflow> getEasyLoader() {
		return easyLoader;
	}

//	@Override
//	public Workflow easyLoad(ExtFileChooser fileChooser) {
//		Workflow workflow = null;
//		try {
//			workflow = getEasyLoader().showLoadDialog(fileChooser);
//		} catch (ClassNotFoundException | IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		if(workflow!=null) {
//			easyLoad(workflow);
//		}
//		else {
//			System.out.println("error while reading file");
//		}
//		return workflow;
//	}

	@Override
	public void suspend() {
		getWorkflowEditor().getPipeline();
		super.suspend();
	}

	@Override
	public void resume() {
		getWorkflowEditor().getPipeline();
		super.resume();
	}

	@Override
	public void dispose() {
		getWorkflowEditor().getPipeline();
		super.dispose();
	}

//	@Override
//	public void easyLoad(Workflow item) {
//		if(item!=null) {
//			getWorkflowEditor().setPipeline(item);
//		}
//	}
	
	

}
