package ca.inuktitutcomputing.morph;

/**
 * Provides the "current" expectations for the morphological 
 * analyser on the cases provided in the 
 * MorphAnalGoldStandard.
 * 
 * By default, we assume that the top decomposition provided 
 * by the analyzer is the correct one for every word.
 * 
 * We only provide a different expectation for words where the 
 * analyzer "fails" in some sense. 
 * 
 * There are several ways in which the analyzer can fail for a word
 * 
 * - TOP_IS_WRONG: The analyzer does include the correct 
 *     decomposition in the list, but it is not the first one
 * 
 * - TIMEOUT: The analyzer times out before it can complete.
 * 
 * - NO_DECOMPS: The analyzer completes without timeout but it 
 *     produces no decompositions at all
 *     
 */
public class MorphAnalCurrentExpectations {
	
	public static enum FailureType {TOP_IS_WRONG, TIMEOUT, NO_DECOMPS};
	
	
	
	public void addFailure(String word, FailureType type) {
		
	}
}
