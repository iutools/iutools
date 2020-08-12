package ca.pirurvik.iutools.corpus;

import java.util.List;

public class WordInfo {
	
	/**
	 * The word. May be left to null if we prefer to use numerical
	 * IDs to identify the word.
	 */
	public String word = null;
	
	/** 
	 * Internal key for this word. May be left to null if we prefer to use strings
	 * IDs to identify the word.
	 */
	Long key = null;
	
	/**
	 * The list of top morphological decompositions for this word.
	 * A null value does NOT mean that the word has no decomposition.
	 * It just means that we haven't yet stored them in the WordInfo.
	 * An EMPTY array on the other hand, DOES mean that the morphological 
	 * analyzer is not able to process that word.
	 * 
	 * Note that the morphological analyser might have produced more 
	 * decompositions than are stored in the WordInfo. The total number of
	 * decompositions that were avaible is provided by totalDecompositions.
	 */
	// TODO-June2020: Get rid of this once we don't need the 
	//   CompiledCorpus_InMemory (that class stores the attribute in the
	//   json file)
	public String[] topDecompositions = null;
	
	/** Sample of the top decompositions for the word
	 * 
	 * A null value does NOT mean that the word has no decomposition.
	 * It just means that we haven't yet stored them in the WordInfo.
	 * An EMPTY array on the other hand, DOES mean that the morphological 
	 * analyzer is not able to process that word.
	 * 
	 * Note that the morphological analyser might have produced more 
	 * decompositions than are stored in the WordInfo. The total number of
	 * decompositions that were available is provided by totalDecompositions.
	 */
	public String[][] decompositionsSample = null;
	
	/**
	 * Total number of decompositions that were obtained for this word.
	 * This may be different from the size of decompositionsSample, as the later
	 * only provides the top N decompositions.
	 * 
	 * A null value does NOT mean that the word has no decomposition.
	 * It just means that we haven't yet stored them in the WordInfo.
	 * A value of 0 on the other hand, DOES mean that the morphological 
	 * analyzer is not able to process that word.
	 */
	public Integer totalDecompositions = null;

	public long frequency = 0;;

	public WordInfo() {
		init_WordInfo(null, null);
	}	
	
	public WordInfo(String _word) {
		init_WordInfo(_word, null);
	}

	public WordInfo(Long _key) {
		init_WordInfo(null, _key);
	}

	public WordInfo(String _word, Long _key) {
		init_WordInfo(_word, _key);
	}
	
	private void init_WordInfo(String _word, Long _key) {
		this.word = _word;
		this.key = _key;
	}
	
	public void setDecompositions(String[][] sampleDecomps, int totalDecomps) {
		if (sampleDecomps == null) {
			topDecompositions = null;
			totalDecompositions = null;
			decompositionsSample = null;
		} else {			
			decompositionsSample = sampleDecomps;
			totalDecompositions = totalDecomps;
			if (decompositionsSample.length > 0) {
				topDecompositions = decompositionsSample[0];
			}
		}
	}

	public Boolean decomposesSuccessfully() {
		Boolean answer = null;
		if (topDecompositions != null) {
			answer = (topDecompositions.length > 0);
		}
		return answer;
	}

	public WordInfo setFrequency(long _freq) {
		this.frequency = _freq;
		return this;
	}
	
	public WordInfo setTopDecompositions(String[] _topDecomps) {
		this.topDecompositions = _topDecomps;
		return this;
	}

	public WordInfo setSampleDecompositions(String[][] _sampleDecomps) {
		this.decompositionsSample = _sampleDecomps;
		return this;
	}

	public WordInfo setTotalDecompositions(int _totalDecomps ) {
		this.totalDecompositions = _totalDecomps;
		return this;
	}
	
	public String[] topDecomposition() {
		String[] topDecomp = null;
		if (decompositionsSample != null && decompositionsSample.length > 0) {
			topDecomp = decompositionsSample[0];
		}
		return topDecomp;
	}
}
