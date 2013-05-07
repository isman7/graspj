package eu.brede.graspj.pipeline.consumers;

import ij.gui.ImageWindow;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import eu.brede.common.config.Configurable;
import eu.brede.common.config.EnhancedConfig;
import eu.brede.common.config.MetaData;
import eu.brede.common.pipeline.NeedsProduct;
import eu.brede.common.pipeline.consumers.AbstractConsumer;
import eu.brede.graspj.datatypes.AnalysisItem;
import eu.brede.graspj.datatypes.ObjectChoice;
import eu.brede.graspj.datatypes.Option;
import eu.brede.graspj.gui.RenderingCanvas;
import eu.brede.graspj.pipeline.processors.renderer.Gaussian2D;
import eu.brede.graspj.pipeline.processors.renderer.GaussianColorCoded3D;
import eu.brede.graspj.pipeline.processors.renderer.SpotRenderer;
import eu.brede.graspj.utils.GroupManager;
import eu.brede.graspj.utils.GroupManager.GroupNames;

public class LiveRenderer extends AbstractConsumer<AnalysisItem>
		implements NeedsProduct<AnalysisItem>, Configurable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private transient RenderingCanvas canvas;
	private transient AnalysisItem product;
	private EnhancedConfig config;
	
	public LiveRenderer() {
		config = new EnhancedConfig() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public EnhancedConfig getRequiredDefault() {
				EnhancedConfig requiredConfig = super.getRequiredDefault();
				requiredConfig.setPrefix("LiveRendererConfig.");
				
				requiredConfig.put("metaData", new MetaData());
				ObjectChoice<SpotRenderer> choice = new ObjectChoice<>();
				
				
				SpotRenderer stdRenderer = new Gaussian2D();
				Set<SpotRenderer> renderers = new HashSet<>();
				renderers.add(stdRenderer);
				renderers.add(new GaussianColorCoded3D());
				for(SpotRenderer renderer : renderers) {
					renderer.getConfig().put("renderWidth", 512);
					renderer.getConfig().put("renderHeight", 512);
					choice.getChoices().add(renderer);
				}
				choice.setChosen(stdRenderer);
				requiredConfig.put("renderer", choice);
				
				Option pushActions = new Option();
				pushActions.put("groupNames", new GroupNames("live"));
				pushActions.setSelected(true);
		
				Option receiveActions = new Option();
				receiveActions.put("groupNames", new GroupNames("live"));
				receiveActions.setSelected(true);
				
				requiredConfig.put("pushActions", pushActions);
				requiredConfig.put("receiveActions", receiveActions);
				requiredConfig.put("autoContrast", true);
				return requiredConfig;
			}
			
		};
		
		config.ensureCompliance();
	}
	
	public LiveRenderer(int channel) {
		this();
		ObjectChoice<SpotRenderer> choice = getConfig().gett("renderer");
		for(SpotRenderer renderer : choice.getChoices()) {
			renderer.setChannel(channel);
			getConfig().getMetaData().setName("Channel " + channel);
		}
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
//		LiveRenderer test = new LiveRenderer();
//		test.getConfig().copy(getConfig());
//		setConfig(test.getConfig());
	}
	
	@Override
	public void setProduct(AnalysisItem product) {
		this.product = product;
		// TODO why was line below necessray?
//		this.canvas = null;
	}

	@Override
	protected void use(AnalysisItem item) throws InterruptedException {
		if(canvas == null) {
			initCanvas();
		}
		canvas.requestRefresh();
		canvas.fitToWindow();
	}
	
	private void initCanvas() {
//		canvas = new RenderingCanvas(product, new Gaussian2D());
//		canvas = new RenderingCanvas(product, new GaussianColorCoded3D());
		SpotRenderer renderer = config.<ObjectChoice<SpotRenderer>>gett("renderer").getChosen();
		renderer.getConfig().getMetaData().setName(getConfig().getMetaData().getName());
//				renderer.getConfig().getMetaData().getName() + getConfig().getMetaData().getName());
		
		Option pushActions = getConfig().gett("pushActions");
		Option receiveActions = getConfig().gett("receiveActions");
		
		boolean autoContrast = getConfig().get("autoContrast", false);
		
		canvas = new RenderingCanvas(product, renderer, pushActions, receiveActions, autoContrast);
		
		ImageWindow window = createRenderingWindow(canvas);
		window.addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosing(WindowEvent e) {
				GroupManager.unRegisterReceiver(canvas);
			}
		});
		window.setVisible(true);
	}
	
	public static ImageWindow createRenderingWindow(RenderingCanvas canvas) {
		
		ImageWindow window = new ImageWindow(canvas.getImage(), canvas);
		canvas.getImage().setWindow(window);
		canvas.getImage().draw();
//		window.setVisible(true);
		window.pack();
		canvas.alwaysFitCanvasToWindow();
		
		return window;
	}

	@Override
	public EnhancedConfig getConfig() {
		return config;
	}

	@Override
	public void setConfig(EnhancedConfig config) {
		this.config = config;
	}

	
}
