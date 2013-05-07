package eu.brede.graspj.configs;

import java.io.File;

import eu.brede.common.config.Configurable;
import eu.brede.common.config.EnhancedConfig;
import eu.brede.common.io.XMLLoader;



public enum User implements Configurable {
	INSTANCE;
	
	private UserConfig config;
	
	private User() {
		if (config == null) {
			// perhaps read from Properties file
			File userConfigFile = new File("graspj.config");
			try {
				XMLLoader<UserConfig> loader = new XMLLoader<>();
				config = loader.loadFrom(userConfigFile);
			} catch (Exception e) {
				e.printStackTrace();
				config = new UserConfig();
			}
		}
	}
	
	@Override
	public UserConfig getConfig() {
		return config;
	}

	@Override
	public void setConfig(EnhancedConfig config) {
		if(config instanceof UserConfig) {
			this.config = (UserConfig) config;
		}
		else {
			if(this.config==null) {
				this.config = new UserConfig();
			}
			this.config.putAll(config);
		}
	}

}