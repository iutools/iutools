package org.iutools.webservice.worddict;


import org.iutools.webservice.EndpointResult;
import org.iutools.worddict.MultilingualDictEntry;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WordDictResult extends EndpointResult {

	public List<String> matchingWords = new ArrayList<String>();

	// If the word pattern matched one word exactly, then this contains the
	// dictionary entry for that single word
	public MultilingualDictEntry queryWordEntry;
	public Long totalWords = new Long(0);
	public String lang = null;
	public String otherLang = null;

	public WordDictResult() {
		init_WordDictResult(
			(MultilingualDictEntry)null, (List<String>)null, (Long)null);
	}

	public WordDictResult(MultilingualDictEntry _entry) {
		init_WordDictResult(_entry, (List<String>)null, (Long)null);
	}

	public WordDictResult(MultilingualDictEntry _entry, List<String> _foundWords) {
		init_WordDictResult(_entry, _foundWords, (Long)null);
	}

	public WordDictResult(
	MultilingualDictEntry _entry, List<String> _foundWords, Long _totalWords) {
		init_WordDictResult(_entry, _foundWords, _totalWords);
	}


	private void init_WordDictResult(
	MultilingualDictEntry _qWordEntry, List<String> _foundWords, Long _totalWords) {
		this.matchingWords = _foundWords;
		this.queryWordEntry = _qWordEntry;
		this.totalWords = _totalWords;
	}

	@Override
	public JSONObject resultLogEntry() {
		return null;
	}

	public WordDictResult setLang(String _lang) {
		this.lang = _lang;
		return this;
	}

	public WordDictResult setOtherLang(String _lang) {
		this.otherLang = _lang;
		return this;
	}

}
