package ca.pirurvik.iutools.webservice.gist;

import ca.inuktitutcomputing.morph.Gist;
import ca.inuktitutcomputing.utilities.Alignment;
import ca.pirurvik.iutools.webservice.ServiceResponse;

public class GistWordResponse extends ServiceResponse {
	
	public String word = null;
	public Gist wordGist = null;
	public Alignment[] alignments = null;
	
	public GistWordResponse() {
		init_GistWordResponse(null);
	}

	public GistWordResponse(String _word) {
		init_GistWordResponse(_word);
	}

	private void init_GistWordResponse(String _word) {
		this.word = _word;
	}
}
