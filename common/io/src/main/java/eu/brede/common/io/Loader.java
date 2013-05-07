package eu.brede.common.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public abstract class Loader<T> {
	public T loadFrom(String fileName) 
			throws FileNotFoundException, IOException, ClassNotFoundException {
		return loadFrom(new BufferedInputStream(new FileInputStream(fileName)));
	}
	public T loadFrom(File file)
			throws FileNotFoundException, ClassNotFoundException, IOException {
		return loadFrom(file.getAbsolutePath());
	}
	public abstract T loadFrom(InputStream stream)
			throws FileNotFoundException, ClassNotFoundException, IOException;
}
