package ca.pirurvik.iutools.corpus;

import ca.nrc.dtrc.elasticsearch.Document;
import org.apache.commons.lang3.StringUtils;

public class WordInfo extends Document {
	
	/**
	 * The word. May be left to null if we prefer to use numerical
	 * IDs to identify the word.
	 */
	public String word = null;
	
	/**
	 * Internal key for this word. May be left to null if we prefer to use strings
	 * IDs to identify the word.
	 */
	public Long key = null;
	
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

	public long frequency = 0;

	/**
	 * Characters of the word, separated by space.
	 * This allows us to compose an ES query that finds all the
	 * words that contain a particular ngram of characters.
	 */
	public String wordCharsSpaceConcatenated = null;

	/**
	 Concatenation of the morphemes in the top decomposition.
	 This is easier to process with ES than the raw morpheme array form
	 of the top decomposition.
	 */
	public String topDecompositionStr = null;

	/**
	 * Morphemes of the word, separated by space.
	 * This allows us to compose an ES query that finds all the
	 * words that contain a particular ngram of morphemes.
	 */
	public String morphemesSpaceConcatenated = null;


	public WordInfo() {
		init_WordInfo(null);
	}	
	
	public WordInfo(String _word) {
		init_WordInfo(_word);
	}

	private void init_WordInfo(String _word) {
		setId(_word);
	}

	@Override
	public WordInfo setId(String _id) {
		super.setId(_id);
		word = _id;
		return this;
	}

	public String getId() {
		return this.word;
	}


	public void setDecompositions(String[][] sampleDecomps, Integer totalDecomps) {

		if (sampleDecomps == null) {
			totalDecompositions = null;
			decompositionsSample = null;
		} else {			
			decompositionsSample = sampleDecomps;
			totalDecompositions = totalDecomps;
		}

		String[] topDecomp = topDecomposition();
		if (topDecomp != null) {
			topDecompositionStr = StringUtils.join(topDecomp, " ");
		}
	}

	public Boolean decomposesSuccessfully() {
		Boolean answer = null;
		if (decompositionsSample != null) {
			answer = (decompositionsSample.length > 0);
		}
		return answer;
	}

	public WordInfo setFrequency(long _freq) {
		this.frequency = _freq;
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

	public String getWordCharsSpaceConcatenated() {
		if (wordCharsSpaceConcatenated == null && word != null) {
			String[] wordChars = word.split("");
			this.wordCharsSpaceConcatenated =
					"BEGIN " + String.join(" ", wordChars) + " END";
		}
		return wordCharsSpaceConcatenated;
	}

	public String getMorphemesSpaceConcatenated() {
		if (morphemesSpaceConcatenated == null) {
			String[] topDecomp = topDecomposition();
			if (topDecomp != null) {
				int last = topDecomp.length-1;
				if (topDecomp[last].equals("\\\\")) {
					// In old corpora, \\ is used to denote the end of the decomp
					topDecomp[last] = "";
				}
				morphemesSpaceConcatenated =
						"BEGIN " + String.join(" ", topDecomp) + " END";
			}
		}
		return morphemesSpaceConcatenated;
	}

	public static String insertSpaces(String ngram) {
		String[] ngramArr = ngram.split("");
		return insertSpaces(ngramArr);
	}

	public static String insertSpaces(String[] ngramArr) {
		String ngramWithSpaces = StringUtils.join(ngramArr, " ");
		return ngramWithSpaces;
	}

	@Override
	public boolean equals(Object other) {
		boolean answer = false;
		if (other instanceof WordInfo) {
			String otherWord = ((WordInfo) other).word;
			answer = (this.word.equals(otherWord));
		}
		return answer;
	}

	@Override
	public int hashCode() {
		int code = this.word.hashCode();
		return code;
	}
}
