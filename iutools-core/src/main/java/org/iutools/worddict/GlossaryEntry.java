package org.iutools.worddict;

import org.iutools.script.TransCoder;
import org.iutools.script.TransCoderException;

import java.util.*;

public class GlossaryEntry {
	public Map<String, List<String>> lang2term = new HashMap<String,List<String>>();

	public String source = null;
	public String reference = null;
	Set<String> _availableLanguages = null;

	public GlossaryEntry() {}

	public GlossaryEntry setTermsInLang(String lang, List<String> terms) throws GlossaryException {
		_availableLanguages = null;
		lang2term.put(lang, terms);

		if (lang.startsWith("iu")) {
			Collection<String> termsSyll = null;
			Collection<String> termsRoman = null;
			try {
				termsSyll = TransCoder.ensureSyllabic((Collection<String>)terms);
				termsRoman = TransCoder.ensureRoman(terms);
			} catch (TransCoderException e) {
				throw new GlossaryException(e);
			}
			lang2term.put("iu", (List<String>)termsRoman);
			lang2term.put("iu_roman", (List<String>)termsRoman);
			lang2term.put("iu_syll", (List<String>)termsSyll);
		}
		return this;
	}

	public GlossaryEntry setTermInLang(String lang, String term) throws GlossaryException {
		_availableLanguages = null;
		List<String> terms = new ArrayList<String>();
		terms.add(term);
		setTermsInLang(lang, terms);
		return this;
	}

	public List<String> termsInLang(String lang) {
		if (lang.equals("iu")) {
			lang = "iu_roman";
		}
		return lang2term.get(lang);
	}

	public Set<String> availableLanguages() {
		if (_availableLanguages == null) {
			_availableLanguages = new HashSet<String>();
			_availableLanguages.addAll(lang2term.keySet());
			if (_availableLanguages.contains("iu_syll") ||
				_availableLanguages.contains("iu_roman")) {
				_availableLanguages.add("iu");
				_availableLanguages.remove("iu_syll");
				_availableLanguages.remove("iu_roman");
			}
		}
		return _availableLanguages;
	}

	public String firstTerm4Lang(String lang) {
		List<String> terms = termsInLang(lang);
		String firstTerm = (terms.isEmpty()?null:terms.get(0));
		return firstTerm;
	}

}
