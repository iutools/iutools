package org.iutools.worddict;

import ca.nrc.string.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.iutools.linguisticdata.LinguisticData;
import org.iutools.linguisticdata.Morpheme;
import org.iutools.linguisticdata.MorphemeException;
import org.iutools.linguisticdata.MorphemeHumanReadableDescr;
import org.iutools.script.TransCoder;
import org.iutools.script.TransCoderException;

import java.util.*;

public class MultilingualDictEntry {

	public String otherLang() throws MultilingualDictException {
		return otherLang(lang);
	}

	public static enum Field {
		DEFINITION, BILINGUAL_EXAMPLES, TRANSLATIONS, DECOMP, RELATED_WORDS
	}

	public String word = null;
	public String lang = "iu";
	public String wordInOtherScript = null;

	public String wordSyllabic;
	public String wordRoman;
	public String definition;
	public List<MorphemeHumanReadableDescr> morphDecomp = new ArrayList<MorphemeHumanReadableDescr>();

	public List<String> origWordTranslations = new ArrayList<String>();
	public List<String> relatedWordTranslations = new ArrayList<String>();

	private boolean _translationsNeedSorting = true;

	// Note: We store sentence pairs as String[] instead of Pair<String,String>
	//   because the latter is jsonified as a dictionary where
	//
	//     - key is the first sentence
	//     - value is the second sentence
	//
	//   and this turns out to be awkward to use on the client-side JavaScript
	//   code.
	public Map<String,List<String[]>> examplesForOrigWordTranslation
		= new HashMap<String,List<String[]>>();
	public Map<String,List<String[]>> examplesForRelWordsTranslation
		= new HashMap<String,List<String[]>>();

	public String[] relatedWords = new String[0];

	public static void assertIsSupportedLanguage(String lang) throws MultilingualDictException {
		if (!lang.matches("^(en|iu)$")) {
			throw new MultilingualDictException("Unsupported language '"+lang+"'");
		}
	}

	public static String otherLang(String lang) throws MultilingualDictException {
		assertIsSupportedLanguage(lang);
		String other = null;
		if (lang.equals("iu")) {
			other = "en";
		} else if (lang.equals("en")) {
			other = "iu";
		}
		return other;

	}


	private List<Pair<String, Double>> _otherLangTranslations = null;

	public List<Pair<String, Double>> otherLangTranslations() {
		if (_otherLangTranslations == null) {
			_otherLangTranslations = new ArrayList<Pair<String, Double>>();
		}
		return _otherLangTranslations;
	}

	public MultilingualDictEntry() throws MultilingualDictException {
		init_IUWordDictEntry((String)null, (String)null);
	}

	public MultilingualDictEntry(String _word) throws MultilingualDictException {
		init_IUWordDictEntry(_word, (String)null);
	}

	public MultilingualDictEntry(String _word, String _lang) throws MultilingualDictException {
		init_IUWordDictEntry(_word, _lang);
	}

	private void init_IUWordDictEntry(String _word, String _lang) throws MultilingualDictException {
		try {
			if (_lang == null) {
				_lang = "iu";
			}
			assertIsSupportedLanguage(lang);
			this.lang = _lang;
			this.word = _word;
			if (lang.equals("iu")) {
				this.wordInOtherScript = TransCoder.inOtherScript(_word);
				this.wordSyllabic =
				TransCoder.ensureScript(TransCoder.Script.SYLLABIC, _word);
			}
		} catch (TransCoderException e) {
			throw new MultilingualDictException(e);
		}
		this.wordRoman =
		TransCoder.ensureRoman(_word);
	}

	public void setDecomp(String[] morphemes) throws MultilingualDictException {
		if (morphemes != null) {
			morphDecomp = new ArrayList<MorphemeHumanReadableDescr>();
			for (String morpheme : morphemes) {
				Morpheme morphInfo = LinguisticData.getInstance().getMorpheme(morpheme);
				if (morphInfo == null) {
					continue;
				}
				try {
					morphDecomp.add(
					new MorphemeHumanReadableDescr(
						morphInfo.id, morphInfo.englishMeaning));
				} catch (MorphemeException e) {
					throw new MultilingualDictException(e);
				}
			}
		}
	}

