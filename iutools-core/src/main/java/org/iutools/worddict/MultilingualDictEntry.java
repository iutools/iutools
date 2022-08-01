package org.iutools.worddict;

import ca.nrc.json.PrettyPrinter;
import ca.nrc.string.StringUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.linguisticdata.LinguisticData;
import org.iutools.linguisticdata.Morpheme;
import org.iutools.linguisticdata.MorphemeException;
import org.iutools.linguisticdata.MorphemeHumanReadableDescr;
import org.iutools.morph.Decomposition;
import org.iutools.morph.DecompositionException;
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

	/** Translations sorted from best to worst.
	 * These may be translations for the original word or for related words.
	 */
	public List<String> sortedTranslations = new ArrayList<String>();

	/** Provides translations for diffferent l1 words. These may be the original
	   word or related words */
	public Map<String,List<String>> translations4word = new HashMap<String,List<String>>();

	/** Provides bilingual examples for an L1 word and its L2 translation. The L1
	   word may be the original word, or one of the related words.

	   Note: We store sentence pairs as String[] instead of Pair<String,String>
	     because the latter is jsonified as a dictionary where

	       - key is the first sentence
	       - value is the second sentence

	     and this turns out to be awkward to use on the client-side JavaScript
	     code.
	 */
	public Map<String,List<String[]>> examples4Translation
		= new HashMap<String,List<String[]>>();

	private boolean _translationsNeedSorting = true;

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

	public MultilingualDictEntry setDecomp(String[] morphemes) throws MultilingualDictException {
		try {
			if (morphemes != null) {
				morphDecomp = new ArrayList<MorphemeHumanReadableDescr>();
				for (String morpheme : morphemes) {
					String morphID = Decomposition.parseComponent(morpheme, true).getRight();
					Morpheme morphInfo = LinguisticData.getInstance().getMorpheme(morphID);
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
		} catch (DecompositionException e) {
			throw new MultilingualDictException(e);
		}
		return this;
	}

	public List<String> decomposition() {
		List<String> decomp = new ArrayList<String>();
		for (MorphemeHumanReadableDescr morph: morphDecomp) {
			decomp.add(morph.id);
		}
		return decomp;
	}

	public boolean hasTranslationsForOriginalWord() {
		boolean answer = translations4word.containsKey(word);
		return answer;
	}

	public MultilingualDictEntry addBilingualExample(
		String translation, String[] example, String l1Word) throws MultilingualDictException {
		Logger tLogger = LogManager.getLogger("org.iutools.worddict.MultilingualDictEntry.addBilingualExample");
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("translation=" + translation + ", l1Word=" + l1Word + ", example=" + String.join(", ", example));
		}

		// Add this new translation
		{
			if (!translation.equals("ALL") && !sortedTranslations.contains(translation)) {
				sortedTranslations.add(translation);
				_translationsNeedSorting = true;
			}
			if (!translations4word.containsKey(l1Word)) {
				translations4word.put(l1Word, new ArrayList<String>());
			}
			List<String> translations = translations4word.get(l1Word);
			if (!translations.contains(translation)) {
				translations.add(translation);
				_translationsNeedSorting = true;
			}
		}

		// Add bilingual examples for that translation
		{
			if (!examples4Translation.containsKey(translation)) {
				examples4Translation.put(translation, new ArrayList<String[]>());
			}
			List<String[]> currentExamples2 = examples4Translation.get(translation);
			if (currentExamples2 == null) {
				currentExamples2 = new ArrayList<String[]>();
			}
			currentExamples2.add(example);
		}

		if (tLogger.isTraceEnabled()) {
			tLogger.trace("upon exit, possible translations="+
				StringUtils.join(sortedTranslations.iterator(), ","));
		}

		return this;
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
			addBilingualExample(translation, anExample, relWord);
		}
	}

	/**
	 * Returns translations of the original word if available, and translations
	 * of related words otherwise.
	 */
	public List<String> bestTranslations() {
		return sortedTranslations;
	}

	public List<String[]> bilingualExamplesOfUse() throws MultilingualDictException {
		List<String[]> allExamples = new ArrayList<String[]>();

		for (String translation: bestTranslations()) {
			List<String[]> examples = bilingualExamplesOfUse(translation);
			allExamples.addAll(examples);
		}
		return allExamples;
	}

	/**
	 * Returns examples of use for a translation. If there are examples of this
	 * as a translation of the original word, return those, otherwise return
	 * examples of this as a translation of a related word.
	 */
	public List<String[]> bilingualExamplesOfUse(String translation) throws MultilingualDictException {
		List<String[]> examples = new ArrayList<String[]>();
		if (examples4Translation.containsKey(translation)) {
			examples = examples4Translation.get(translation);
		}

		return examples;
	}

	public void sortAndPruneTranslations(
		int maxTranslations, Integer minRequiredPairs) throws MultilingualDictException {
		Logger tLogger = LogManager.getLogger("org.iutools.worddict.MultilingualDictEntry.sortTranslations");
		try {
			if (_translationsNeedSorting) {
				tLogger.trace("sorting translations for word: "+this.wordRoman);

				TranslationComparator comparator =
					new TranslationComparator(otherLang(), this.examples4Translation);
				Collections.sort(sortedTranslations, comparator);
				sortedTranslations =
					pruneTranslations(
					sortedTranslations, maxTranslations, examples4Translation, minRequiredPairs);

			}
		} catch (RuntimeException e) {
			throw new MultilingualDictException(e);
		}
		return;
	}

	private List<String> pruneTranslations(
	List<String> translations, int maxTranslations, Map<String, List<String[]>> examples, Integer minRequiredPairs) {
		List<String> pruned = new ArrayList<String>();
		if (!translations.isEmpty()) {
			int translationNum = 0;
			String topTranslation = translations.get(0);
			List<String[]> topTranslationExamples = examples.get(topTranslation);
			for (String aTranslation: translations) {
				translationNum++;
				List<String[]> aTranslationExamples = examples.get(aTranslation);
				boolean removeTranslation = translationNum > maxTranslations;
				if (!removeTranslation &&
				   topTranslationExamples.size() >= minRequiredPairs &&
				   aTranslationExamples.size() < minRequiredPairs) {
					//
					// Some of the translations had more than the minimum number of
					// examples, but we have now reached a point where remaining
					// translations have less than that. So stop here because there is not enough evidence to
					// support those remaining translations.
					//
					// Note: If none of the translations we got have enough examples,
					// we choose to keep all of them.
					removeTranslation = true;
				}
				if (!removeTranslation) {
					pruned.add(aTranslation);
				} else {
					examples.remove(aTranslation);
					continue;
				}
			}
		}

		return pruned;
	}

	public void addRelatedWordTranslations(MultilingualDictEntry entry) throws MultilingualDictException {
		List<String> relatedWordTranslations = entry.sortedTranslations;
		String relWord = entry.word;
		for (String translation: relatedWordTranslations) {
			List<String[]> examplesOfUse = entry.bilingualExamplesOfUse(translation);
			this.addBilingualExamples(translation, examplesOfUse, true, relWord);
		}
	}

	public int totalBilingualExamples() throws MultilingualDictException {
		int total = 0;
		for (String aTranslation: examples4Translation.keySet()) {
			if (aTranslation.equals("ALL")) {
				continue;
			}
			total += examples4Translation.get(aTranslation).size();
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
			Logger tLogger = LogManager.getLogger("org.iutools.worddict.MultilingualDictEntry.TranslationComparator.compare");
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
				// We favor translations that do not include a gap in them
				if (t1.contains("...") && !t2.contains("...")) {
					comp = 1;
				} else if (!t1.contains("...") && t2.contains("...")) {
					comp = -1;
				}
			}

			if (comp == 0) {
				// We favour multi-word translations (as long as they don't contains
				// a gap) because they are more precise
				int t1Words = t1.split("\\s+").length;
				int t2Words = t2.split("\\s+").length;
				comp = Integer.compare(t2Words, t1Words);
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
		Logger logger = LogManager.getLogger("org.iutools.worddict.MultilingualDictEntry.ensureScript");
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
				new List[] {sortedTranslations}) {
				CollectionTranscoder.transcodeList(script, translations);
			}
		} else {
			// Input word is iu and its translations are en
			try {
				// Transcode keys of the map that provides en translations for each
				// iu related word.
				CollectionTranscoder.transcodeKeys(script, this.translations4word);
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
				examples4Translation
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

	@JsonIgnore
	public boolean isMisspelled() {
		boolean misspelled = true;
		if (morphDecomp != null && !morphDecomp.isEmpty()) {
			misspelled = false;
		}
		return misspelled;
	}

	boolean isEmpty() {
		boolean empty = true;
		if (
			(!isMisspelled())  |
			(examples4Translation != null && !examples4Translation.isEmpty()) |
			(sortedTranslations != null && !sortedTranslations.isEmpty()) |
			(relatedWords != null && relatedWords.length > 0) |
			definition != null) {
			empty = false;
		}

		return empty;
	}

}