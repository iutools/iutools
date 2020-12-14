package org.iutools.webservice.gist;

import ca.inuktitutcomputing.morph.Gist;
import ca.inuktitutcomputing.script.TransCoder;
import ca.inuktitutcomputing.utilities.Alignment;
import org.iutools.webservice.ServiceResponse;

public class GistWordResponse extends ServiceResponse {
	
	public String inputWord = null;
	public String wordRomanized = null;
	public Gist wordGist = null;
	public Alignment[] alignments = null;
	
	public GistWordResponse() {
		init_GistWordResponse(null);
	}

	public GistWordResponse(String _word) {
		init_GistWordResponse(_word);
	}

	private void init_GistWordResponse(String _word) {
		this.inputWord = _word;
		if (_word != null) {
			this.wordRomanized = TransCoder.ensureRoman(_word);
		}
	}
}
