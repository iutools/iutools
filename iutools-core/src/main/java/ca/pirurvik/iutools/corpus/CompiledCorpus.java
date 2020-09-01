package ca.pirurvik.iutools.corpus;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ca.inuktitutcomputing.data.LinguisticDataException;
import ca.nrc.datastructure.trie.StringSegmenter;
import ca.nrc.datastructure.trie.StringSegmenterException;
import ca.nrc.datastructure.trie.StringSegmenter_Char;
import ca.nrc.datastructure.trie.Trie;
import ca.pirurvik.iutools.text.ngrams.NgramCompiler;

/**
 * This class stores stats about Inuktut words seen in a corpus, such as:
 * 
 * - frequency of words and ngrams
 * - word morphological decompositions
 * 
 * @author desilets
 *
 */
public abstract class CompiledCorpus {
	
	public abstract Iterator<String> allWords() throws CompiledCorpusException;
	
	public abstract WordInfo info4word(String word) throws CompiledCorpusException;
	
	public abstract void updateDecompositionsIndex(WordInfo winfo) 
		throws CompiledCorpusException;

	public abstract void regenerateMorphNgramsIndex()
		throws CompiledCorpusException;

	public abstract Set<String> wordsContainingNgram(String ngram) 
			throws CompiledCorpusException;
	
	public abstract boolean containsWord(String word) throws CompiledCorpusException;
		
	protected abstract Set<String> wordsContainingMorphNgram(String[] morphemes) 
			throws CompiledCorpusException;
	
	public abstract long totalOccurences() throws CompiledCorpusException;
	
	public abstract long totalWordsWithNoDecomp() throws CompiledCorpusException;
	public abstract long totalWordsWithDecomps() throws CompiledCorpusException;
    public abstract long totalOccurencesWithNoDecomp() throws CompiledCorpusException;
	public abstract Long totalOccurencesWithDecomps() throws CompiledCorpusException;
	
	public abstract Iterator<String> wordsWithNoDecomposition() throws CompiledCorpusException;

	
	public abstract String[] bestDecomposition(String word) throws CompiledCorpusException;
	
	public abstract WordInfo[] mostFrequentWordsExtending(
			String[] morphemes, Integer N) throws CompiledCorpusException;

	protected abstract void addWordOccurence(String word, String[][] sampleDecomps, 
			Integer totalDecomps, long freqIncr) throws CompiledCorpusException;
	
	// TODO-June2020: Get rid of this method.
	//   The trie is an internal detail of the Corpus
	@JsonIgnore
	public abstract Trie getMorphNgramsTrie() throws CompiledCorpusException;
	
	protected String segmenterClassName = StringSegmenter_Char.class.getName();
	protected transient StringSegmenter segmenter = null;
		
	private int decompsSampleSize = 10;
	
	@JsonIgnore
	public transient String name;
	
	protected transient NgramCompiler charsNgramCompiler = null;	
	protected transient NgramCompiler morphsNgramCompiler = null;
	
	public abstract long charNgramFrequency(String ngram) throws CompiledCorpusException;
	
	public CompiledCorpus() {
		initialize(StringSegmenter_Char.class.getName()); 
	}
	
	public CompiledCorpus(String segmenterClassName) {
		initialize(segmenterClassName);
	}

	public void initialize(String _segmenterClassName) {
		if (_segmenterClassName != null) {
			this.segmenterClassName = _segmenterClassName;
		}
	}
	
	public CompiledCorpus setName(String _name) {
		name = _name;
		return this;
	}
	
	public CompiledCorpus setSegmenterClassName(String className) {
		segmenterClassName = className;
		return this;
	}

	public CompiledCorpus setSegmenterClassName(
			Class<? extends StringSegmenter> segClass) {
		segmenterClassName = segClass.getName();
		return this;
	}

	public CompiledCorpus setDecompsSampleSize(int size) {
		return this;
	}

