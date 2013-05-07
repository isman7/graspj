package eu.brede.graspj.gui.configurator;

/**
 * Inspired by ConsoleConfigPanel from andrebossard.com
 */

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map.Entry;

//import org.apache.commons.configuration.Configuration;
import org.ciscavate.cjwizard.CustomWizardComponent;

import eu.brede.common.config.Configurable;
import eu.brede.common.config.EnhancedConfig;
import eu.brede.graspj.configs.Global;


/**
 * Stub for displaying Configuration item.
 *
 * @author andre
 *
 */
public class ConfigForm extends CollapsableItemContainer implements Configurable,
	CustomWizardComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private EnhancedConfig config;
	
	public ConfigForm() {
		super();
		addUpdatingListeners();
	}
	
	public ConfigForm(boolean collapsed) {
		super(collapsed);
		addUpdatingListeners();
	}
	
	public ConfigForm(EnhancedConfig config) {
		this();
		setConfig(config);
	}
	
	public ConfigForm(Configurable configurable) {
		this(configurable.getConfig());
	}
	
//	public ConfigForm(Configuration config) {
//		this(EnhancedConfig.wrap(config));
//	}
	
	private void addUpdatingListeners() {
		this.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				super.focusLost(e);
//				System.out.println("lost focus");
				updateConfig();
			}
			
		});
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				super.mouseExited(e);
//				System.out.println("mouse exited");
				updateConfig();
			}
		});
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
		
		String name = config.getPrefix();
		if(name.endsWith(".")) {
			name = name.substring(0, name.length()-1);				
		}
		setLabelText(Global.getStringfromBundle("ConfigLabels", name));
		
	}
	
	public void updateConfig() {

		for (Entry<String,ConfigFormItem<?>> entry : components.entrySet()) {
			String plainKey = entry.getKey().replaceFirst(
					config.getPrefix(), ""); 
			
			config.setProperty(plainKey, entry.getValue().getValue());
		}
	}

	@Override
	protected void finalize() throws Throwable {
//		System.out.println("finalized");
		updateConfig();
		super.finalize();
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
		String prefix = config.getPrefix();
		
		for (Entry<String,ConfigFormItem<?>> entry : components.entrySet()) {
			String key = entry.getKey();
			String plainKey = key.replaceFirst(prefix, ""); 
			
			if(config.containsKey(plainKey)) {
				entry.getValue().setValue(config.get(plainKey));
			}
			else {
				components.remove(key);
			}
		}
		
		for(Entry<String,Object> entry : config.entrySet()) {
			String key = prefix + entry.getKey();
			if(!components.containsKey(key)) {
				this.put(key, entry.getValue());
			}
		}
		super.refresh();
	}
	
	



}