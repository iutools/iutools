/*
 * Conseil national de recherche Canada 2007/
 * National Research Council Canada 2007
 * 
 * Créé le / Created on Apr 27, 2007
 * par / by Benoit Farley
 * 
 */
package ca.inuktitutcomputing.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;

public class Examples {

    public String filename = "examples.txt";
    
    public Examples() {
        
    }
    
    public InputStream getExampleStream() {
    	File file = new File(locateFile(filename));
        InputStream targetStream = null;
		try {
			targetStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
        return targetStream;
        }
    
	public String locateFile(String fileName) {
    	ClassLoader classLoader = getClass().getClassLoader();
    	Package pk = getClass().getPackage();
    	String packagePath = pk.getName().replace('.', '/');
    	String fullFilename = packagePath + '/' + fileName;
		URL res = classLoader.getResource(fullFilename);
		String filePath = res.getPath();
		String filePathRep = filePath.replaceAll("%20", " ");
		return filePathRep;
	}
	
    
	
}
