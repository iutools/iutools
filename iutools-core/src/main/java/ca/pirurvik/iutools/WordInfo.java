package ca.pirurvik.iutools;

import java.util.Arrays;

import ca.inuktitutcomputing.morph.Decomposition;

public class WordInfo {
	
	/** 
	 * Internal key for this word.
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
	String[] topDecompositions = null;
	
	/**
	 * Total number of decompositions that were obtained for this word.
	 * This may be different from the size of topDecompositions, as the later
	 * only provides the top few decompositions.
	 * 
	 * A null value does NOT mean that the word has no decomposition.
	 * It just means that we haven't yet stored them in the WordInfo.
	 * A value of 0 on the other hand, DOES mean that the morphological 
	 * analyzer is not able to process that word.
	 */
	Integer totalDecompositions = null;

	public int frequency = 0;;

	public WordInfo(Long _key) {
		this.key = _key;
	}

	public void setDecompositions(String[] decomps) {
		if (decomps == null) {
			topDecompositions = null;
			totalDecompositions = null;
		} else {
			topDecompositions = Arrays.copyOfRange(decomps, 0, 5);
			totalDecompositions = decomps.length;
		}
	}
}
