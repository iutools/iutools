package ca.inuktitutcomputing.morph.failureanalysis;

import java.util.HashSet;
import java.util.Set;

public class ProblematicNGram {
	
	public static final int MAX_EXAMPLES = 20;
	public static enum SortBy {FS_RATIO, N_FAILURES};
	
	public String ngram = null;
	public long numFailures = 0;
	public long numSuccesses = 0;
	public Set<String> failureExamples = new HashSet<String>();
	public Set<String> successExamples = new HashSet<String>();
	
	public ProblematicNGram(String _ngram) {
		initProblematicNGram(_ngram);
	}

	private void initProblematicNGram(String _ngram) {
		this.ngram = _ngram;
	}
	
	public void addFailureExample(String word) {
		if (failureExamples.size() < MAX_EXAMPLES) {
			failureExamples.add(word);
		}
	}

	public void addSuccessExample(String word) {
		if (successExamples.size() < MAX_EXAMPLES) {
			successExamples.add(word);
		}
	}
	

	public Double getFailSucceedRatio() {
		Double ratio = new Double(Double.MAX_VALUE);
		if (numSuccesses > 0) {
			ratio = 1.0 * numFailures / numSuccesses;
		}
		
		return ratio;
	}

	public Long getNumFailures() {
		return numFailures;
	}
}
