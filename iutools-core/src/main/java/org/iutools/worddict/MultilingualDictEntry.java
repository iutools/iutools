package org.iutools.worddict;

import ca.nrc.json.PrettyPrinter;
import ca.nrc.string.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.iutools.linguisticdata.LinguisticData;
import org.iutools.linguisticdata.Morpheme;
import org.iutools.linguisticdata.MorphemeException;
import org.iutools.linguisticdata.MorphemeHumanReadableDescr;
import org.iutools.script.CollectionTranscoder;
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
	public Map<String,List<String>> relatedWordTranslationsMap = new HashMap<String,List<String>>();

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
		String translation, String[] example, boolean forRelatedWord)
		throws MultilingualDictException {
		return addBilingualExample(translation, example, forRelatedWord, (String)null);
	}


	public MultilingualDictEntry addBilingualExample(
		String translation, String[] example, boolean forRelatedWord,
		String relWord) throws MultilingualDictException {
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
		if (relWord != null) {
			updateRelWordTranslationsMap(relWord, translation);
		}

		if (tLogger.isTraceEnabled()) {
			tLogger.trace("upon exit, possible translations="+
				StringUtils.join(possibleTranslationsIn("en").iterator(), ","));
		}

		_translationsNeedSorting = true;

		return this;
	}

	private void updateRelWordTranslationsMap(String relWord, String translation) {
		if (!relatedWordTranslationsMap.containsKey(relWord)) {
			relatedWordTranslationsMap.put(relWord, new ArrayList<String>());
		}
		List<String> relTranslations = relatedWordTranslationsMap.get(relWord);
		if (!relTranslations.contains(translation)) {
			relTranslations.add(translation);
		}
	}

	private void addBilingualExamples(
		String translation, List<String[]> examples, Boolean forRelatedWord)
		throws MultilingualDictException {
		addBilingualExamples(translation, examples, forRelatedWord, (String)null);
	}

	private void addBilingualExamples(
		String translation, List<String[]> examples, Boolean forRelatedWord,
		String relWord)
		throws MultilingualDictException {
		for (String[] anExample: examples) {
			addBilingualExample(translation, anExample, forRelatedWord, relWord);
		}
	}

	public List<String> allTranslations() {
		List<String> all = new ArrayList<String>();
		all.addAll(origWordTranslations);
		for (String aTransl: relatedWordTranslations) {
			if (!all.contains(aTransl)) {
				all.add(aTransl);
			}
		}
		return all;
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
		String relWord = entry.word;
		for (String translation: relatedWordTranslations) {
			List<String[]> examplesOfUse = entry.bilingualExamplesOfUse(translation);
			this.addBilingualExamples(translation, examplesOfUse, true, relWord);
		}
	}


	public int totalBilingualExamples() throws MultilingualDictException {
		int total = bilingualExamplesOfUse("ALL").size();
		return total;
	}

	public int totalBilingualExamples(MultilingualDict.WhatTerm where) throws MultilingualDictException {
		int total = 0;
		Map<String, List<String[]>> examplesMap = examplesForOrigWordTranslation;
		if (where == MultilingualDict.WhatTerm.RELATED) {
			examplesMap = examplesForRelWordsTranslation;
		}
		for (String aTranslation: examplesMap.keySet()) {
			if (aTranslation.equals("ALL")) {
				continue;
			}
			total += examplesMap.get(aTranslation).size();
		}
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

	public void ensureScript(TransCoder.Script script)
		throws MultilingualDictException{
		Logger logger = Logger.getLogger("org.iutools.worddict.MultilingualDictEntry.ensureScript");
		logger.trace("word="+word+", script="+script);
		ensureScript_translations(script);
		ensureScript_relatedwords(script);
		ensureScript_BilingualExamples(script);
		ensureScript_word(script);
		if (logger.isTraceEnabled()) {
			logger.trace("upon exit, this="+ PrettyPrinter.print(this));
		}
	}

	private void ensureScript_word(TransCoder.Script script) throws MultilingualDictException {
		try {
			if (lang.equals("iu") && word != null) {
				word = TransCoder.ensureScript(script, word);
				wordInOtherScript = TransCoder.inOtherScript(word);
			}
		} catch (TransCoderException e) {
			throw new MultilingualDictException(e);
		}
	}

	private void ensureScript_translations(TransCoder.Script script) throws MultilingualDictException {
		if (otherLang().equals("iu")) {
			// Input word is en and its translations are iu
			for (List<String> translations:
				new List[] {origWordTranslations, relatedWordTranslations}) {
				CollectionTranscoder.transcodeList(script, translations);
//				for (int ii=0; ii < translations.size(); ii++) {
//					try {
//
//						translations.set(ii,
//							TransCoder.ensureScript(script, translations.get(ii))
//							);
//					} catch (TransCoderException e) {
//						throw new MultilingualDictException(e);
//					}
//				}
			}
		} else {
			// Input word is iu and its translations are en
			try {
				// Transcode keys of the map that provides en translations for each
				// iu related word.
				CollectionTranscoder.transcodeKeys(script, this.relatedWordTranslationsMap);
			} catch (TransCoderException e) {
				throw new MultilingualDictException(e);
			}
		}
	}

	private void ensureScript_relatedwords(TransCoder.Script script) throws MultilingualDictException {

//		try {
//			for (int ii = 0; ii < relatedWords.length; ii++) {
//				relatedWords[ii] = TransCoder.ensureScript(script, relatedWords[ii]);
//			}
//		} catch (TransCoderException e) {
//			throw new MultilingualDictException(e);
//		}

		CollectionTranscoder.transcodeArray(script, relatedWords);
		return;
	}

	private void ensureScript_BilingualExamples(TransCoder.Script script) throws MultilingualDictException {

		Map<String, List<String[]>>[] alignmentMaps =
			new Map[] {
				examplesForOrigWordTranslation,
				examplesForRelWordsTranslation
			};
		for (Map<String, List<String[]>> anAlignmentsMap: alignmentMaps) {
			ensureScript_alignmentMap(script, anAlignmentsMap);
		}
	}

	private void ensureScript_alignmentMap(
		TransCoder.Script script, Map<String, List<String[]>> anAlignmentsMap) throws MultilingualDictException {
		Map<String, List<String[]>> convertedMap = new HashMap<String, List<String[]>>();
		for (String translation: anAlignmentsMap.keySet()) {
			String convertedTranslation = translation;
			if (otherLang().equals("iu")) {
				try {
					convertedTranslation = TransCoder.ensureScript(script, translation);
				} catch (TransCoderException e) {
					throw new MultilingualDictException(e);
				}
			}
			List<String[]> alignments = anAlignmentsMap.get(translation);
			for (int ii=0; ii < alignments.size(); ii++) {
				String[] convertedAlignment = ensureScript_alignment(script, alignments.get(ii));
				alignments.set(ii, convertedAlignment);
			}
			convertedMap.put(convertedTranslation, alignments);
		}

		anAlignmentsMap.clear();
		for (String translation: convertedMap.keySet()) {
			anAlignmentsMap.put(translation, convertedMap.get(translation));
		}
		return;
	}

	private String[] ensureScript_alignment(
		TransCoder.Script script, String[] alignment) throws MultilingualDictException {
		int iuSide = 0;
		if (!lang.equals("iu")) {
			iuSide = 1;
		}
		String iuSentence = alignment[iuSide];
		try {
			String iuConverted = TransCoder.ensureScript(script, iuSentence);
			iuConverted = iuConverted.replaceAll("<ᔅᑦᕐoᖕ>", "<strong>");
			iuConverted = iuConverted.replaceAll("</ᔅᑦᕐoᖕ>", "</strong>");
			alignment[iuSide] = iuConverted;
		} catch (TransCoderException e) {
			throw new MultilingualDictException(e);
		}

		return alignment;
	}

}