package org.iutools.webservice.gist;

import org.iutools.morph.Gist;
import org.iutools.script.TransCoder;
import org.iutools.utilities.Alignment;
import org.iutools.webservice.EndpointResult;

public class GistWord2Result extends EndpointResult {
	public String inputWord = null;
	public String wordRomanized = null;
	public Gist wordGist = null;
	public Alignment[] alignments = null;

	public GistWord2Result() {
		init_GistWord2Result(null);
	}

	public GistWord2Result(String _word) {
		init_GistWord2Result(_word);
	}

	private void init_GistWord2Result(String _word) {
		this.inputWord = _word;
		if (_word != null) {
			this.wordRomanized = TransCoder.ensureRoman(_word);
		}
	}
}
