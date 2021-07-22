package org.iutools.webservice.worddict;


import org.iutools.webservice.EndpointResult;
import org.iutools.worddict.IUWordDictEntry;
import org.json.JSONObject;

public class WordDictResult extends EndpointResult {

	public IUWordDictEntry entry;

	public WordDictResult() {
		init_WordDictResult((IUWordDictEntry)null);
	}

	public WordDictResult(IUWordDictEntry _entry) {
		init_WordDictResult(_entry);
	}

	private void init_WordDictResult(IUWordDictEntry _entry) {
		this.entry = _entry;
	}

	@Override
	public JSONObject resultLogEntry() {
		return null;
	}
}
