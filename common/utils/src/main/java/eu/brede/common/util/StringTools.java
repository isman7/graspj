package eu.brede.common.util;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringTools {
	// Token format: /*${name}*/
	
	public static HashMap<Integer,LinkedHashSet<String>> missingKeys = new HashMap<>();
	
	public static String replaceTokens(String text,
			Map<String, String> replacements) {
		if((replacements==null) || (text == null)) {
			return text;
		}
		Pattern pattern = Pattern.compile("\\/\\*\\$\\{(.+?)\\}\\*\\/");
		Matcher matcher = pattern.matcher(text);
		StringBuffer buffer = new StringBuffer();
		while (matcher.find()) {
			String replacement = replacements.get(matcher.group(1));
			if (replacement != null) {
				matcher.appendReplacement(buffer, "");
				buffer.append(replacement);
			}
		}
		matcher.appendTail(buffer);
		return buffer.toString();
	}
	
	public static String replaceToken(String text, String token, String replacement) {
		HashMap<String,String> map = new HashMap<>();
		map.put(token, replacement);
		return replaceTokens(text, map);
	}
	
	public static String getStringFromBundle(ResourceBundle bundle, String key, String defaultString) {
		if(bundle==null) {
			return defaultString;
		}
		if(bundle.containsKey(key)) {
			return bundle.getString(key);
		}
		else {
			Integer id = bundle.hashCode();
			if(!missingKeys.containsKey(id)) {
				missingKeys.put(id, new LinkedHashSet<String>());
			}
			LinkedHashSet<String> keySet = missingKeys.get(id);
			keySet.add(key);
			return defaultString;
		}
	}
	
	public static String getStringFromBundle(ResourceBundle bundle, String key) {
		return getStringFromBundle(bundle, key, key);
	}
	
	public static String getStringFromBundle(ResourceBundle bundle, Object object) {
		String key = object.getClass().getSimpleName();
		return getStringFromBundle(bundle, key, key);
	}
	
	public static String splitCamelCase(String s) {
		return s.replaceAll(
				String.format("%s|%s|%s",
						"(?<=[A-Z])(?=[A-Z][a-z])",
						"(?<=[^A-Z])(?=[A-Z])",
						"(?<=[A-Za-z])(?=[^A-Za-z])"
						),
						" "
				);
	}

}
