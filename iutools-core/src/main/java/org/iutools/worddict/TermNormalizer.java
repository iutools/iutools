package org.iutools.worddict;

import org.iutools.script.TransCoder;
import org.iutools.script.TransCoderException;

import static org.iutools.script.TransCoder.Script;

import java.util.*;

/**
 * Normalize a term.
 *   1. If it's in IU, make sure it is in the same script as the entry's input term
 *   2. If it's in EN, lowercase it
 *
 * Typically used when you want to use the term as a key in a Map.
 */
public class TermNormalizer {

	/**
	 * Remembers the normalization of terms so we don't have to do it over and
	 * over again.
	 */
	public Map<String, Map<String,String>> normalizedTermsCache = new HashMap<String,Map<String,String>>();

	public TermNormalizer() {
		init__TermNormalizer();
	}

	private void init__TermNormalizer() {
		for (String lang: new String[] {"en", "iu"}) {
			normalizedTermsCache.put(lang, new HashMap<String,String>());
		}
	}

	public String normalize(String term, String termLang) throws TermNormalizerException {
		return normalize(term, termLang, (Script)null);
	}

	public String normalize(String term, String termLang, Script normalizationScript) throws TermNormalizerException {
		if (termLang.startsWith("iu")) {
			termLang = "iu";
		}
		if (normalizationScript == null && termLang.equals("iu")) {
			normalizationScript = Script.ROMAN;
		}
		String normalized = term;
		if (term != null) {
			if (normalizedTermsCache.get(termLang).containsKey(term)) {
				normalized = normalizedTermsCache.get(termLang).get(term);
			} else {
				if (termLang.equals("iu")) {
					try {
						normalized = TransCoder.ensureScript(normalizationScript, term);
					} catch (TransCoderException e) {
						throw new TermNormalizerException(e);
					}
				} else {
					normalized = term.toLowerCase();
				}
				normalizedTermsCache.get(termLang).put(term, normalized);
			}
		}
		return normalized;
	}

	public List<String> normalizeTerms(String[] terms, String termsLang) throws TermNormalizerException {
		return normalizeTerms(terms, termsLang, (Script)null);
	}

	public List<String> normalizeTerms(String[] terms, String termsLang, Script normalizationScript) throws TermNormalizerException {
		List<String> termsList = new ArrayList<String>();
		Collections.addAll(termsList, terms);
		return normalizeTerms(termsList, termsLang, normalizationScript);
	}

	public List<String> normalizeTerms(List<String> terms, String termsLang) throws TermNormalizerException {
		return normalizeTerms(terms, termsLang, (Script)null);
	}

	public List<String> normalizeTerms(List<String> terms, String termsLang, Script normalizationScript) throws TermNormalizerException {
		List<String> normalized = new ArrayList<String>();
		for (String aTerm: terms) {
			normalized.add(normalize(aTerm, termsLang, normalizationScript));
		}
		return normalized;
	}

	public Map<String, Map<String,String>> normalizedTerms() {
		return normalizedTermsCache;
	}
}