package ca.inuktitutcomputing.morph;

/**
 * Case for testing the morphological analyzer
 * 
 * @author desilets
 *
 */
public class AnalyzerCase {
	public String word;
	public String correctDecomp = null;
	public boolean isMisspelled = false;
	public boolean skipped = false;
	public boolean decompUnknown = false;
	public boolean properName = false;
	public String comment = null;
	
	public AnalyzerCase(String _word, String _correctDecomp) throws Exception {
		initAnalyzerCase(_word, _correctDecomp);
	}
	
	private void initAnalyzerCase(String _word, String _correctDecomp) 
			throws Exception {
		word = _word;
		correctDecomp = _correctDecomp;
	}
	
	private void throwIncompatibleID(String _word, String prefix, String id, 
			String _correctAnalysis) throws Exception {
		throw new Exception(
			"Case ID for word "+_word+" is incompatible with the expected decomposition\n"+
			"Cases whose ID start with "+prefix+" cannot have an expected decommposition.\n"+
			"Yet, this case was assigned exepcted decomposition: "+_correctAnalysis);
	}

	public AnalyzerCase isMisspelled() {
		isMisspelled = true;
		return this;
	}

	public AnalyzerCase skip() {
		skipped = true;
		return this;
	}
	
	public AnalyzerCase correctDecompUnknown() {
		decompUnknown = true;
		return this;
	}
	
	public AnalyzerCase isProperName() {
		properName = true;
		return this;
	}

	public AnalyzerCase comment(String _comment) {
		this.comment = _comment;
		return this;
	}
}
