package ca.inuktitutcomputing.core;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

public class CorpusReader_Directory extends CorpusReader {
	
	public Iterator<?> getFiles(String corpusDirectoryPathname) {
		File corpusDirectory = new File(corpusDirectoryPathname);
    	File [] files = corpusDirectory.listFiles(
    			new FilenameFilter() {
    				public boolean accept(File dir, String name) {
    					return !name.equals(".") && !name.equals("..");
    				}
    			});
    	Collection<CorpusDocument_File> collection = new Vector<CorpusDocument_File>();
    	for (int i = 0; i<files.length; i++) {
    		CorpusDocument_File cdf = new CorpusDocument_File(files[i].getAbsolutePath());
    		if (cdf.hasContents())
    			collection.add(cdf);
    	}
		return collection.iterator();
	}
	
}
