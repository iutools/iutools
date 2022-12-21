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

	public static Word build(String wordStr) throws WordException {
		Word word = null;
		// See if this can be made into an IU word
		try {
			word = new IUWord(wordStr);
		} catch (WordException e) {
			// This is not an IU word. Create it as a NonIUWord, unless
			// we haven't specified its language
			word = new NonIUWord(wordStr, null);
		}
		return word;
	}

	public String word() {
		return _word;
	}

	public String lang() {
		return _lang;
	}
}
