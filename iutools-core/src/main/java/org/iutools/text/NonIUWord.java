package org.iutools.text;

public class NonIUWord extends Word {

	String _lang = null;
	public NonIUWord(String __word, String __lang) throws WordException {
		super(__word, __lang);
	}

	public boolean isInLang(String lang) {
		return _lang != null && lang.equals(_lang);
	}
}
