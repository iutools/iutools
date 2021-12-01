package org.iutools.worddict;

import java.util.HashMap;
import java.util.Map;

public class GlossaryEntry {
	public Map<String,String> lang2term = new HashMap<String,String>();

	public GlossaryEntry() {}

	public GlossaryEntry setTermInLang(String lang, String term) {
		lang2term.put(lang, term);
		return this;
	}

	public String getTermInLang(String lang) {
		return lang2term.get(lang);
	}
}
