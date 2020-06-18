package ca.pirurvik.iutools.corpus;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import ca.nrc.datastructure.trie.Trie;
import ca.nrc.datastructure.trie.TrieException;
import ca.nrc.datastructure.trie.TrieNode;
import ca.nrc.datastructure.trie.Trie_InFileSystem;
import ca.pirurvik.iutools.text.ngrams.NgramCompiler;

public class CompiledCorpus_InFileSystem extends CompiledCorpus_InMemory {
	
	public File corpusDir = null;
	
	Trie_InFileSystem wordCharTrie = null;
	Trie_InFileSystem charNgramsTrie = null;
	Trie_InFileSystem wordMorphTrie = null;
	// TODO-June2020: Make that private again after debugging
	public Trie_InFileSystem morphNgramsTrie = null;
	
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

	@Override
	public void addWord(String word, String[] decomps) throws CompiledCorpusException {
		super.addWord(word,  decomps);
		String[] wordChars = word.split("");
		addWordCharTrie(word, wordChars, decomps);
		addWordMorphTrie(word, decomps);
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
		
		Set<String> ngrams = getNgramCompiler().compile(word);
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
	
	@Override
	protected TrieNode[] getAllTerminals() throws CompiledCorpusException {
		try {
			return this.wordMorphTrie.getTerminals();
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}
	}
	
	@Override
	public TrieNode getMostFrequentTerminal(String[] morphemes) throws CompiledCorpusException {
		TrieNode mostFrequent = null;
		try {
			mostFrequent = this.wordMorphTrie.getMostFrequentTerminal(morphemes);
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}
		
		return mostFrequent;
	}
	
	
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
		return super.wordsContainingMorphNgram(morphemes);
	}

	// TODO-June2020: Make this method independant the 'super' implementation
	@Override
	public String getWordSegmentations() {
		return super.getWordSegmentations();
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
	public Trie getTrie() {
		return super.getTrie();
	}

	// TODO-June2020: Make this method independant the 'super' implementation
	@Override
	public List<WordWithMorpheme> wordsContainingMorpheme(String morpheme) throws CompiledCorpusException {
		return super.wordsContainingMorpheme(morpheme);
	}
	
	
    @Override
	protected void addToWordCharIndex(
		String word, String[][] decomps) 
		throws CompiledCorpusException {
		try {
			String[] chars = Trie.wordChars(word);
			TrieNode node = wordCharTrie.getNode(chars);
			node.setField("topDecomps", decomps);
			wordCharTrie.add(word.split(""), word);
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}
	}
	
	@Override
	protected void addToWordSegmentations(String word, String[][] decomps) throws CompiledCorpusException {
		String[] bestDecomp = new String[0];
		if (decomps != null && decomps.length > 0) {
			bestDecomp = decomps[0];
		}
		for (int start=0; start < bestDecomp.length; start++) {
			for (int end=start+1; end < bestDecomp.length+1; end++) {
				String[] morphNgram = 
					Arrays.copyOfRange(bestDecomp, start, end);
				try {
					String joinedMorphNgram = String.join(" ", morphNgram);
					TrieNode node = morphNgramsTrie.add(morphNgram, word);
				} catch (TrieException e) {
					throw new CompiledCorpusException(e);
				}
			}
		}
		
		try {
			morphNgramsTrie.add(bestDecomp, word);
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}
	}	

	@Override
	protected void addToWordNGrams(
		String word, String[][] decomps) throws CompiledCorpusException {
		super.addToWordNGrams(word, decomps);
		
		try {
			Set<String> ngrams = getNgramCompiler().compile(word);
			for (String aNgram: ngrams) {
				TrieNode node = charNgramsTrie.add(aNgram.split(""), word);
				List<String> ngramWords = node.getField("words", new ArrayList<String>());
				ngramWords.add(word);
				charNgramsTrie.saveNode(node);
			}
		} catch (TrieException e) {
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
	public String[] topDecompositions(String word) throws CompiledCorpusException {
		String[] topDec = null;
		TrieNode node;
		try {
			node = wordCharTrie.getNode(Trie.ensureTerminal(word.split("")));
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}
		
		if (node != null) {
			String[][] topDecomps = 
				node.getField("topDecomps", new String[0][]);
			if (topDecomps.length > 0) {
				topDec = topDecomps[0];
			}
		}
		
		return topDec;
	}

	// TODO-June2020: Make this method independant of CompiledCorpus_InMemory
	@Override
	protected void addToDecomposedWordsSuite(String word) {
		super.addToDecomposedWordsSuite(word);
	}

	@Override
	public long charNgramFrequency(String ngram) throws CompiledCorpusException {
//		return super.charNgramFrequency(ngram);
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
			TrieNode node = wordCharTrie.getNode(Trie.ensureTerminal(word.split("")));
			if (node != null) {
				info = new WordInfo()
					.setFrequency(node.getFrequency());
			}
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}		
		return info;
	}
	
	// TODO-June2020: Make this method independant of super impl.
	@Override
	public long totalWords() throws CompiledCorpusException {
		return super.totalWords();
	}	
	
	@Override
	public WordInfo[] mostFrequentWordsExtending(String[] morphemes, Integer N) 
			throws CompiledCorpusException {
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.corpus.CompiledCorpus_InFileSystem.mostFrequentWordsExtending");
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("morphemes="+String.join(",", morphemes));
		}
		
		List<WordInfo> mostFrequentLst = new ArrayList<WordInfo>();
		TrieNode node;
		N = Math.min(N,  Integer.MAX_VALUE);
		try {
			TrieNode[] mostFreqExtensions = 
				morphNgramsTrie.getMostFrequentTerminals(N, morphemes);
			for (TrieNode anExtension: mostFreqExtensions) {
				for (String word: anExtension.getSurfaceForms().keySet()) {
					WordInfo winfo = info4word(word);
					mostFrequentLst.add(winfo);
				}
			}
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}
		
		return mostFrequentLst.toArray(new WordInfo[mostFrequentLst.size()]);
	}
}
