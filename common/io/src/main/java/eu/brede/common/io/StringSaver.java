package eu.brede.common.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public abstract class StringSaver extends Saver<String> {

	private String encoding = "UTF-8";

	@Override
	public void saveTo(OutputStream stream) throws FileNotFoundException,
			IOException {
		try (Writer out = new OutputStreamWriter(stream, encoding);) {
	      out.write(getObject());
	    }
	}
}

