package eu.brede.common.io;

import au.com.bytecode.opencsv.CSVWriter;

import java.io.*;
import java.util.Collection;


public abstract class CSVSaver<U extends Stringifiable, T extends TxtExportable<U>>
        extends Saver<T> {
    public CSVExporter exporter = new CSVExporter();

	@Override
	public void saveTo(OutputStream stream) throws FileNotFoundException,
			IOException {
		try {
            T object = getObject();
            CSVWriter writer = new CSVWriter(new OutputStreamWriter(stream, "UTF-8"),
                    ',', CSVWriter.NO_QUOTE_CHARACTER);
            String[] line;
            Collection<String> stringCollection = object.toStringCollection();
            line = (String[]) stringCollection.toArray(
                    (new String[stringCollection.size()]));
            writer.writeNext(line);

            for (Stringifiable item : object) {
                stringCollection = item.toStringCollection();
                line = (String[]) stringCollection.toArray(
                        (new String[stringCollection.size()]));
                writer.writeNext(line);
            }

            writer.close();
//			outputStream.writeObject(getObject());
//			outputStream.flush();
//			outputStream.close();
		}
        finally {
            stream.close();
        }
	}

}
