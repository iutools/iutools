package org.iutools.webservice;

import org.iutools.linguisticdata.Morpheme;

import java.util.List;


public class MorphemeSearchResult {
	public String morphID;
	public String morphDescr;
	public String  meaning;
	public List<String> words;
	public List<Double> wordScores;
	
	public MorphemeSearchResult() {
	};
	
	public MorphemeSearchResult(String _morphID, String _meaning, List<String> _words, List<Double> _wordScores) {
		this.morphID = _morphID;
		this.morphDescr = Morpheme.description4id(morphID);
		this.meaning = _meaning;
		this.words = _words;
		this.wordScores = _wordScores;
	}
}

