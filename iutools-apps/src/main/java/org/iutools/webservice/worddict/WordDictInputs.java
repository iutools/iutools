package org.iutools.webservice.worddict;

import org.iutools.webservice.ServiceException;
import org.iutools.webservice.ServiceInputs;

import java.util.Map;

public class WordDictInputs extends ServiceInputs {
	public String word = null;
	public String lang = "iu";
	/** If false, means we are looking for all words that math the query word.
	 * Otherwise, we just want the word that corresponds exactly to the query word.
	 */
	boolean exactWordLookup = true;

	public WordDictInputs() throws ServiceException {
		init_WordDictInputs((String)null, (String)null);
	}

	public WordDictInputs(String _word, String _lang) throws ServiceException {
		init_WordDictInputs(_word, _lang);
	}

	public WordDictInputs(String _word) throws ServiceException {
		init_WordDictInputs(_word, (String)null);
	}

	private void init_WordDictInputs(String _word, String _lang) throws ServiceException {
		if (_word != null) {
			this.word = _word.toLowerCase();
		}
		if (_lang != null) {
			this.lang = _lang;
		}
		validate();
	}

	@Override
	public Map<String, Object> summarizeForLogging() throws ServiceException {
		return asMap();
	}

	public WordDictInputs setExactWordLookup(boolean exact) {
		exactWordLookup = exact;
		return this;
	}
}
