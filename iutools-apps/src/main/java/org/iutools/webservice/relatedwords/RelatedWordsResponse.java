package org.iutools.webservice.relatedwords;

import org.iutools.morphrelatives.MorphologicalRelative;
import org.iutools.webservice.ServiceResponse;

public class RelatedWordsResponse extends ServiceResponse  {

	public String inputWord = null;
	public MorphologicalRelative[] relatedWords = new MorphologicalRelative[0];

	public RelatedWordsResponse() {}

	public RelatedWordsResponse(
		String _inputWord, MorphologicalRelative[] _relatedWords) {
		this.inputWord = _inputWord;
		this.relatedWords = _relatedWords;
	}

}
