package org.iutools.webservice.worddict;

import org.iutools.webservice.ServiceInputs;

public class WordDictInputs extends ServiceInputs {
	public String word = null;
	public boolean wordIsEnglish = false;

	public WordDictInputs() {
		init_WordDictInputs((String)null, (Boolean)null);
	}

	public WordDictInputs(String _word, Boolean _wordIsEnglish) {
		init_WordDictInputs(_word, _wordIsEnglish);
	}

	public WordDictInputs(String _word) {
		init_WordDictInputs(_word, (Boolean)null);
	}

	private void init_WordDictInputs(String _word, Boolean _wordIsEnglish) {
		this.word = _word;
		if (_wordIsEnglish != null) {
			wordIsEnglish = _wordIsEnglish;
		}
	}
}
