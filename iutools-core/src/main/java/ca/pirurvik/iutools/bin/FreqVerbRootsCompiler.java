package ca.pirurvik.iutools.bin;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.pirurvik.iutools.corpus.*;
import org.apache.log4j.Logger;

import ca.nrc.datastructure.Pair;
import ca.nrc.datastructure.trie.Trie;
import ca.nrc.datastructure.trie.TrieException;
import ca.nrc.datastructure.trie.TrieNode;
import ca.nrc.json.PrettyPrinter;

public class FreqVerbRootsCompiler {
	
	private String defaultSorting = "freq";
	public String sorting;

	
	public FreqVerbRootsCompiler() {
	}
	
	public HashMap<String,Long> compileFreqs(CompiledCorpus corpus) throws TrieException, CompiledCorpusException {
		Logger logger = Logger.getLogger("FreqVerbRootsCompiler.compileFreqs");
		HashMap<String,Long> freqsOfVerbRoots = new HashMap<String,Long>();
		Trie trie = corpus.getMorphNgramsTrie();
		TrieNode[] wordRootNodes = trie.getRoot(true).childrenNodes();
		Map<String,TrieNode> nodesOfRootsOfWords = trie.getRoot().getChildren();
		String rootIds[] = nodesOfRootsOfWords.keySet().toArray(new String[] {});
		logger.debug("rootIds: "+PrettyPrinter.print(rootIds));
		Pattern pat = Pattern.compile("\\{(.+/\\d+v)\\}");
		for (TrieNode aWordRootNode: wordRootNodes) {
			Long freq = (long)0;
			String rootId = aWordRootNode.keysAsString();
			logger.debug("rootId: "+rootId);
			Matcher mat = pat.matcher(rootId);
			if (mat.matches()) {
				freq = aWordRootNode.getFrequency();
				freqsOfVerbRoots.put(mat.group(1), freq);
			}
		}
		return freqsOfVerbRoots;
	}

	public static void main(String[] args) throws TrieException {
		FreqVerbRootsCompiler freqCompiler = new FreqVerbRootsCompiler();
		String corpusDirectoryPathname = null;
		String sorting = null;
		if (args.length==0) {
			printHelp();
			System.exit(0);
		}
		else if ( args[0].startsWith("-")) {
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
			corpusDirectoryPathname = args[0];
		}
		try {
			File corpusJsonFile = new File(corpusDirectoryPathname+"/"+"trie_compilation.json");
			String corpusName = "work-corpus";
			CompiledCorpusRegistry.registerCorpus(corpusName,corpusJsonFile);
			CompiledCorpus corpus = CompiledCorpusRegistry.getCorpusWithName_ES(corpusName);
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
		System.out.println("(The information is read in the file trie_compilation.json in the given corpus directory.)");
		System.out.println(MethodHandles.lookup().lookupClass().getCanonicalName()+" options* corpus_directory_pathname");
		System.out.println("Options:");
		System.out.println("-h : print this message");
		System.out.println("-r : print in alphabetical order of roots");
		System.out.println("-f : print in highest-first order of frequencies");
	}

	
}
