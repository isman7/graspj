package eu.brede.common.util;

public class AssertionTools {
	public static <T> T defaultIfNull(T value, T defaultValue) {
		if(value==null) {
			return defaultValue;
		}
		else {
			return value;
		}
	}
}
