package eu.brede.graspj.pipeline.processors.renderer;

import eu.brede.common.config.Configurable;
import eu.brede.common.config.EnhancedConfig;
import eu.brede.common.pipeline.Processor;
import eu.brede.graspj.configs.rendering.RenderConfig;
import eu.brede.graspj.datatypes.AnalysisItem;
import eu.brede.graspj.datatypes.Option;
import eu.brede.graspj.datatypes.Rendering;

public abstract class SpotRenderer implements Processor<AnalysisItem>, Configurable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//	private RenderingDeprecated rendering;
//	private Fit fit;
//	private Filtering filtering;
//	private GJProject project;
	protected transient Rendering rendering;
	protected int fitDimension = 5;
	private RenderConfig config;
	
	public SpotRenderer() {
		config = new RenderConfig();
	}
	
	public SpotRenderer(int channel) {
		this();
		setChannel(channel);
	}
	
	public void setChannel(int channel) {
		Option applyMask = getConfig().gett("applyMask");
		applyMask.setSelected(true);
		Option specificChannel = applyMask.gett("specificChannel");
		specificChannel.setSelected(true);
		specificChannel.put("channelNr", channel);
		getConfig().getMetaData().setName("Channel " + channel);
	}

	@Override
	public RenderConfig getConfig() {
		return config;
	}

	@Override
	public void setConfig(EnhancedConfig config) {
		this.config = (RenderConfig) config; // TODO wrap, not cast
	}

	public Rendering getRendering() {
		return rendering;
	}
	
	
	
}
