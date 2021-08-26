package org.iutools.webservice.worddict;


import org.iutools.webservice.EndpointResult;
import org.iutools.worddict.IUWordDictEntry;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WordDictResult extends EndpointResult {

	public List<String> matchingWords = new ArrayList<String>();

	// If the word pattern matched one word exactly, then this contains the
	// dictionary entry for that single word
	public IUWordDictEntry queryWordEntry;
	public Long totalWords = new Long(0);

	public WordDictResult() {
		init_WordDictResult(
			(IUWordDictEntry)null, (List<String>)null, (Long)null);
	}

	public WordDictResult(IUWordDictEntry _entry) {
		init_WordDictResult(_entry, (List<String>)null, (Long)null);
	}

	public WordDictResult(IUWordDictEntry _entry, List<String> _foundWords) {
		init_WordDictResult(_entry, _foundWords, (Long)null);
	}

	public WordDictResult(
		IUWordDictEntry _entry, List<String> _foundWords, Long _totalWords) {
		init_WordDictResult(_entry, _foundWords, _totalWords);
	}


	private void init_WordDictResult(
		IUWordDictEntry _qWordEntry, List<String> _foundWords, Long _totalWords) {
		this.matchingWords = _foundWords;
		this.queryWordEntry = _qWordEntry;
		this.totalWords = _totalWords;
	}

	@Override
	public JSONObject resultLogEntry() {
		return null;
	}
}
