package org.iutools.corpus;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;

public class CorpusReader_Directory extends CorpusReader {
	
	public Iterator<CorpusDocument_File> getFiles(String corpusDirectoryPathname) {
		File corpusDirectory = new File(corpusDirectoryPathname);
    	File [] files = corpusDirectory.listFiles(
    			new FilenameFilter() {
    				public boolean accept(File dir, String name) {
    					return !name.equals(".") && !name.equals("..");
    				}
    			});
    	if ( files == null ) files = new File[0];
    	Arrays.sort(files, new FileNameComparator());
    	Collection<CorpusDocument_File> collection = new Vector<CorpusDocument_File>();
    	for (int i = 0; i<files.length; i++) {
			CorpusDocument_File cdf = new CorpusDocument_File(files[i].getAbsolutePath());
    		if (files[i].isDirectory()) 
    			collection.add(cdf);
    		else if (cdf.hasContents())
    				collection.add(cdf);
    	}
		return collection.iterator();
	}
	
}


class FileNameComparator implements Comparator<File> {
	    @Override
	    public int compare(File a, File b) {
	        return a.getName().compareToIgnoreCase(b.getName());
	    }
	}

