package ca.pirurvik.iutools.corpus;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ca.inuktitutcomputing.data.LinguisticDataException;
import ca.inuktitutcomputing.utilities.StopWatch;
import ca.inuktitutcomputing.utilities.StopWatchException;
import ca.nrc.datastructure.trie.StringSegmenter;
import ca.nrc.datastructure.trie.StringSegmenterException;
import ca.nrc.datastructure.trie.StringSegmenter_Char;
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
	
	
	public abstract String[] bestDecomposition(String word) throws CompiledCorpusException;
	
	public abstract WordInfo[] mostFrequentWordsExtending(
			String[] morphemes, Integer N) throws CompiledCorpusException;

	protected abstract void updateDecompositionsIndex(
			String word,String[][] sampleDecomps, int totalDecomps) 
		throws CompiledCorpusException;
	
	protected abstract void updateWordIndex(
		String word, String[][] sampleDecomps, int totalDecomps) throws CompiledCorpusException;
	
	protected abstract void updateCharNgramIndex(
		String word, String[][] sampleDecomps, int totalDecomps) throws CompiledCorpusException;
	
	protected String segmenterClassName = StringSegmenter_Char.class.getName();
	protected transient StringSegmenter segmenter = null;
		
	private int decompsSampleSize = Integer.MAX_VALUE;
	
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
		
		String[][] decomps = null;
		
		try {
			decomps = getSegmenter().possibleSegmentations(word);
		} catch (TimeoutException | StringSegmenterException e) {
			throw new CompiledCorpusException(e);
		}
		
		String[][] sampleDecomps = null;
		int totalDecomps = 0;
		if (decomps != null) {
			totalDecomps = decomps.length;
			int numToKeep = Math.min(totalDecomps, decompsSampleSize);
			sampleDecomps = Arrays.copyOfRange(decomps, 0, numToKeep);
		}		
		addWordOccurence(word, sampleDecomps, totalDecomps);
		return;
	}
	
	public void addWordOccurence(String word, String[][] sampleDecomps, 
			int totalDecomps) throws CompiledCorpusException {
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.corpus.CompiledCorpus.addWordOccurence");
		Logger tLogger_STEPS = Logger.getLogger("ca.pirurvik.iutools.corpus.CompiledCorpus.addWordOccurence_STEPS");

		TimeUnit tunit = TimeUnit.MILLISECONDS;
		long methodStart = 0;
		if (tLogger.isTraceEnabled()) {
			try {
				methodStart = StopWatch.now(tunit);
			} catch (StopWatchException e) {
				throw new CompiledCorpusException(e);
			}
			tLogger.trace("Adding word="+word);
		}

		long start = 0;
		if (tLogger_STEPS.isTraceEnabled()) {
			try {
				start = StopWatch.now(tunit);
			} catch (StopWatchException e) {
				throw new CompiledCorpusException(e);
			}
		}
		
		updateWordIndex(word, sampleDecomps, totalDecomps);
		if (tLogger_STEPS.isTraceEnabled()) {
			try {
				tLogger_STEPS.trace("addToWordCharIndex took "+
					StopWatch.elapsedSince(start, tunit)+" "+tunit);
				start = StopWatch.now(tunit);
			} catch (StopWatchException e) {
				throw new CompiledCorpusException(e);
			};
		}
		
		updateDecompositionsIndex(word, sampleDecomps, totalDecomps);
		if (tLogger_STEPS.isTraceEnabled()) {
			try {
				tLogger_STEPS.trace("addToWordSegmentations took "+
					StopWatch.elapsedSince(start, tunit)+" "+tunit);
				start = StopWatch.now(tunit);
			} catch (StopWatchException e) {
				throw new CompiledCorpusException(e);
			};
		}

		updateCharNgramIndex(word, sampleDecomps, totalDecomps);
		if (tLogger_STEPS.isTraceEnabled()) {
			try {
				tLogger_STEPS.trace("updateCharNgramIndex took "+
					StopWatch.elapsedSince(start, tunit)+" "+tunit);
			} catch (StopWatchException e) {
				throw new CompiledCorpusException(e);
			};
		}
		
		if (tLogger.isTraceEnabled()) {
			try {
				tLogger.trace("addWordOccurence took "+
					StopWatch.elapsedSince(methodStart, tunit)+" "+tunit+"\n");
			} catch (StopWatchException e) {
				throw new CompiledCorpusException(e);
			};			
		}
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
			charsNgramCompiler = new NgramCompiler(3,true);
		}
		return charsNgramCompiler;
	}	
	
	@JsonIgnore
	protected NgramCompiler getMorphsNgramCompiler() {
		if (morphsNgramCompiler == null) {
			morphsNgramCompiler = new NgramCompiler(0,true);
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
}
