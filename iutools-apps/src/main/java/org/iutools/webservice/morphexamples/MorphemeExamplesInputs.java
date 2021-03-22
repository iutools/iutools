package org.iutools.webservice.morphexamples;

import org.iutools.webservice.ServiceInputs;

public class MorphemeExamplesInputs extends ServiceInputs {

	public String wordPattern = null;
	public String corpusName = null;
	public String nbExamples = "20";

	public MorphemeExamplesInputs() {
		init_MorphemeExamplesInputs((String)null, (String)null, (String)null);
	};

	public MorphemeExamplesInputs(String _wordPattern) {
		init_MorphemeExamplesInputs(_wordPattern, (String)null, (String)null);
	}

	public MorphemeExamplesInputs(String _wordPattern, String _corpusName,
		String _nbExamples) {
		init_MorphemeExamplesInputs(_wordPattern, _corpusName, _nbExamples);
	}

	private void init_MorphemeExamplesInputs(String _wordPattern, String _corpusName,
		String _nbExamples) {
		this.wordPattern = _wordPattern;
		this.corpusName = _corpusName;
		if (_nbExamples != null) {
			this.nbExamples = _nbExamples;
		}
	}
}