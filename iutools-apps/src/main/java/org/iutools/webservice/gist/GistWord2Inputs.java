package org.iutools.webservice.gist;

import org.iutools.script.TransCoder;
import org.iutools.webservice.ServiceInputs;

public class GistWord2Inputs extends ServiceInputs {

	public String word = null;
	private String wordRomanized = null;
		public void setWordRomanized(String wr) { this.wordRomanized = wr; }
		public String getWordRomanized() {
			if (this.wordRomanized == null) {
				this.wordRomanized = TransCoder.ensureRoman(this.word);
			}
			return this.wordRomanized;
		}

	public GistWord2Inputs() {
		init_GistWordInputs(null);
	}

	public GistWord2Inputs(String _word) {
		init_GistWordInputs(_word);
	}

	private void init_GistWordInputs(String _word) {
		this.word = _word;
		if (_word != null) {
			this.wordRomanized = TransCoder.ensureRoman(_word);
		}
	}
}
