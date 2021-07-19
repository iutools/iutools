package org.iutools.webservice.gist;

import org.iutools.concordancer.SentencePair;
import org.iutools.morph.Gist;
import org.iutools.script.TransCoder;
import org.iutools.webservice.EndpointResult;
import org.json.JSONObject;

public class GistWordResult extends EndpointResult {
	public String inputWord = null;
	public String wordRomanized = null;
	public Gist wordGist = null;
	public SentencePair[] alignments = null;

	public GistWordResult() {
		init_GistWord2Result(null);
	}

	@Override
	public JSONObject resultLogEntry() {
		return null;
	}

	public GistWordResult(String _word) {
		init_GistWord2Result(_word);
	}

	private void init_GistWord2Result(String _word) {
		this.inputWord = _word;
		if (_word != null) {
			this.wordRomanized = TransCoder.ensureRoman(_word);
		}
	}
}
