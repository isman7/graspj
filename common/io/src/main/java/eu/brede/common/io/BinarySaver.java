package eu.brede.common.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;


public abstract class BinarySaver extends Saver<Serializable> {

	@Override
	public void saveTo(OutputStream stream) throws FileNotFoundException,
			IOException {
		try (ObjectOutputStream outputStream = new ObjectOutputStream(stream);) {
			outputStream.writeObject(getObject());
			outputStream.flush();
			outputStream.close();
		}
	}

}
