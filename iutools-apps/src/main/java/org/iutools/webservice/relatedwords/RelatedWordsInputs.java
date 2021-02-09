package org.iutools.webservice.relatedwords;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.iutools.webservice.ServiceInputs;

public class RelatedWordsInputs extends ServiceInputs {

	public String word = null;
	public String useCorpus = null;

	public RelatedWordsInputs() {
		init_RelatedWordsInputs((String)null, (String)null);
	}

	public RelatedWordsInputs(String _word) {
		init_RelatedWordsInputs(_word, (String)null);
	}

	public RelatedWordsInputs(String _word, String _useCorpus) {
		init_RelatedWordsInputs(_word, _useCorpus);
	}

	private void init_RelatedWordsInputs(String _word, String _useCorpus) {
		this.word = _word;
		this.useCorpus = _useCorpus;
	}
}
