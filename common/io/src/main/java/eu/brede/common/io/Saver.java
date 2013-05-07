package eu.brede.common.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public abstract class Saver<T> {
	public void saveTo(String fileName) throws FileNotFoundException, IOException {
		saveTo(new BufferedOutputStream(new FileOutputStream(fileName)));
	}
	public void saveTo(File file) throws FileNotFoundException, IOException {
		saveTo(file.getAbsolutePath());
	}
	
	public abstract void saveTo(OutputStream stream) throws FileNotFoundException, IOException;
	public abstract T getObject();
}
