package ca.pirurvik.iutools.webservice;

import java.util.List;


class MorphemeSearchResult {
	public String  meaning;
	public List<String> words;
	public List<Long> wordFrequencies;
	
	public MorphemeSearchResult() {
	};
	
	public MorphemeSearchResult(String _meaning, List<String> _words, List<Long> _wordFrequencies) {
		this.meaning = _meaning;
		this.words = _words;
		this.wordFrequencies = _wordFrequencies;
	}
}

