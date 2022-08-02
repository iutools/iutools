package org.iutools.bin;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.iutools.corpus.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ca.nrc.datastructure.Pair;
import org.iutools.corpus.elasticsearch.CompiledCorpus_ES;
import org.iutools.datastructure.trie.TrieException;

public class FreqVerbRootsCompiler {
	
	private String defaultSorting = "freq";
	public String sorting;

	
	public FreqVerbRootsCompiler() {
	}
	
	public HashMap<String,Long> compileFreqs(CompiledCorpus corpus) throws TrieException, CompiledCorpusException {
		Logger logger = LogManager.getLogger("FreqVerbRootsCompiler.compileFreqs");
		HashMap<String,Long> freqsOfVerbRoots = new HashMap<String,Long>();
		Iterator<String> wordsIter = corpus.allWords();
		Pattern pattVerb = Pattern.compile("\\{(.+/\\d+v)\\}");

		while (wordsIter.hasNext()) {
			String word = wordsIter.next();
			WordInfo winfo = corpus.info4word(word);
			String[] topDecomp = winfo.topDecomposition();
			if (topDecomp != null && topDecomp.length > 0) {
				String wordRoot = topDecomp[0];
				Long freq = winfo.frequency;

				Matcher mat = pattVerb.matcher(wordRoot);
				if (mat.matches()) {
					String morpheme = mat.group(1);
					if (!freqsOfVerbRoots.containsKey(morpheme)) {
						freqsOfVerbRoots.put(morpheme, new Long(0));
					}
					long oldFreq = freqsOfVerbRoots.get(morpheme);
					freqsOfVerbRoots.put(mat.group(1), oldFreq+freq);
				}
			}
		}

		return freqsOfVerbRoots;
	}

	public static void main(String[] args) throws TrieException {

		FreqVerbRootsCompiler freqCompiler = new FreqVerbRootsCompiler();

		String corpusName = null;
		String corpusDirectoryPathname = null;
		String sorting = null;

		if (args.length==0) {
			printHelp();
			System.exit(0);
		} 	else if ( args[0].startsWith("-")) {
			if (args[0].equals("-h")) {
				printHelp();
				System.exit(0);
			} else if (args[0].equals("-r")) {
				sorting = "root";
			} else if (args[0].equals("-f")) {
				sorting = "freq";
			} else {
				printHelp();
				System.exit(0);
			}
			corpusDirectoryPathname = args[1];
		} else {
			sorting = freqCompiler.defaultSorting;
			corpusName = args[0];
		}

		try {
			CompiledCorpus corpus =
				new CompiledCorpusRegistry().getCorpus(corpusName);
			HashMap<String,Long> freqsOfVerbRoots = freqCompiler.compileFreqs(corpus);
			
			if (sorting.equals("root")) {
				Iterator<String> iter = freqsOfVerbRoots.keySet().iterator();
				List<String> verbRootIds = new ArrayList<String>();
				while (iter.hasNext()) {
					verbRootIds.add(iter.next());
				}
				Collections.sort(verbRootIds);
				for (int i=0; i<verbRootIds.size(); i++) {
					System.out.println(verbRootIds.get(i)+","+freqsOfVerbRoots.get(verbRootIds.get(i)));
				}
			} else {
				Iterator<String> iter = freqsOfVerbRoots.keySet().iterator();
				List<Pair<String,Long>> rootFreqPairs = new ArrayList<Pair<String,Long>>();
				int iPair = 0;
				while (iter.hasNext()) {
					String root = iter.next();
					rootFreqPairs.add(Pair.of(root,freqsOfVerbRoots.get(root)));
				}
				RootFreqComparator comparator = freqCompiler.new RootFreqComparator();
				Collections.sort(rootFreqPairs,comparator);
				for (int i=0; i<rootFreqPairs.size(); i++) {
					Pair<String,Long> pair = rootFreqPairs.get(i);
					System.out.println(pair.getFirst()+","+freqsOfVerbRoots.get(pair.getFirst()));
				}
			}
		} catch (CompiledCorpusRegistryException | CompiledCorpusException e) {
			System.err.println(e.getMessage());
		}
	}
	
	
	public class RootFreqComparator implements Comparator<Pair<String,Long>> {
	    @Override
	    public int compare(Pair<String,Long> a, Pair<String,Long> b) {
	    	if (a.getSecond().longValue() > b.getSecond().longValue())
	    		return -1;
	    	else if (a.getSecond().longValue() < b.getSecond().longValue())
				return 1;
	    	else 
	    		return 0;
	    }
	}

	private static void printHelp() {
		System.out.println("Print the roots of the words in a given corpus and their frequencies.");
		System.out.println(MethodHandles.lookup().lookupClass().getCanonicalName()+" options* corpusName");
		System.out.println("Options:");
		System.out.println("-h : print this message");
		System.out.println("-r : print in alphabetical order of roots");
		System.out.println("-f : print in highest-first order of frequencies");
	}

	
}
