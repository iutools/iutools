package org.iutools.webservice.worddict;

import org.iutools.webservice.ServiceInputs;

public class WordDictInputs extends ServiceInputs {
	public String word = null;

	public WordDictInputs() {
		init_WordDictInputs(null);
	}

	public WordDictInputs(String _word) {
		init_WordDictInputs(_word);
	}

	private void init_WordDictInputs(String _word) {
		this.word = _word;
	}

}
