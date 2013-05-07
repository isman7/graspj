package eu.brede.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

public class ResourceTools {

    public static void main(String[] args) throws Exception {
//        ResourceTools sts = new ResourceTools();
//
//        /*
//         * Get input stream of our data file. This file can be in
//         * the root of you application folder or inside a jar file
//         * if the program is packed as a jar.
//         */
//        InputStream is =
//                sts.getClass().getResourceAsStream("/data.txt");
//
//        /*
//         * Call the method to convert the stream to string
//         */
//        System.out.println(sts.convertStreamToString(is));
    }

    
    // TODO clean up!
    public static String convertStreamToString(InputStream is) {
        /*
         * To convert the InputStream to String we use the
         * Reader.read(char[] buffer) method. We iterate until the
         * Reader return -1 which means there's no more data to
         * read. We use the StringWriter class to produce the string.
         */
        if (is != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(
                        new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
                try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
            return writer.toString();
        } else {        
            return "";
        }
    }
    
    public static String getResourceAsString(String resourceURI) {
    	return convertStreamToString(
    			ResourceTools.class.getClassLoader().getResourceAsStream(resourceURI));
    }
}
