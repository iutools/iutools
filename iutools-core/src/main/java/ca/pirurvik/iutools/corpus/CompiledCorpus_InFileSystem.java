package ca.pirurvik.iutools.corpus;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
			node.setData("decomps", decomps);
			wordCharTrie.saveNode(node);
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}
	}
	
	private void addCharNgramTrie(String word, String[] wordChars) 
			throws CompiledCorpusException {
		
		NgramCompiler compiler = new NgramCompiler(true);
		HashSet<String> ngrams = ngramCompiler.compile(word);
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
	public WordInfo info4word(String word) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> wordsContainingNgram(String ngram) 
			throws CompiledCorpusException {
		return super.wordsContainingNgram(ngram);
//		try {
//			TrieNode node = charNgramsTrie.getNode(charNgramsTrie.wordChars(ngram));
//		} catch (TrieException e) {
//			throw new CompiledCorpusException(e);
//		}
	}

	@Override
	protected Set<String> wordsContainingMorphNgram(String[] morphemes) {
		return super.wordsContainingMorphNgram(morphemes);
	}

	@Override
	public String getWordSegmentations() {
		return super.getWordSegmentations();
	}

	@Override
	public long getNbOccurrencesOfWord(String word) throws CompiledCorpusException {
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

	@Override
	public List<WordWithMorpheme> getWordsContainingMorpheme(String morpheme) throws CompiledCorpusException {
		return super.getWordsContainingMorpheme(morpheme);
	}
	
	
    @Override
	protected void addToWordCharIndex(String word, String[] segments) 
		throws CompiledCorpusException {
    	super.addToWordCharIndex(word, segments);
		try {
			wordCharTrie.add(word.split(""), word);
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}
		
	}
	
	@Override
	protected void addToWordSegmentations(String word,String[] segments) throws CompiledCorpusException {
		super.addToWordSegmentations(word, segments);
		try {
			morphNgramsTrie.add(segments, word);
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}
	}	

	@Override
	protected void addToWordNGrams(String word, String[] segments) throws CompiledCorpusException {
		super.addToWordNGrams(word, segments);
		try {
			charNgramsTrie.add(word.split(""), word);
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}
	}

}
