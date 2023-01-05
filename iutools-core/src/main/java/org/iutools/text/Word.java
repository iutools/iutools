package org.iutools.text;

import org.iutools.script.Syllabics;

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
		if (_lang != null && !_lang.equals("iu")) {
			// Make sure the word does not contain any syllabic characters
			if (Syllabics.containsInuktitut(_word)) {
				throw new WordException("Word \""+_word+"\" is not supposed to be in IU but it contains syllabic characters");
			}
		}
	}

	public static Word build(String wordStr) throws WordException {
		return build(wordStr, (String)null);
	}

	public static Word build(String wordStr, String lang) throws WordException {
		Word word = null;

		if (lang != null) {
			if (lang.equals("iu")) {
				word = new IUWord(wordStr);
			} else {
				word = new NonIUWord(wordStr, lang);
			}
		}

		if (word == null) {
			// See if this can be made into an IU word
			try {
				word = new IUWord(wordStr);
			} catch (WordException e) {
				// This is not an IU word. Create it as a NonIUWord, unless
				// we haven't specified its language
				word = new NonIUWord(wordStr, null);
			}
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
