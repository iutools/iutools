package ca.inuktitutcomputing.applications;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import com.google.gson.Gson;

import ca.pirurvik.iutools.corpus.CompiledCorpus;

public class WordsNotDecomposed {
	
	static private int limite = -1; //200;

	public static void main(String[] args) throws FileNotFoundException {
		String compilationFilePath = args[0];
		FileReader fr = new FileReader(compilationFilePath);
		CompiledCorpus compiledCorpus = new Gson().fromJson(fr, CompiledCorpus.class);
		HashMap<String,Long> wordsThatWereNotDecomposed = compiledCorpus.getWordsThatFailedSegmentationWithFreqs();
		int nWords = wordsThatWereNotDecomposed.size();
		Object[][] objs = new Object[nWords][2];
		HashMap<Long,Long> freqs = new HashMap<Long,Long>();
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
	    	Long freq = (Long)o[1];
	    	System.out.println(o[0]+","+o[1]);
	    	if (freqs.containsKey(freq))
	    		freqs.put(freq, freqs.get(freq)+1);
	    	else
	    		freqs.put(freq, new Long(1));
	    	if (j++ == limite) break;
	    }
	    System.out.println("Nb. mots: "+nWords);
	    
	    System.out.println("\n#occ. : #words\n");
	    
	    Object[][] frqs = new Object[freqs.size()][2];
		Iterator<Long> iteratorfrq = freqs.keySet().iterator();
		int k = 0;
	    while(iteratorfrq.hasNext()) {
	    	Long freq = iteratorfrq.next();
	        frqs[k++] = new Object[] {freq,freqs.get(freq)};
	    }
	    Arrays.sort(frqs, (Object[] a, Object[] b) -> {
	    	return ((Long)b[0]).compareTo((Long)a[0]);
	    });
	    for (k=0; k<frqs.length; k++)
	    	System.out.println(frqs[k][0]+" : "+frqs[k][1]);
	}

}
