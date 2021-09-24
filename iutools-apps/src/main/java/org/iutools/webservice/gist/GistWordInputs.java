package org.iutools.webservice.gist;

import org.iutools.script.TransCoder;
import org.iutools.webservice.ServiceException;
import org.iutools.webservice.ServiceInputs;

import java.util.Map;

public class GistWordInputs extends ServiceInputs {

	public String word = null;
	private String wordRomanized = null;
		public void setWordRomanized(String wr) { this.wordRomanized = wr; }
		public String getWordRomanized() {
			if (this.wordRomanized == null) {
				this.wordRomanized = TransCoder.ensureRoman(this.word);
			}
			return this.wordRomanized;
		}

	public GistWordInputs() throws ServiceException {
		init_GistWordInputs(null);
	}

	public GistWordInputs(String _word) throws ServiceException {
		init_GistWordInputs(_word);
	}

	private void init_GistWordInputs(String _word) throws ServiceException {
		this.word = _word;
		if (_word != null) {
			this.wordRomanized = TransCoder.ensureRoman(_word);
		}
		validate();
	}

	@Override
	public Map<String, Object> summarizeForLogging() throws ServiceException {
		return asMap();
	}
}
