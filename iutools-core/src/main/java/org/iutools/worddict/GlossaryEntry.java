package org.iutools.worddict;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.iutools.script.TransCoder;
import org.iutools.script.TransCoderException;

import java.util.*;

public class GlossaryEntry {
	private Map<String, List<String>> _lang2term = new HashMap<String,List<String>>();

	public String source = null;
	public String reference = null;
	Set<String> _availableLanguages = null;
	TermNormalizer normalizer = new TermNormalizer();

	public GlossaryEntry() throws GlossaryException {
		init__GlossaryEntry();
	}

	private void init__GlossaryEntry() throws GlossaryException {
	}

	public void setLang2term(Map<String, List<String>> map) throws GlossaryException {
		this._lang2term = map;
		for (String lang: _lang2term.keySet()) {
			List<String> termsInLang = _lang2term.get(lang);
			try {
				termsInLang = normalizer.normalizeTerms(termsInLang, lang, TransCoder.Script.ROMAN);
			} catch (TermNormalizerException e) {
				throw new GlossaryException(e);
			}
		}
	}

	public GlossaryEntry setTermsInLang(String lang, List<String> terms) throws GlossaryException {
		_availableLanguages = null;
		terms = normalizeTerms(lang, terms);
		_lang2term.put(lang, terms);

		if (lang.startsWith("iu")) {
			Collection<String> termsSyll = null;
			Collection<String> termsRoman = null;
			try {
				termsSyll = TransCoder.ensureSyllabic((Collection<String>)terms);
				termsRoman = TransCoder.ensureRoman(terms);
			} catch (TransCoderException e) {
				throw new GlossaryException(e);
			}
			_lang2term.put("iu", (List<String>)termsRoman);
			_lang2term.put("iu_roman", (List<String>)termsRoman);
			_lang2term.put("iu_syll", (List<String>)termsSyll);
		}
		return this;
	}

	private List<String> normalizeTerms(String lang, List<String> terms) throws GlossaryException {
		List<String> normalized = new ArrayList<String>();
		try {
			MDictEntry mdEntry = new MDictEntry();
			for (String term: terms) {
				normalized.add(mdEntry.normalizeTerm(term, lang));
			}
		} catch (MachineGeneratedDictException e) {
			throw new GlossaryException(e);
		}
		return normalized;
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
		return _lang2term.get(lang);
	}

	public Set<String> availableLanguages() {
		if (_availableLanguages == null) {
			_availableLanguages = new HashSet<String>();
			_availableLanguages.addAll(_lang2term.keySet());
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

	@Override
	public String toString() {
		String toS = null;
		try {
			toS = new ObjectMapper().writeValueAsString(this);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}

		return toS;
	}
}
