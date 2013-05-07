package eu.brede.graspj.gui.configurator;

/**
 * Inspired by ConsoleConfigPanel from andrebossard.com
 */

import java.util.Map.Entry;

import org.ciscavate.cjwizard.CustomWizardComponent;

import eu.brede.common.config.Configurable;
import eu.brede.common.config.EnhancedConfig;


/**
 * Stub for displaying Configuration item.
 *
 * @author andre
 *
 */
public class EmbeddedConfigForm extends ItemContainer implements Configurable,
	CustomWizardComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private EnhancedConfig config;
	
	public EmbeddedConfigForm() {
	}
	
	public EmbeddedConfigForm(EnhancedConfig config) {
		setConfig(config);
	}
	
	public EmbeddedConfigForm(Configurable configurable) {
		this(configurable.getConfig());
	}
	
	@Override
	public EnhancedConfig getValue() {
		return getConfig();
	}

	@Override
	public EnhancedConfig getConfig() {
		updateConfig();
		return config;
	}

	@Override
	public void setConfig(EnhancedConfig newConfig) {
		clear();
		
		config = newConfig;
		String prefix = config.getPrefix();
		
		for(Entry<String,Object> entry: config.entrySet()) {
			put(prefix + entry.getKey(),entry.getValue());
		}
		
	}
	
	private void updateConfig() {

		for (Entry<String,ConfigFormItem<?>> entry : components.entrySet()) {
			String plainKey = entry.getKey().replaceFirst(
					config.getPrefix(), ""); 
			
			config.setProperty(plainKey, entry.getValue().getValue());
		}
	}

	@Override
	public void setValue(Object value) {
		if(value instanceof EnhancedConfig) {			
			if(value!=config) {
				setConfig((EnhancedConfig)value);
			}
			else {
				refresh();
			}
		}
	}

	@Override
	public void refresh() {
		for (Entry<String,ConfigFormItem<?>> entry : components.entrySet()) {
			// TODO plainKey should be equal to key anyways.
			String plainKey = entry.getKey().replaceFirst(
					config.getPrefix(), ""); 
			
			if(config.containsKey(plainKey)) {
				entry.getValue().setValue(config.get(plainKey));
			}
			else {
				components.remove(entry.getKey());
			}
		}
		String prefix = config.getPrefix();
		for(Entry<String,Object> entry : config.entrySet()) {
			if(!components.containsKey(entry.getKey())) {
				this.put(entry.getKey(), entry.getValue());
			}
		}
		super.refresh();
	}

}