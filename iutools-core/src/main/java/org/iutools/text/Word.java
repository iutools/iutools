package org.iutools.text;

/**
 * Class that encapsulates a word in such a way that we know and remember
 * the language of the word (en or iu) and, in the case of an Inuktitut wordm
 * the Script it is written in.
 *
 * Furthermore, for an Inuktitut word, we can get it in either Scripts, without
 * having to constantly transcode it from one script to another.
 */

public class Word {


	protected String _word = null;
	protected String _lang = null;

	public Word(String __word, String __lang) throws WordException {
		this._word = __word;
		this._lang = __lang;
	}

	public String word() {
		return _word;
	}

	public String lang() {
		return _lang;
	}
}
