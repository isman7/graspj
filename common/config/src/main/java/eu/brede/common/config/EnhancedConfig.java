package eu.brede.common.config;

import java.io.ObjectStreamException;
import java.util.Map.Entry;


public class EnhancedConfig extends ESOMap {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	String prefix = "";
	
	protected Object readResolve() throws ObjectStreamException {
		if(prefix==null) {
			prefix = "";
		}
		ensureCompliance();
		return this;
	}
	
	public EnhancedConfig getDefault() {
		return getRequiredDefault();
	}
	
	public EnhancedConfig getRequiredDefault() {
		return new EnhancedConfig(false);
	}
	
	public void ensureCompliance() {
		// introduce strict compliance with type check?
		EnhancedConfig requiredConfig = getRequiredDefault();
		
		for(Entry<String,Object> entry: requiredConfig.entrySet()) {
			String requiredKey = entry.getKey();
			if(!containsKey(requiredKey)) {
				put(requiredKey, entry.getValue());
			}
		}
		// drop this?
		prefix = requiredConfig.getPrefix();
	}

	@Deprecated
	public void setProperty(String requiredKey, Object value) {
		put(requiredKey, value);
	}
	
	@Deprecated
	public Object getProperty(String key) {
		return get(key);
	}
	
//	public EnhancedConfig(Map<? extends String, ? extends Object> m) {
//		super(m);
//		ensureCompliance();
//	}
//
//	public EnhancedConfig(EnhancedConfig config) {
//		this((Map<String,? extends Object>)config);
//		this.prefix = config.getPrefix();
//	}
	
	public EnhancedConfig() {
		this(true);
	}
	
	public EnhancedConfig(boolean ensureCompliance) {
		super();
		if(ensureCompliance) {			
			ensureCompliance();
		}
	}
	
	public EnhancedConfig(String prefix) {
		this();
		setPrefix(prefix);
	}
	
	@SafeVarargs
	public <O extends Object> EnhancedConfig(Entry<String,O>... entries) {
		this();
		for(Entry<String,O> entry : entries) {
			put(entry.getKey(), entry.getValue());
		}
	}
	
	@SafeVarargs
	public <O extends Object> EnhancedConfig(String prefix, Entry<String,O>... entries) {
		this(entries);
		setPrefix(prefix);
	}
	
//	public static String buildSimpleName(Configurable configurable) {
//		return configurable.getConfig().getMetaData().getName("unnamed")
//				+ " - " + Global.getStringfromBundle("MenuLabels", 
//						configurable.getClass().getSimpleName());
//	}
	
	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public Double getProduct(String... keys) {
		Double product = new Double(1);
		for (String key : keys) {
			product = product * this.<Number>gett(key).doubleValue();
		}
		return product;
	}
	
	public MetaData getMetaData() {
		return gett("metaData");
	}
	
	public void initMetaData() {
		put("metaData", new MetaData());
	}
}
