package eu.brede.common.opencl.programs;

import java.io.InputStream;
import java.util.Map;

import eu.brede.common.util.ResourceTools;
import eu.brede.common.util.StringTools;

public class CLCode {
	private StringBuilder builder = new StringBuilder();

	
	public CLCode append(InputStream is) {
		builder.append(ResourceTools.convertStreamToString(is));
		return this;
	}
	
	public CLCode append(CLCode code) {
		builder.append(code.toString());
		return this;
	}
	
	public CLCode appendResource(String resourceURI) {
		builder.append(ResourceTools.getResourceAsString(resourceURI));
		return this;
	}
	
	public CLCode appendln(String str) {
		builder.append(str).append("\n");
		return this;
	}
	
	public CLCode appendln(String str, Map<String,String> replacements) {
		builder.append(StringTools.replaceTokens(str, replacements)).append("\n");
		return this;
	}
	
	// delegate some StringBuilder methods to builder
	public int length() {
		return builder.length();
	}

	public int capacity() {
		return builder.capacity();
	}

	public int hashCode() {
		return builder.hashCode();
	}

	public void ensureCapacity(int minimumCapacity) {
		builder.ensureCapacity(minimumCapacity);
	}

	public void trimToSize() {
		builder.trimToSize();
	}

	public boolean equals(Object obj) {
		return builder.equals(obj);
	}

	public CLCode append(String str) {
		builder.append(str);
		return this;
	}

	public String toString() {
		return builder.toString();
	}

}
