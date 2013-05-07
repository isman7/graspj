package eu.brede.common.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;


public class StringLoader extends Loader<String> {

	private String encoding = "UTF-8";

	@Override
	public String loadFrom(InputStream stream)
			throws FileNotFoundException, ClassNotFoundException, IOException {
		StringBuilder text = new StringBuilder();
	    String NL = System.getProperty("line.separator");
	    Scanner scanner = new Scanner(stream, encoding);
	    try {
	      while (scanner.hasNextLine()){
	        text.append(scanner.nextLine() + NL);
	      }
	    }
	    finally{
	      scanner.close();
	    }
		return text.toString();
	}

}
