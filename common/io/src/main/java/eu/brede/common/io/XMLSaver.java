package eu.brede.common.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;


public abstract class XMLSaver<T> extends Saver<T> {
	Saver<String> stringSaver;
	
	public XMLSaver() {
		stringSaver = new StringSaver() {
			
			@Override
			public String getObject() {
				XStream xstream = new XStream(new DomDriver());
				return xstream.toXML(XMLSaver.this.getObject());
			}
		};
	}

	public Saver<String> getStringSaver() {
		return stringSaver;
	}

	public void setStringSaver(Saver<String> stringSaver) {
		this.stringSaver = stringSaver;
	}

	@Override
	public void saveTo(OutputStream stream) throws FileNotFoundException,
			IOException {
		stringSaver.saveTo(stream);
	}

}
