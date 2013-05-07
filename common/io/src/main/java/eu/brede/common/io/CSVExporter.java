package eu.brede.common.io;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;


import au.com.bytecode.opencsv.CSVWriter;

public class CSVExporter implements TxtExporter {

	@Override
	public void exportToFile(TxtExportable<? extends Stringifiable> object, String fileName) {
    	try
    	{
			CSVWriter writer = new CSVWriter(new FileWriter(fileName));
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
			
		}
    	catch (IOException e)
    	{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

