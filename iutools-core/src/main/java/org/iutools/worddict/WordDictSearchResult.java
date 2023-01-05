package org.iutools.worddict;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of a search for a word in a WordDict.
 */
public class WordDictSearchResult {
	public String origQuery = null;
	public String lang = null;
	public String spellCheckedQuery = null;
	public List<String> hits = new ArrayList<String>();
	public long totalWords = 0;

	public WordDictSearchResult(String _partialWord, String _lang) {
		this.origQuery = _partialWord;
		this.lang = _lang;
	}
}
