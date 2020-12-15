package org.iutools.webservice;

import org.iutools.morph.Gist;
import ca.inuktitutcomputing.utilities.Alignment;


class ExampleWordWithMorpheme {
	public String  word;
	public Gist gist;
	public Alignment[] alignments;
	
	public ExampleWordWithMorpheme() {
	};
	
	public ExampleWordWithMorpheme(String _word, Gist _gist, Alignment[] _alignments) {
		this.word = _word;
		this.gist = _gist;
		this.alignments = _alignments;
	}
}

