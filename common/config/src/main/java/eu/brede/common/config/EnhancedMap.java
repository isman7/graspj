package eu.brede.common.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.beanutils.converters.BooleanConverter;
import org.apache.commons.beanutils.converters.ByteConverter;
import org.apache.commons.beanutils.converters.DoubleConverter;
import org.apache.commons.beanutils.converters.FloatConverter;
import org.apache.commons.beanutils.converters.IntegerConverter;
import org.apache.commons.beanutils.converters.ShortConverter;

public class EnhancedMap<K,V> extends LinkedHashMap<K,V> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public EnhancedMap() {
		super();
	}
	
	public EnhancedMap(Map<? extends K, ? extends V> m) {
        super(m);
    }
	
	// a convenient generic get method
	@SuppressWarnings("unchecked")
	public <T> T gett(Object key) {
		return (T) this.get(key);
	}
	
	// convenient generic get method with default value support
	@SuppressWarnings("unchecked")
	public <T> T get(Object key, T defaultValue) {
		if(this.containsKey(key)) {
			return (T) this.get(key);
		}
		else {
			return defaultValue;
		}
	}
	
	public Integer getInt(Object key) {
		Object value = get(key);
		IntegerConverter converter = new IntegerConverter(value);
		return (Integer) converter.convert(Integer.class, value);
	}
	
	public Float getFloat(Object key) {
		Object value = get(key);
		FloatConverter converter = new FloatConverter(value);
		return (Float) converter.convert(Float.class, value);
	}
	
	public Double getDouble(Object key) {
		Object value = get(key);
		DoubleConverter converter = new DoubleConverter(value);
		return (Double) converter.convert(Double.class, value);
	}
	
	public Boolean getBoolean(Object key) {
		Object value = get(key);
		BooleanConverter converter = new BooleanConverter(value);
		return (Boolean) converter.convert(Boolean.class, value);
	}
	
	public Short getShort(Object key) {
		Object value = get(key);
		ShortConverter converter = new ShortConverter(value);
		return (Short) converter.convert(Short.class, value);
	}
	
	public Byte getByte(Object key) {
		Object value = get(key);
		ByteConverter converter = new ByteConverter(value);
		return (Byte) converter.convert(Byte.class, value);
	}
	
	public String getString(Object key) {
		return gett(key).toString();
	}
}
