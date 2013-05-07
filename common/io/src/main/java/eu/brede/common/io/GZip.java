package eu.brede.common.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZip {
	public static <T> Saver<T> wrapSaver(final Saver<T> saver) {
		return new Saver<T>() {

			@Override
			public void saveTo(OutputStream stream)
					throws FileNotFoundException, IOException {
				
				try (GZIPOutputStream zippedStream = new GZIPOutputStream(stream);) {
					saver.saveTo(zippedStream);
				}
			}

			@Override
			public T getObject() {
				return saver.getObject();
			}
			
		};
	}
	
	public static <T> Loader<T> wrapLoader(final Loader<T> loader) {
		return new Loader<T>() {

			@Override
			public T loadFrom(InputStream stream) throws FileNotFoundException,
					ClassNotFoundException, IOException {
				
				try (GZIPInputStream unzippedStream = new GZIPInputStream(stream);) {			
					return loader.loadFrom(unzippedStream);
				}
			}
			
		};
	}
}
