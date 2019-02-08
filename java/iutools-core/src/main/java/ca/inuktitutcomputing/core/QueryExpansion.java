package ca.inuktitutcomputing.core;

public class QueryExpansion {
	
	public String word;
	public long frequency; // in the trie-compiled corpus
	public String[] morphemes;
	
	public QueryExpansion(String _word, String[] _morphemes, long _frequency) {
		this.word = _word;
		this.morphemes = _morphemes;
		this.frequency = _frequency;
	}

}
