package ca.pirurvik.iutools.corpus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ca.inuktitutcomputing.data.LinguisticDataException;
import ca.inuktitutcomputing.morph.Decomposition;
import ca.nrc.datastructure.trie.StringSegmenter;
import ca.nrc.datastructure.trie.StringSegmenterException;
import ca.nrc.datastructure.trie.StringSegmenter_Char;
import ca.nrc.datastructure.trie.Trie;
import ca.nrc.datastructure.trie.TrieNode;
import ca.pirurvik.iutools.corpus.CompiledCorpus_InMemory.WordWithMorpheme;

/**
 * This class stores stats about Inuktut words seen in a corpus, such as:
 * 
 * - frequency of words and ngrams
 * - word morphological decompositions
 * 
 * @author desilets
 *
 */
public abstract class CompiledCorpus_Base {
	
	public abstract Iterator<String> allWords() throws CompiledCorpusException;
	
	public abstract WordInfo info4word(String word);
	
	// TODO-June2020: Rename to exprContainingCharNgram(String ngram)
	public abstract Set<String> wordsContainingNgram(String ngram) 
			throws CompiledCorpusException;
	
	// TODO-June2020: Rename to containsExpression(String expression)
	public abstract boolean containsWord(String word);
	
	// TODO-June2020: Should rename to exprContainingSegmentNgram
	protected abstract Set<String> wordsContainingMorphNgram(String[] morphemes);
	
	// TODO-June2020: Doesn't seem like it belongs in CompiledCorpus
	//  TrieNode classes are an internal implementation detail of 
	//  implementations of CompiledCorpus which use Tries.
	//
	//  Some CompiledCorpus implementation may not use Tries at all...
	//
	protected abstract TrieNode[] getAllTerminals() throws CompiledCorpusException;
	
	public abstract String[] topSegmentation(String word);
	
	// TODO-June2020: Should probably choose a better name
	protected abstract void addToWordSegmentations(String word,String[] segments) 
		throws CompiledCorpusException;
	
	protected abstract void addToWordCharIndex(String word, String[] segments) throws CompiledCorpusException;

	
	// TODO-June2020: Should probably choose a better name
	protected abstract void addToWordNGrams(String word, String[] morphemes) throws CompiledCorpusException;
	
	// TODO-June2020: Should move this out ouf CompiledCorpus_Base.
	protected abstract void addToDecomposedWordsSuite(String word);

	protected String segmenterClassName = StringSegmenter_Char.class.getName();
	protected transient StringSegmenter segmenter = null;
	
	// TODO-June2020: Is this really necessary?
	//   Why don't we just look up in the chars trie to 
	//   see if the word has already been analyzed?	
	private HashMap<String,String[]> segmentsCache = new HashMap<String, String[]>();
	
	private int decompsSampleSize = Integer.MAX_VALUE;
	
	// TODO-June2020: Is this still needed?
	@JsonIgnore
	public transient boolean verbose = true;
	
	@JsonIgnore
	public transient String name;
	
	public abstract long charNgramFrequency(String ngram);
	
	public CompiledCorpus_Base() {
		initialize(StringSegmenter_Char.class.getName()); 
	}
	
	public CompiledCorpus_Base(String segmenterClassName) {
		initialize(segmenterClassName);
	}

	public void initialize(String _segmenterClassName) {
		if (_segmenterClassName != null) {
			this.segmenterClassName = _segmenterClassName;
		}
	}
	
	public CompiledCorpus_Base setVerbose(boolean value) {
		verbose = value;
		return this;
	}
	
	public CompiledCorpus_Base setName(String _name) {
		name = _name;
		return this;
	}
	
	public CompiledCorpus_Base setSegmenterClassName(String className) {
		segmenterClassName = className;
		return this;
	}
	

	public CompiledCorpus_Base setDecompsSampleSize(int size) {
		return this;
	}

	public void addWordOccurences(String[] words) throws CompiledCorpusException {
		for (String aWord: words) {
			addWordOccurence(aWord);
		}
	}
	
	public void addWordOccurence(String word) throws CompiledCorpusException {
		String[] segments = fetchSegmentsFromCache(word);
		if (segments == null) {
			segments = segmentText(word);
			addToCache(word, segments);	
		}
		// new word decomposed or word that now decomposed: add to word segmentations string
		if (segments.length != 0) {
			addToWordCharIndex(word, segments);
			addToWordSegmentations(word,segments);
			addToWordNGrams(word, segments);
			addToDecomposedWordsSuite(word);
		}
	}

	// TODO-June2020: Is this really necessary?
	//   Why don't we just look up in the chars trie to 
	//   see if the word has already been analyzed?	
	protected void addToCache(String word, String[] segments) {
		getSegmentsCache().put(word, segments);
	}
		
	// TODO-June2020: Is this really necessary?
	//   Why don't we just look up in the chars trie to 
	//   see if the word has already been analyzed?
	String[] fetchSegmentsFromCache(String word) {
		String[] segmentsFromCache = null;
		if (getSegmentsCache().containsKey(word)) {
			segmentsFromCache = getSegmentsCache().get(word);
		}
		return segmentsFromCache;
	}	
	
	// TODO-June2020: Is this really necessary?
	//   Why don't we just look up in the chars trie to 
	//   see if the word has already been analyzed?	
	public HashMap<String,String[]> getSegmentsCache() {
		return segmentsCache;
	}

	public void setSegmentsCache(HashMap<String,String[]> segmentsCache) {
		this.segmentsCache = segmentsCache;
	}

//	@JsonIgnore
//	public void setWordDecompositions(Decomposition[] decomps) {
//		// TODO Auto-generated method stub
//		
//	}

	public abstract String getWordSegmentations();
	
	public abstract long getNbOccurrencesOfWord(String word) throws CompiledCorpusException;
	
	public abstract Trie getTrie();
	
	public abstract List<WordWithMorpheme> getWordsContainingMorpheme(String morpheme) throws CompiledCorpusException;

	public abstract long morphemeNgramFrequency(String[] ngram) throws CompiledCorpusException;
	
	@JsonIgnore
	protected StringSegmenter getSegmenter() throws CompiledCorpusException {
		if (segmenter == null) {
			Class cls;
			try {
				cls = Class.forName(segmenterClassName);
			} catch (ClassNotFoundException e) {
				throw new CompiledCorpusException(e);
			}
			try {
				segmenter = (StringSegmenter) cls.newInstance();
			} catch (Exception e) {
				throw new CompiledCorpusException(e);
			}
		}
		return segmenter;
	}
			
	public String[] segmentText(String text) throws CompiledCorpusException {
		String[] segments;
		try {
			segments = getSegmenter().segment(text);
		} catch (TimeoutException | StringSegmenterException | LinguisticDataException | CompiledCorpusException e) {
			throw new CompiledCorpusException(e);
		}
		return segments;
	}
	
	public void disactivateSegmenterTimeout() throws CompiledCorpusException {
        getSegmenter().disactivateTimeout();
	}
	
	public boolean containsCharNgram(String ngram) throws CompiledCorpusException {
		long numWords = wordsContainingNgram(ngram).size();
		return numWords > 0;
	}
}
