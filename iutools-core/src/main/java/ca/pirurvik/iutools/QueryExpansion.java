package ca.pirurvik.iutools;

import java.util.Arrays;

import ca.nrc.datastructure.trie.TrieNode;

public class QueryExpansion {
	
	public String word;
	public long frequency; // in the trie-compiled corpus
	public String[] morphemes;
	
	public QueryExpansion(String _word, String[] _morphemes, long _frequency) {
		this.word = _word;
		this.morphemes = _morphemes;
		this.frequency = _frequency;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(word);
		builder.append("::");
		builder.append(frequency);
		builder.append("::");
		for (String morph: morphemes) {
			builder.append(morph);
		}
		builder.append("]");
		
		
		return builder.toString();
	}
}
