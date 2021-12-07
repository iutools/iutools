package org.iutools.worddict;

import org.iutools.script.TransCoder;

import java.util.HashMap;
import java.util.Map;

public class GlossaryEntry {
	public Map<String,String> lang2term = new HashMap<String,String>();

	public GlossaryEntry() {}

	public GlossaryEntry setTermInLang(String lang, String term) {
		lang2term.put(lang, term);
		if (lang.equals("iu_syll")) {
			lang2term.put("iu_roman", TransCoder.ensureRoman(term));
		} else if (lang.equals("iu_roman")) {
			lang2term.put("iu_syll", TransCoder.ensureSyllabic(term));
		}
		return this;
	}

	public String getTermInLang(String lang) {
		return lang2term.get(lang);
	}
}
