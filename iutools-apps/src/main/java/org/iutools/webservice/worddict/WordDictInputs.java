package org.iutools.webservice.worddict;

import org.iutools.webservice.ServiceInputs;

public class WordDictInputs extends ServiceInputs {
	public String word = null;
	public String lang = null;

	public WordDictInputs() {
		init_WordDictInputs((String)null, (String)null);
	}

	public WordDictInputs(String _word, String _lang) {
		init_WordDictInputs(_word, _lang);
	}

	public WordDictInputs(String _word) {
		init_WordDictInputs(_word, (String)null);
	}

	private void init_WordDictInputs(String _word, String _lang) {
		this.word = _word;
		this.lang = _lang;
	}
}
