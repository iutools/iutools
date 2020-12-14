package org.iutools.webservice;

public class OccurenceSearchInputs extends ServiceInputs {
	
	public String wordPattern = null;
	public String corpusName = null;
	public String nbExamples = null;
//	public String exampleWord = null;
	
	public OccurenceSearchInputs() {};
	
	public OccurenceSearchInputs(String _wordPattern, String _corpusName, String _nbExamples) {
		this.wordPattern = _wordPattern;
		this.corpusName = _corpusName;
		this.nbExamples = _nbExamples;
	}
	
}
