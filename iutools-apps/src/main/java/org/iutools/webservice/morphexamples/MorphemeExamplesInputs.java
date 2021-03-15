package org.iutools.webservice.morphexamples;

import org.iutools.webservice.ServiceInputs;

public class MorphemeExamplesInputs extends ServiceInputs {

	public String wordPattern = null;
	public String corpusName = null;
	public String nbExamples = null;

	public MorphemeExamplesInputs() {};

	public MorphemeExamplesInputs(String _wordPattern, String _corpusName,
		String _nbExamples) {

		this.wordPattern = _wordPattern;
		this.corpusName = _corpusName;
		this.nbExamples = _nbExamples;
	}
}