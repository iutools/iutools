package ca.pirurvik.iutools.webservice.gist;

import ca.inuktitutcomputing.script.TransCoder;
import ca.pirurvik.iutools.webservice.ServiceInputs;

public class GistWordInputs extends ServiceInputs {

	public String word = null;
	public String wordRomanized = null;

	public GistWordInputs() {
		init_GistWordInputs(null);
	}

	public GistWordInputs(String _word) {
		init_GistWordInputs(_word);
	}

	private void init_GistWordInputs(String _word) {
		this.word = _word;
		if (_word != null) {
			this.wordRomanized = TransCoder.ensureRoman(_word);
		}
	}
}
