package org.iutools.webservice;

import java.util.List;


class MorphemeSearchResult {
	public String  meaning;
	public List<String> words;
	public List<Double> wordScores;
	
	public MorphemeSearchResult() {
	};
	
	public MorphemeSearchResult(String _meaning, List<String> _words, List<Double> _wordScores) {
		this.meaning = _meaning;
		this.words = _words;
		this.wordScores = _wordScores;
	}
}

