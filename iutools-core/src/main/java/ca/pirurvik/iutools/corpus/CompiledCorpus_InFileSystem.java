package ca.pirurvik.iutools.corpus;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import ca.inuktitutcomputing.data.Morpheme;
import ca.inuktitutcomputing.utilities.StopWatch;
import ca.inuktitutcomputing.utilities.StopWatchException;
import ca.nrc.datastructure.trie.Trie;
import ca.nrc.datastructure.trie.TrieException;
import ca.nrc.datastructure.trie.TrieNode;
import ca.nrc.datastructure.trie.Trie_InFileSystem;
import ca.nrc.datastructure.trie.Trie.NodeOption;

public class CompiledCorpus_InFileSystem extends CompiledCorpus
	{
	
	public File corpusDir = null;
	
	Trie_InFileSystem wordCharTrie = null;
	Trie_InFileSystem charNgramsTrie = null;
	Trie_InFileSystem wordMorphTrie = null;
	Trie_InFileSystem morphNgramsTrie = null;
	
	public CompiledCorpus_InFileSystem(File _corpusDir) {
		init_CompiledCorpus_InFileSystem(_corpusDir);
	}

	private void init_CompiledCorpus_InFileSystem(File _corpusDir) {
		this.corpusDir = _corpusDir;
		wordCharTrie = new Trie_InFileSystem(new File(_corpusDir, "wordCharTrie"));
		charNgramsTrie = new Trie_InFileSystem(new File(_corpusDir, "charNgramsTrie"));
		wordMorphTrie = new Trie_InFileSystem(new File(_corpusDir, "wordMorphTrie"));
		morphNgramsTrie = new Trie_InFileSystem(new File(_corpusDir, "morphNgramsTrie"));
	}

	private void addWordCharTrie(String word, String[] wordChars, 
			String[] decomps) 
			throws CompiledCorpusException {
		try {
			TrieNode node = wordCharTrie.add(wordChars, word);
			node.setField("decomps", decomps);
			wordCharTrie.saveNode(node);
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}
	}
	
	private void addCharNgramTrie(String word, String[] wordChars) 
			throws CompiledCorpusException {
		
		Set<String> ngrams = getCharsNgramCompiler().compile(word);
		for (String aNgram: ngrams) {
			try {
				charNgramsTrie.add(aNgram.split(""), aNgram);
			} catch (TrieException e) {
				throw new CompiledCorpusException(e);
			}
		}
	}

	private void addWordMorphTrie(String word, String[] decomps) throws CompiledCorpusException {
		if (decomps != null && decomps.length > 0) {
			String[] topDecomp = decomps[0].split("\\s+");
			try {
				wordMorphTrie.add(topDecomp, word);
			} catch (TrieException e) {
				throw new CompiledCorpusException(e);
			}	
		}		
	}
	
//	@Override
//	public TrieNode getMostFrequentTerminal(String[] morphemes) throws CompiledCorpusException {
//		TrieNode mostFrequent = null;
//		try {
//			mostFrequent = this.wordMorphTrie.getMostFrequentTerminal(morphemes);
//		} catch (TrieException e) {
//			throw new CompiledCorpusException(e);
//		}
//		
//		return mostFrequent;
//	}
	
	
	public Boolean isWordInCorpus(String word) throws CompiledCorpusException {
		Boolean inCorpus = null;
		try {
			inCorpus = wordCharTrie.contains(word.split(""));
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}
		
		return inCorpus;
	}

	@Override
	public Iterator<String> allWords() throws CompiledCorpusException {
		HashSet<String> allWordsSet = new HashSet<String>();
		try {
			TrieNode[] terminalNodes = wordCharTrie.getTerminals();
			for (TrieNode aTerminal: terminalNodes) {
				allWordsSet.add(aTerminal.getTerminalSurfaceForm());
			}
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}
		return allWordsSet.iterator();
	}

	@Override
	public Set<String> wordsContainingNgram(String ngram) 
			throws CompiledCorpusException {
		List<String> words = new ArrayList<String>();
		try {
			TrieNode node = charNgramsTrie.getNode(charNgramsTrie.wordChars(ngram));
			if (node != null) {
				words = node.getField("words", words);
			}
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}
		
		Set<String> wordsSet = new HashSet<String>();
		wordsSet.addAll(words);
		
		
		return wordsSet;
	}

	@Override
	protected Set<String> wordsContainingMorphNgram(String[] morphemes) 
			throws CompiledCorpusException {
		Set<String> words = new HashSet<String>();
		try {
			morphemes = morphemesWithBraces(morphemes);
			TrieNode node = 
				morphNgramsTrie.getNode(morphemes, 
					NodeOption.TERMINAL, NodeOption.NO_CREATE);
			if (node != null) {
				List<String> matchingWords = node.getField("words", new ArrayList<String>());
				words.addAll(matchingWords);
			}
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}
		
		return words;
	}

	private String[] morphemesWithBraces(String[] morphemes) {
		String[] withBraces = new String[morphemes.length];
		for (int ii=0; ii < morphemes.length; ii++) {
			String aMorpheme = morphemes[ii];
			if (!aMorpheme.matches("[\\^\\$]")) {
				withBraces[ii] = Morpheme.withBraces(aMorpheme);
			} else {
				withBraces[ii] = aMorpheme;
			}
		}

		return withBraces;
	}

	@Override
	public long totalOccurencesOf(String word) throws CompiledCorpusException {
		String[] wordChars = Arrays.copyOf(word.split(""), word.length());
		
		TrieNode wordNode;
		try {
			wordNode = this.wordCharTrie.getNode(wordChars);
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}
		return wordNode.getFrequency();
	}

	@Override
	public List<WordWithMorpheme> wordsContainingMorpheme(String morpheme) 
			throws CompiledCorpusException {
		
		Set<String> matchingMorphemes = morphemesWithCanonicalForm(morpheme);
		List<WordWithMorpheme> results = new ArrayList<WordWithMorpheme>();
		for (String morphID: matchingMorphemes) {
			try {
				TrieNode node = 
					morphNgramsTrie.getNode(
						new String[] {morphID}, NodeOption.TERMINAL);
				List<String> matchingWords = node.getField("words", new ArrayList<String>());
				for (String aWord: matchingWords) {
					WordInfo aWordInfo = info4word(aWord);
					results.add(
						new WordWithMorpheme(aWord, morphID, String.join("", aWordInfo.topDecompositions), aWordInfo.frequency));					
				}
				
				
			} catch (TrieException e) {
				throw new CompiledCorpusException(e);
			}
		}
		return results;
	}
	
	
    protected Set<String> morphemesWithCanonicalForm(String canonicalMorpheme) throws CompiledCorpusException {
    	Set<String> matchingMorphemes = new HashSet<String>();
		try {
			for (String candMorpheme: morphNgramsTrie.getRoot().childrenSegments()) {
				if (Morpheme.hasCanonicalForm(candMorpheme, canonicalMorpheme)) {
					matchingMorphemes.add(candMorpheme);
				}
			}
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}
			
		return matchingMorphemes;
	}

	@Override
	protected void updateWordIndex(
		String word, String[][] sampleDecomps, int totalDecomps) 
		throws CompiledCorpusException {
    	
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.corpus.CompiledCorpus_InFileSystem.updateWordIndex");
		long start = 0;
		TimeUnit unit = TimeUnit.MILLISECONDS;
		try {
			if (tLogger.isTraceEnabled()) {
				start = StopWatch.now(unit);
			}
			String[] chars = Trie.wordChars(word);
			if (tLogger.isTraceEnabled()) {
				tLogger.trace("wordChars took "+StopWatch.elapsedSince(start, unit)+unit);
				start = StopWatch.now(unit);
			}
			TrieNode node = wordCharTrie.add(chars, word);
			if (tLogger.isTraceEnabled()) {
				tLogger.trace("add took "+StopWatch.elapsedSince(start, unit)+unit);
				start = StopWatch.now(unit);
			}
			node.setField("topDecomps", sampleDecomps);
			if (tLogger.isTraceEnabled()) {
				tLogger.trace("setField(\"topDecomps\") took "+StopWatch.elapsedSince(start, unit)+unit);
				start = StopWatch.now(unit);
			}
			wordCharTrie.saveNode(node);
			if (tLogger.isTraceEnabled()) {
				tLogger.trace("saveNode took "+StopWatch.elapsedSince(start, unit)+unit);
				start = StopWatch.now(unit);
			}
			
		} catch (TrieException | StopWatchException e) {
			throw new CompiledCorpusException(e);
		}
	}
	
	@Override
	protected void updateDecompositionsIndex(String word, String[][] sampleDecomps, 
		int totalDecomps) throws CompiledCorpusException {
		
		String[] bestDecomp = null;
		if (sampleDecomps != null && sampleDecomps.length > 0) {
			bestDecomp = sampleDecomps[0];
		}
		
		Set<String[]> morphNgrams = getMorphsNgramCompiler().compile(bestDecomp);
		for (String[] morphNgram: morphNgrams) {
			try {
				String joinedMorphNgram = String.join(" ", morphNgram);
				//
				// TODO-June2020: Instead of passing 'word' as second argument,
				// maybe we should pass the concatenation of the written forms
				// of the morphemes?
				//
				TrieNode node = morphNgramsTrie.add(morphNgram, word);
				addWordToMorphNgram(word, node);
			} catch (TrieException e) {
				throw new CompiledCorpusException(e);
			}
		}
		
		try {
			morphNgramsTrie.add(bestDecomp, word);
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}
	}	

	private void addWordToMorphNgram(String word, TrieNode ngramNode) throws CompiledCorpusException {
		List<String> words = new ArrayList<String>();
		words = ngramNode.getField("words", words);
		words.add(word);
		try {
			morphNgramsTrie.saveNode(ngramNode);
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}
	}


	@Override
	protected void updateCharNgramIndex(
		String word, String[][] sampleDecomps, int totalDecomps) throws CompiledCorpusException {
		
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.corpus.CompiledCorpus_InFileSystem.updateCharNgramIndex");
		Logger tLogger_TIME = Logger.getLogger("ca.pirurvik.iutools.corpus.CompiledCorpus_InFileSystem.updateCharNgramIndexTIME");
		
		tLogger.trace("word = "+word);
		
		long start = 0; TimeUnit unit = TimeUnit.MILLISECONDS;
		try {
			if (tLogger_TIME.isTraceEnabled()) {
				start = StopWatch.now(unit);
			}
			Set<String> ngrams = getCharsNgramCompiler().compile(word);
			tLogger.trace("Updating a total of "+ngrams.size()+" ngrams");
			if (tLogger_TIME.isTraceEnabled()) {
				tLogger_TIME.trace("compile(); took "+
					StopWatch.elapsedSince(start, unit)+" "+unit);
				start = StopWatch.now(unit);
			}
			for (String aNgram: ngrams) {
				TrieNode node = charNgramsTrie.add(aNgram.split(""), word);
				if (tLogger_TIME.isTraceEnabled()) {
					tLogger_TIME.trace("add() took "+
						StopWatch.elapsedSince(start, unit)+" "+unit);
					start = StopWatch.now(unit);
				}				
				List<String> ngramWords = node.getField("words", new ArrayList<String>());
				if (tLogger_TIME.isTraceEnabled()) {
					tLogger_TIME.trace("getField(\"words\") took "+
						StopWatch.elapsedSince(start, unit)+" "+unit);
					start = StopWatch.now(unit);
				}				
				ngramWords.add(word);
				charNgramsTrie.saveNode(node);
				if (tLogger_TIME.isTraceEnabled()) {
					tLogger_TIME.trace("saveNode() took "+
						StopWatch.elapsedSince(start, unit)+" "+unit);
					start = StopWatch.now(unit);
				}				
			}
		} catch (TrieException | StopWatchException e) {
			throw new CompiledCorpusException(e);
		}
	}
	
	@Override
	public boolean containsWord(String word) throws CompiledCorpusException {
		boolean answer;
		try {
			answer = wordCharTrie.contains(word.split(""));
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}
		return answer;
	}

	@Override
	public String[] bestDecomposition(String word) throws CompiledCorpusException {
		String[] topDecs = null;
		TrieNode node;
		try {
			node = wordCharTrie.getNode(Trie.ensureTerminal(word.split("")));
			if (node != null) {
				topDecs = nodeBestDecomp(node);
			}
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}
				
		return topDecs;
	}

	@Override
	public long charNgramFrequency(String ngram) throws CompiledCorpusException {
		long freq = 0;
		try {
			String[] ngramChars = Trie.wordChars(ngram);
			TrieNode node = charNgramsTrie.getNode(ngramChars);
			if (node != null) {
				freq = node.getFrequency();
			}
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}
		
		return freq;
	}

	@Override
	public long morphemeNgramFrequency(String[] morphemes) throws CompiledCorpusException {
		long freq = 0;
		try {
			String[] termMorphemes = Trie.ensureTerminal(morphemes);
			TrieNode node = morphNgramsTrie.getNode(termMorphemes);
			if (node != null) {
				freq = node.getFrequency();
			}
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}
		
		return freq;
	}
	
	public long totalOccurences() throws CompiledCorpusException {
		Iterator<String> iter = allWords();
		long sumFreqs = 0;
		while (iter.hasNext()) {
			WordInfo wInfo = info4word(iter.next());
			sumFreqs += wInfo.frequency;
		}

		return sumFreqs;
	}
	@Override
	public WordInfo info4word(String word) throws CompiledCorpusException {
		WordInfo info = null;
		try {
			String[] chars = Trie.ensureTerminal(word.split(""));
			TrieNode node = 
				wordCharTrie.getNode(chars, NodeOption.NO_CREATE);
			if (node != null) {
				info = new WordInfo()
					.setFrequency(node.getFrequency())
					.setTopDecompositions(nodeBestDecomp(node))
					;
			}
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}		
		return info;
	}
	
	private String[] nodeBestDecomp(TrieNode node) {
		String[] bestDecomp = null;
		List<List<String>> topDecomps = 
			node.getField("topDecomps", new ArrayList<List<String>>());
		if (topDecomps != null && topDecomps.size() > 0) {
			List<String> bestDecompLst = topDecomps.get(0);
			bestDecomp = bestDecompLst.toArray(new String[0]);
		}
		return bestDecomp;
	}
	
	@Override
	public WordInfo[] mostFrequentWordsExtending(String[] morphemes, Integer N) 
			throws CompiledCorpusException {
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.corpus.CompiledCorpus_InFileSystem.mostFrequentWordsExtending");
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("morphemes="+String.join(",", morphemes));
		}
		
		morphemes = ensureLeading(morphemes);
		
		List<WordInfo> mostFrequentLst = new ArrayList<WordInfo>();
//		TrieNode node;
//		N = Math.min(N,  Integer.MAX_VALUE);
//		try {
//			TrieNode[] mostFreqExtensions = 
//				morphNgramsTrie.getMostFrequentTerminals(N, morphemes);
//			for (TrieNode anExtension: mostFreqExtensions) {
//				for (String word: anExtension.getSurfaceForms().keySet()) {
//					WordInfo winfo = info4word(word);
//					mostFrequentLst.add(winfo);
//				}
//			}
//		} catch (TrieException e) {
//			throw new CompiledCorpusException(e);
//		}
//		
//		return mostFrequentLst.toArray(new WordInfo[mostFrequentLst.size()]);
		
		try {
			TrieNode node = morphNgramsTrie.getNode(morphemes, NodeOption.TERMINAL);
			List<String> extensions = node.getField("words", new ArrayList<String>());
			for (String aWord: extensions) {
				WordInfo winfo = info4word(aWord);
				mostFrequentLst.add(winfo);				
			}
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}
		
		return mostFrequentLst.toArray(new WordInfo[mostFrequentLst.size()]);
	}

	private String[] ensureLeading(String[] ngram) {
		String[] withLeading = ngram;
		if (ngram != null && ngram.length > 0) {
			if (!ngram[0].equals("^")) {
				withLeading = new String[ngram.length+1];
				withLeading[0] = "^";
				for (int ii=0; ii < ngram.length; ii++) {
					withLeading[ii+1] = ngram[ii];
				}
			}
		}
		return withLeading;
	}

	private String[] ensureTailing(String[] ngram) {
		String[] withTailing = ngram;
		if (ngram != null && ngram.length > 0) {
			if (!ngram[ngram.length-1].equals("^")) {
				withTailing = new String[ngram.length+1];
				for (int ii=0; ii < ngram.length; ii++) {
					withTailing[ii] = ngram[ii];
				}
				withTailing[ngram.length-1] = "$";
			}
		}
		return withTailing;
	}

	@Override
	public long totalWords() throws CompiledCorpusException {
		long total = -1;
		try {
			total = wordCharTrie.getTerminals().length;
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}
		return total;
	}
	
	@Override
	public long totalWordsWithNoDecomp() throws CompiledCorpusException {
		long total = 0;
		try {
			TrieNode node = morphNgramsTrie.getNode(null, NodeOption.TERMINAL);
			if (node != null) {
				total = node.getSurfaceForms().size();
			}
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}
		
		return total;
	}

	@Override
	public long totalWordsWithDecomps() throws CompiledCorpusException {
		long total = totalWords() - totalWordsWithNoDecomp();
		return total;
	}

	@Override
	public long totalOccurencesWithNoDecomp() throws CompiledCorpusException {
		long total = 0;
		try {
			TrieNode node = 
				morphNgramsTrie.getNode(null, NodeOption.TERMINAL);
			for (Entry<String, Long> entry: node.getSurfaceForms().entrySet()) {
				total += entry.getValue();
			}
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}
		
		return total;
	}

	@Override
	public Long totalOccurencesWithDecomps() throws CompiledCorpusException {
		long total = totalOccurences() - totalOccurencesWithNoDecomp();
		return total;
	}
}
