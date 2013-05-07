package eu.brede.common.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;


public class BinaryLoader<T extends Serializable> extends Loader<T> {

	@SuppressWarnings("unchecked")
	@Override
	public T loadFrom(InputStream stream) throws FileNotFoundException,
			ClassNotFoundException, IOException {
		T object=null;
		try (ObjectInputStream inputStream = new ObjectInputStream(stream);) {			
			object = (T) inputStream.readObject();
			inputStream.close();
			return object;
		}
	}

}
