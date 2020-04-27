package ca.inuktitutcomputing.morph.failureanalysis;

import java.util.HashSet;
import java.util.Set;

import ca.nrc.string.StringUtils;

public class ProblematicNGram {
	
	public static final int MAX_EXAMPLES = 20;
	public static enum SortBy {FS_RATIO, N_FAILURES};
	
	public String ngram = null;
	public long numFailures = 0;
	public long numSuccesses = 0;
	public Set<String> failureExamples = new HashSet<String>();
	public Set<String> successExamples = new HashSet<String>();
	private Double failureSuccessRatio = null;
	
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
	
	public Long getNumFailures() {
		return numFailures;
	}
	
	public void setFailureSuccessRatio(Double ratio) {
		this.failureSuccessRatio = ratio;
	}	
	
	public Double getFailSucceedRatio() {
		return this.failureSuccessRatio;
	}	

	public String toCSV() {
		String csv = ngram+","+getFailSucceedRatio()+","+getNumFailures()+","+
			StringUtils.join(failureExamples.iterator(), "; ")+","+
			StringUtils.join(successExamples.iterator(), "; ")
			;
				
		return csv;
	}

	public static String csvHeaders() {
		String headers = 
			"ngram,fs_ratio,n_failures,failure_examples,success_examples";
		return headers;
	}

}
