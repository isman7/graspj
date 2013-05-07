package eu.brede.common.config;




public class PlainConfigurable implements Configurable {

	protected EnhancedConfig config;
	
	public PlainConfigurable() {
		this(new EnhancedConfig());
	}
	
	public PlainConfigurable(EnhancedConfig config) {
		super();
		this.config = config;
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
