package ca.inuktitutcomputing.applications;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import com.google.gson.Gson;

import ca.inuktitutcomputing.core.CompiledCorpus;

public class WordsNotDecomposed {
	
	static private int limite = 200;

	public static void main(String[] args) throws FileNotFoundException {
		String compilationFilePath = args[0];
		FileReader fr = new FileReader(compilationFilePath);
		CompiledCorpus compiledCorpus = new Gson().fromJson(fr, CompiledCorpus.class);
		HashMap<String,Long> wordsThatWereNotDecomposed = compiledCorpus.getWordsThatFailedSegmentationWithFreqs();
		Object[][] objs = new Object[wordsThatWereNotDecomposed.size()][2];
		Iterator<String> iterator = wordsThatWereNotDecomposed.keySet().iterator();
		int i = 0;
	    while(iterator.hasNext()) {
	    	String word = iterator.next();
	        objs[i++] = new Object[] {word,wordsThatWereNotDecomposed.get(word)};
	    }
	    Arrays.sort(objs, (Object[] a, Object[] b) -> {
	    	return ((Long)b[1]).compareTo((Long)a[1]);
	    });
	    int j=0;
	    for (Object[] o : objs) {
	    	System.out.println(o[0]+","+o[1]);
	    	if (j++ == limite) break;
	    }
	}

}
