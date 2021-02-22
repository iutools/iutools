package org.iutools.morph;

import java.util.HashMap;
import java.util.Map;

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
 * - CORRECT_NOT_FIRST: The analyzer does include the correct 
 *     decomposition in the list, but it is not the first one.
 *     
 * - CORRECT_NOT_PRESENT: The analyzer does provide some analyses, but none of 
 *     them is the correct one.
 * 
 * - NO_DECOMPS: The analyzer completes without timeout but it 
 *     produces no decompositions at all
 *
 * - TIMEOUT: The analyzer times out before it can complete.
 * 
 *     
 */
public abstract class MorphAnalCurrentExpectationsAbstract {

	abstract void initMorphAnalCurrentExpectations() throws MorphologicalAnalyzerException;
	// IMPORTANT: The order of those matters. They are from 
	//   least successful to most successful
	//
	public static enum OutcomeType {
		NO_DECOMPS, CORRECT_NOT_PRESENT, CORRECT_NOT_FIRST, SUCCESS};
	
	
	Map<String,OutcomeType> expFailures = 
		new HashMap<String,OutcomeType>();
	
	public MorphAnalCurrentExpectationsAbstract() throws MorphologicalAnalyzerException {
		initMorphAnalCurrentExpectations();
	}

	public void expectFailure(String word, OutcomeType type) throws MorphologicalAnalyzerException {
		if (type == OutcomeType.SUCCESS) {
			throw new MorphologicalAnalyzerException(
				"Outcome "+OutcomeType.SUCCESS+" is not a type of failure");
		}
		expFailures.put(word, type);
	}

	public OutcomeType type4outcome(AnalysisOutcome outcome, 
			String correctDecomp) {
		
		OutcomeType type = null;
		
		if (outcome.decompositions == null || 
			outcome.decompositions.length == 0) {
			type = OutcomeType.NO_DECOMPS;
		}
		
		if (type == null) {
			// The Decomp produces some decompositions. 
			// What is the position of the correct one in that list?
			//
			Integer rank = outcome.decompRank(correctDecomp);
			if (rank == null) {
				type = OutcomeType.CORRECT_NOT_PRESENT;
			} else if (rank > 0) {
				type = OutcomeType.CORRECT_NOT_FIRST;
			} else {
				type = OutcomeType.SUCCESS;
			}
		}
		
		if (type == null) {
			type = OutcomeType.SUCCESS;
		}
		
		return type;
	}
	
	public OutcomeType expectedOutcome(String word) {
		OutcomeType outcome = OutcomeType.SUCCESS;
		if (expFailures.containsKey(word)) {
			outcome = expFailures.get(word);
		}
		return outcome;
	}
	
}
