package org.iutools.morph;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Provides gold standard answers that the morphological 
 * analyzer should return for a series of words.
 * 
 * @author desilets
 *
 */
public abstract class MorphAnalGoldStandardAbstract {

	abstract void initCases() throws Exception;

	Map<String,AnalyzerCase> case4word = 
			new HashMap<String,AnalyzerCase>();
	
	public MorphAnalGoldStandardAbstract() throws Exception {
		initCases();
	}
	
	public void addCase(AnalyzerCase caseData) {
		case4word.put(caseData.word, caseData);
	}
	
	public Set<String> allWords() {
		return case4word.keySet();
	}

	public String[] correctDecomps(String word) {
		AnalyzerCase anlCase = case4word.get(word);
		return anlCase.correctDecomps;
	}
		
	public AnalyzerCase caseData(String word) {
		return case4word.get(word);
	}
	
}
