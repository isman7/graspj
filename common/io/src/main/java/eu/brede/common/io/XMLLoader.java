package eu.brede.common.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;


public class XMLLoader<T> extends Loader<T> {
	Loader<String> stringLoader;
	
	public XMLLoader() {
		this(new StringLoader());
	}

	public XMLLoader(Loader<String> stringLoader) {
		super();
		this.stringLoader = stringLoader;
	}

	// TODO throw cast error
	@SuppressWarnings("unchecked")
	@Override
	public T loadFrom(InputStream stream) throws FileNotFoundException,
			ClassNotFoundException, IOException {
		String xml = stringLoader.loadFrom(stream);
		XStream xstream = new XStream(new DomDriver());
		return (T)xstream.fromXML(xml);
	}
	
	

}