	public MultilingualDictEntry addBilingualExample(
		String translation, String[] example, boolean forRelatedWord) throws MultilingualDictException {
		Logger tLogger = Logger.getLogger("org.iutools.worddict.MultilingualDictEntry.addBilingualExample");
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("translation=" + translation + ", forRelatedWord=" + forRelatedWord + ", example=" + String.join(", ", example));
		}

		List<String> translations = this.origWordTranslations;
		Map<String, List<String[]>> examplesForTranslation =
			this.examplesForOrigWordTranslation;
		if (forRelatedWord) {
			translations = this.relatedWordTranslations;
			examplesForTranslation = this.examplesForRelWordsTranslation;
		}

		if (!translation.matches("^(ALL|MISC)$") &&
			!translations.contains(translation)) {
			translations.add(translation);
		}
		List<String[]> currentExamples = examplesForTranslation.get(translation);
		if (currentExamples == null) {
			currentExamples = new ArrayList<String[]>();
		}
		currentExamples.add(example);
		examplesForTranslation.put(translation, currentExamples);

		if (tLogger.isTraceEnabled()) {
			tLogger.trace("upon exit, possible translations="+
				StringUtils.join(possibleTranslationsIn("en").iterator(), ","));
		}

		_translationsNeedSorting = true;

		return this;
	}

	private void addBilingualExamples(
		String translation, List<String[]> examples, Boolean forRelatedWord)
		throws MultilingualDictException {
		for (String[] anExample: examples) {
			addBilingualExample(translation, anExample, forRelatedWord);
		}
	}


	public List<String[]> bilingualExamplesOfUse() {
		List<String[]> allExamples = new ArrayList<String[]>();
		for (String translation: examplesForOrigWordTranslation.keySet()) {
			allExamples.addAll(examplesForOrigWordTranslation.get(translation));
		}
		return allExamples;
	}

	public List<String[]> bilingualExamplesOfUse(String translation) throws MultilingualDictException {
		List<String[]> examples = new ArrayList<String[]>();
		if (examplesForOrigWordTranslation.containsKey(translation)) {
			examples = examplesForOrigWordTranslation.get(translation);
		}

		return examples;
	}

	public void ensureIUScript(TransCoder.Script script) throws MultilingualDictException {
		try {
			for (int ii = 0; ii < relatedWords.length; ii++) {
				relatedWords[ii] = TransCoder.ensureScript(script, relatedWords[ii]);
			}

			for (String translation : this.examplesForOrigWordTranslation.keySet()) {
				List<String[]> examples = this.examplesForOrigWordTranslation.get(translation);
				for (int ii = 0; ii < examples.size(); ii++) {
					String[] example_ii = examples.get(ii);
					example_ii[0] = TransCoder.ensureScript(script, example_ii[0]);
					if (script == TransCoder.Script.SYLLABIC) {
						// Restore the <strong> tags to Roman
						example_ii[0] = example_ii[0].replaceAll("ᔅᑦᕐoᖕ>", "strong>");
					}
					examples.set(ii, example_ii);
				}
			}
		} catch (TransCoderException e) {
			throw new MultilingualDictException(e);
		}
		return;
	}

	public List<String> possibleTranslationsIn(String lang) throws MultilingualDictException {
		return  possibleTranslationsIn(lang, (Boolean)null);
	}

	public List<String> possibleTranslationsIn(String lang, Boolean forRelatedWords) throws MultilingualDictException {
		assertIsSupportedLanguage(lang);
		if (forRelatedWords == null) {
			forRelatedWords = false;
		}

		List<String> translations = origWordTranslations;
		if (forRelatedWords) {
			translations = relatedWordTranslations;
		}

		return translations;
	}

	public void sortTranslations() throws MultilingualDictException {
		Logger tLogger = Logger.getLogger("org.iutools.worddict.MultilingualDictEntry.sortTranslations");
		try {
			if (_translationsNeedSorting) {
				tLogger.trace("sorting orig word translations");
				TranslationComparator comparator =
					new TranslationComparator(otherLang(), this.examplesForOrigWordTranslation);
				Collections.sort(this.origWordTranslations, comparator);
				tLogger.trace("sorting related words translations");
				comparator =
					new TranslationComparator(otherLang(), this.examplesForRelWordsTranslation);
				Collections.sort(this.relatedWordTranslations, comparator);
			}
		} catch (RuntimeException e) {
			throw new MultilingualDictException(e);
		}
		return;
	}

	public void addRelatedWordTranslations(MultilingualDictEntry entry) throws MultilingualDictException {
		List<String> relatedWordTranslations = entry.possibleTranslationsIn("en");
		for (String translation: relatedWordTranslations) {
			List<String[]> examplesOfUse = entry.bilingualExamplesOfUse(translation);
			this.addBilingualExamples(translation, examplesOfUse, true);
		}
	}


	public int totalBilingualExamples() throws MultilingualDictException {
		int total = bilingualExamplesOfUse("ALL").size();
		return total;
	}

	public static class TranslationComparator implements java.util.Comparator<String> {

		private final Map<String, List<String[]>> _examplesForTranslation;
		private final Object lang;

		public TranslationComparator(
			String _lang, Map<String, List<String[]>> _examplesForTranslation) {
			this._examplesForTranslation = _examplesForTranslation;
			this.lang = _lang;
		}

		@Override
		public int compare(String t1, String t2) {
			Logger tLogger = Logger.getLogger("org.iutools.worddict.MultilingualDictEntry.TranslationComparator.compare");
			tLogger.trace("t1="+t1+", t2="+t2);
			List<String[]> t1Examples = _examplesForTranslation.get(t1);
			int t1NumEx = 0;
			if (t1Examples != null) {
				t1NumEx = t1Examples.size();
			}
			List<String[]> t2Examples = _examplesForTranslation.get(t2);
			int t2NumEx = 0;
			if (t2Examples != null) {
				t2NumEx = t2Examples.size();
			}
			int comp = Integer.compare(t2NumEx, t1NumEx);
			tLogger.trace("t1NumEx="+t1NumEx+", t2NumEx="+t2NumEx+": comp="+comp);
			if (comp == 0) {
				// If there are the same number of examples for both
				// translations, prefer translations that do not have a "gap"
				tLogger.trace("Same number of examples; Looking at number of gaps");
				float t1Gap = Math.signum(t1.indexOf("..."));
				float t2Gap = Math.signum(t2.indexOf("..."));
				comp = Float.compare(t1Gap, t2Gap);
				tLogger.trace("t1Gap="+t1Gap+", t2Gap="+t2Gap+": comp="+comp);
			}

			if (comp == 0) {
				// If translations are equivalent in terms of gaps, prefer shorter ones
				tLogger.trace("Equivalent in terms of gaps; Looking at length");
				comp = Integer.compare(t1.length(), t2.length());
			}

			if (comp == 0) {
				// If all else fails, sort alphabetically
				tLogger.trace("Same length; Sorting alphabetically");
				comp = this.compareAlphabetically(t1, t2);
				tLogger.trace("t1="+t1+", t2="+t2+": comp="+comp);
			}

			tLogger.trace("For t1="+t1+", t2="+t2+", returning comp="+comp);
			return comp;
		}

		private int compareAlphabetically(String t1, String t2) {
			int comp = 0;
			if (lang.equals("iu")) {
				try {
					t1 = TransCoder.ensureScript(TransCoder.Script.ROMAN, t1);
					t2 = TransCoder.ensureScript(TransCoder.Script.ROMAN, t2);
					comp = t1.compareToIgnoreCase(t2);
				} catch (TransCoderException e) {
					throw new RuntimeException(e);
				}
			}

			return comp;
		}
	}
}