	public void addWordOccurences(String[] words) 
			throws CompiledCorpusException {
		for (String aWord: words) {
			addWordOccurence(aWord);
		}		
	}
	
	public void addWordOccurences(Collection<String> words) 
			throws CompiledCorpusException {
		String[] wordsArr = new String[words.size()];
		int pos = 0;
		for (String aWord: words) {
			wordsArr[pos] = aWord;
			pos++;
		}
		addWordOccurences(wordsArr);
	}
	
	public void addWordOccurence(String word) throws CompiledCorpusException {
		addWordOccurence(word, false);
	}
	
	public void addWordOccurence(String word, boolean frequenciesOnly) throws CompiledCorpusException {
		
		String[][] sampleDecomps = null;
		Integer totalDecomps = null;
		if (!frequenciesOnly) {
			String[][] decomps = null;
			try {
				decomps = getSegmenter().possibleSegmentations(word);
			} catch (TimeoutException | StringSegmenterException e) {
				throw new CompiledCorpusException(e);
			}
			 
			if (decomps != null) {
				totalDecomps = decomps.length;
				int numToKeep = Math.min(totalDecomps, decompsSampleSize);
				sampleDecomps = Arrays.copyOfRange(decomps, 0, numToKeep);
			}		
		}
		addWordOccurence(word, sampleDecomps, totalDecomps);
		return;
	}
	
	private void addWordOccurence(String word, String[][] sampleDecomps, 
			Integer totalDecomps) throws CompiledCorpusException {
		addWordOccurence(word, sampleDecomps, totalDecomps, 1);
	}
	
		
	public abstract long totalOccurencesOf(String word) throws CompiledCorpusException;
	
	public abstract List<WordWithMorpheme> wordsContainingMorpheme(String morpheme) throws CompiledCorpusException;

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
			
	public String[] decomposeWord(String word) throws CompiledCorpusException {
		String[] segments;
		try {
			segments = getSegmenter().segment(word);
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
	
	@JsonIgnore
	protected NgramCompiler getCharsNgramCompiler() {
		if (charsNgramCompiler == null) {
			charsNgramCompiler = new NgramCompiler(3, 6,true);
		}
		return charsNgramCompiler;
	}	
	
	@JsonIgnore
	protected NgramCompiler getMorphsNgramCompiler() {
		if (morphsNgramCompiler == null) {
			morphsNgramCompiler = new NgramCompiler(0, 3,true);
		}
		return morphsNgramCompiler;
	}	

	public WordInfo mostFrequentWordExtending(String[] morphemes) 
			throws CompiledCorpusException {
		WordInfo mostFrequent = null;
		WordInfo[] mostFrequentWords = mostFrequentWordsExtending(morphemes, 1);
		if (mostFrequentWords != null && mostFrequentWords.length > 0) {
			mostFrequent = mostFrequentWords[0];
		}
		return mostFrequent;
	}
	
	public long totalWords() throws CompiledCorpusException {
		long total;
		try {
			total = totalWordsWithNoDecomp() + totalWordsWithDecomps();
		} catch (CompiledCorpusException e) {
			throw new CompiledCorpusException(e);
		}
		return total;
	}
	
	protected void updateWordDecompositions(String word, 
			String[][] wordDecomps) throws CompiledCorpusException {
		String[][] sampleDecomps = decompsSample(wordDecomps);
		Integer totalDecomps = null;
		if (wordDecomps != null) {
			totalDecomps = wordDecomps.length;
		}
		addWordOccurence(word, sampleDecomps, totalDecomps, 0);
	}
	
	protected String[][] decompsSample(String[][] allDecomps) {
		String[][] sample = null;
		if (allDecomps != null) {
			int sampleSize = Math.min(decompsSampleSize, allDecomps.length);
			sample = Arrays.copyOfRange(allDecomps, 0, sampleSize);
		}
		return sample;
	}
}
