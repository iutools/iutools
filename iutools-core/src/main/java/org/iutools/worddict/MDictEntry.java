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
import static org.iutools.script.TransCoder.Script;
import org.iutools.script.TransCoderException;

import java.util.*;

public class MDictEntry {

	private Script inScript = null;

	public static enum Field {
		DEFINITION, BILINGUAL_EXAMPLES, TRANSLATIONS, DECOMP, RELATED_WORDS
	}

	private String word = null;
	private String lang = "iu";
	private String wordInOtherScript = null;

	private String wordSyllabic;
	private String wordRoman;
	public String definition;
	public List<MorphemeHumanReadableDescr> morphDecomp = new ArrayList<MorphemeHumanReadableDescr>();

	/** Standardized spelling in ROMAN and SYLLABIC */
	public String romanStandardized = null;
	public String syllStandardized = null;
	public String wordStandardizedSpelling = null;
	public String otherScriptStandardizedSpelling = null;

	/** Translations sorted from best to worst.
	 * These may be translations for the original word or for related words.
	 */
	public List<String> bestTranslations = new ArrayList<String>();

	/**
	 * Human-generated translations for the word and related words (if any were
	 * found).
	 */
	public Map<String,List<String>> humanTranslations =
		new HashMap<String,List<String>>();

	/**
	 * Sources for human-generated translations.
	 */
	public Map<String,Set<String>> humanTranslationSources =
		new HashMap<String,Set<String>>();

	/** Provides translations for diffferent l1 words. These may be the original
	   word or related words */
	public Map<String,List<String>> translations4l1Word = new HashMap<String,List<String>>();


	/** Provides translations for diffferent l2 words. These may be the original
	   word or related words */
	public Map<String,Set<String>> translations4l2Word = new HashMap<String,Set<String>>();

	/** Provides bilingual examples for an L1 word and its L2 translation. The L1
	   word may be the original word, or one of the related words.

	   Note: We store sentence pairs as String[] instead of Pair<String,String>
	     because the latter is jsonified as a dictionary where

	       - key is the first sentence
	       - value is the second sentence

	     and this turns out to be awkward to use on the client-side JavaScript
	     code.
	 */
	public Map<String,List<String[]>> translationExamplesIndex
		= new HashMap<String,List<String[]>>();

	private boolean _translationsNeedSorting = true;

	public String[] relatedWords = new String[0];

	TermNormalizer termNormalizer = new TermNormalizer();

	/**
	 * Index of how terms in different languages were normalized for inclusion
	 * in the various Maps and Lists used internally by the WordEntry.
	 */
	public Map<String, Map<String, String>> normalizedTerms;

	public static void assertIsSupportedLanguage(String lang) throws MachineGeneratedDictException {
		if (!lang.matches("^(en|iu)$")) {
			throw new MachineGeneratedDictException("Unsupported language '"+lang+"'");
		}
	}

	public void setLang(String _lang) {
		this.lang = _lang;
	}

	public String getLang() {
		return this.lang;
	}

	public String otherLang() throws MachineGeneratedDictException {
		return otherLang(lang);
	}

	public static String otherLang(String lang) throws MachineGeneratedDictException {
		assertIsSupportedLanguage(lang);
		String other = null;
		if (lang.equals("iu")) {
			other = "en";
		} else if (lang.equals("en")) {
			other = "iu";
		}
		return other;
	}

	public void setWord(String _word) throws MachineGeneratedDictException {
		this.word = _word;
		try {
			if (word != null && lang.equals("iu")) {
				inScript = TransCoder.textScript(word);
				wordSyllabic = TransCoder.ensureScript(Script.SYLLABIC, word);
				wordRoman = TransCoder.ensureScript(Script.ROMAN, word);
				if (!wordSyllabic.equals(word)) {
					wordInOtherScript = wordSyllabic;
				} else {
					wordInOtherScript = wordRoman;
				}
			}
		} catch (TransCoderException e) {
			throw new MachineGeneratedDictException(e);
		}
	}

	public String getWord() {
		return word;
	}

	public void setWordInOtherScript(String _word) {
		this.wordInOtherScript = _word;
	}

	public String getWordInOtherScript() {
		return wordInOtherScript;
	}

	public void setWordSyllabic(String _word) {
		this.wordSyllabic = _word;
	}

	public String getWordSyllabic() {
		return wordSyllabic;
	}

	public void setWordRoman(String _word) {
		this.wordRoman = _word;
	}

	public String getWordRoman() {
		return wordRoman;
	}

	public String inSameScriptAsInputWord(String text) throws MachineGeneratedDictException {
		String convertedText = null;
		try {
			convertedText = TransCoder.ensureScript(inScript, text);
		} catch (TransCoderException e) {
			throw new MachineGeneratedDictException(e);
		}
		return convertedText;
	}

	public String normalizeTerm(String term, String termLang) throws MachineGeneratedDictException {
		try {
			return termNormalizer.normalize(term, termLang, inScript);
		} catch (TermNormalizerException e) {
			throw new MachineGeneratedDictException(e);
		}
	}

	/** Ensure that the normalizedTerms table has an entry for all
	 * terms used in the entry */
	public void includeNormalizationForAllTerms() throws MachineGeneratedDictException {
		try {
			// Normalize individual terms in the SOURCE language
			for (String aTerm: new String[] {
				word, wordInOtherScript, wordRoman, wordSyllabic}) {
				termNormalizer.normalize(aTerm, lang, inScript);
				if (lang.equals("iu")) {
					String aTermOtherScript = TransCoder.inOtherScript(aTerm);
					termNormalizer.normalize(aTermOtherScript, lang, inScript);
				}
			}

			// Normalize arrays of individual terms in the SOURCE language
			for (String[] termArray: new String[][] {
				relatedWords}) {
				for (String aTerm: termArray) {
					termNormalizer.normalize(aTerm, lang, inScript);
					if (lang.equals("iu")) {
						String aTermOtherScript = TransCoder.inOtherScript(aTerm);
						termNormalizer.normalize(aTermOtherScript, lang, inScript);
					}
				}
			}

			// Normalize lists of terms in the TARGET language
			for (List<String> aTermsList: new List[] {
				bestTranslations}) {
				for (String aTerm: aTermsList) {
					termNormalizer.normalize(aTerm, otherLang());
					if (otherLang().equals("iu")) {
						String aTermOtherScript = TransCoder.inOtherScript(aTerm);
						termNormalizer.normalize(aTermOtherScript, otherLang(), inScript);
					}

				}
			}

			// Normalize maps whose keys are lists of TARGET language terms
			for (Map<String,List<String>> aMap: new Map[] {
				humanTranslations}) {
				for (Map.Entry<String, List<String>> anEntry: aMap.entrySet()) {
					List<String> l2Terms = anEntry.getValue();
					for (String aTerm: l2Terms) {
						termNormalizer.normalize(aTerm, otherLang(), inScript);
						if (otherLang().equals("iu")) {
							String aTermOtherScript = TransCoder.inOtherScript(aTerm);
							termNormalizer.normalize(aTermOtherScript, otherLang(), inScript);
						}
					}
				}
			}

		} catch (TermNormalizerException | TransCoderException e) {
			throw new MachineGeneratedDictException(e);
		}
	}


	public Set<String> addGlossEntries4word(String word, List<GlossaryEntry> glossEntries) throws MachineGeneratedDictException {
		Set<String> translations = new HashSet<String>();
		for (GlossaryEntry glossEntry: glossEntries) {
			String source = glossEntry.source;
			List<String> glossTranslations = glossEntry.termsInLang(otherLang());
			addHumanTranslations(word, glossTranslations, source);
			translations.addAll(glossTranslations);
		}
		return translations;
	}

	private void addHumanTranslations(
		String word, List<String> translations, String source) throws MachineGeneratedDictException {
		if (!humanTranslations.containsKey(word)) {
			humanTranslations.put(word, new ArrayList<String>());
		}
		if (!humanTranslationSources.containsKey(word)) {
			humanTranslationSources.put(word, new HashSet<String>());
		}
		for (String aTranslation: translations) {
			if (!humanTranslations.containsKey(aTranslation)) {
				humanTranslations.put(aTranslation, new ArrayList<String>());
			}
			if (!humanTranslationSources.containsKey(aTranslation)) {
				humanTranslationSources.put(aTranslation, new HashSet<String>());
			}

			humanTranslations.get(word).add(aTranslation);
			if (!bestTranslations.contains(aTranslation)) {
				bestTranslations.add(aTranslation);
			}
			humanTranslationSources.get(word).add(source);
			humanTranslationSources.get(aTranslation).add(source);
			addTranslation4Word(word, aTranslation);
		}
	}

	public void addGlossarySource(String translation,  String glossSource) {
		if (!humanTranslationSources.containsKey(translation)) {
			humanTranslationSources.put(translation, new HashSet<String>());
		}
		humanTranslationSources.get(translation).add(glossSource);
	}

	public boolean isHumanTranslation(String translation) {
		boolean answer = humanTranslations.containsKey(translation);
		return answer;
	}

	private List<Pair<String, Double>> _otherLangTranslations = null;

	public List<Pair<String, Double>> otherLangTranslations() {
		if (_otherLangTranslations == null) {
			_otherLangTranslations = new ArrayList<Pair<String, Double>>();
		}
		return _otherLangTranslations;
	}

	public MDictEntry() throws MachineGeneratedDictException {
		init_IUWordDictEntry((String)null, (String)null);
	}

	public MDictEntry(String _word) throws MachineGeneratedDictException {
		init_IUWordDictEntry(_word, (String)null);
	}

	public MDictEntry(String _word, String _lang) throws MachineGeneratedDictException {
		init_IUWordDictEntry(_word, _lang);
	}

	private void init_IUWordDictEntry(String _word, String _lang) throws MachineGeneratedDictException {
		if (_lang == null) {
			_lang = "iu";
		}
		assertIsSupportedLanguage(this.lang);
		setLang(_lang);
		setWord(_word);
		this.normalizedTerms = termNormalizer.normalizedTerms();
	}

	public MDictEntry setDecomp(String[] morphemes) throws MachineGeneratedDictException {
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
						throw new MachineGeneratedDictException(e);
					}
				}
			}
		} catch (DecompositionException e) {
			throw new MachineGeneratedDictException(e);
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
		boolean answer = translations4l1Word.containsKey(word);
		if (!answer && lang.equals("iu")) {
			answer = translations4l1Word.containsKey(wordInOtherScript);
		}
		return answer;
	}

	public MDictEntry addBilingualExample(
		String translation, String[] example, String l1Word) throws MachineGeneratedDictException {
		Logger tLogger = LogManager.getLogger("org.iutools.worddict.MDictEntry.addBilingualExample");
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("translation=" + translation + ", l1Word=" + l1Word + ", example=" + String.join(", ", example));
		}

		// Add this new translation
		{
			if (!translation.equals("ALL") && !bestTranslations.contains(translation)) {
				bestTranslations.add(translation);
				_translationsNeedSorting = true;
			}
			if (!hasTranslationForWord(l1Word)) {
				addTranslations4Word(l1Word, new ArrayList<String>());
			}

			if (!translationsInclude(l1Word, translation)) {
				addTranslation4Word(l1Word, translation);
				_translationsNeedSorting = true;
			}
		}

		// Add bilingual examples for that translation
		{
			if (!translationExamplesIndex.containsKey(translation)) {
				translationExamplesIndex.put(translation, new ArrayList<String[]>());
			}
			List<String[]> currentExamples2 = translationExamplesIndex.get(translation);
			if (currentExamples2 == null) {
				currentExamples2 = new ArrayList<String[]>();
			}
			currentExamples2.add(example);

			addExample4translation(translation, example);
		}

		if (tLogger.isTraceEnabled()) {
			tLogger.trace("upon exit, possible translations="+
				StringUtils.join(bestTranslations.iterator(), ","));
		}

		return this;
	}

	private boolean hasTranslationForWord(String l1Word) throws MachineGeneratedDictException {
		boolean answer = translations4l1Word.containsKey(l1Word);
		if (!answer && lang.equals("iu")) {
			String l1WordOtherScript = null;
			try {
				l1WordOtherScript = TransCoder.inOtherScript(l1Word);
			} catch (TransCoderException e) {
				throw new MachineGeneratedDictException(e);
			}
			answer = translations4l1Word.containsKey(l1WordOtherScript);
		}
		return answer;
	}

	public void addTranslations4Word(String l1Word, List<String> newTranslations) throws MachineGeneratedDictException {
		if (lang.equals("iu")) {
			l1Word = normalizeTerm(l1Word, lang);
			try {
				TransCoder.ensureScript(inScript, newTranslations);
			} catch (TransCoderException e) {
				throw new MachineGeneratedDictException(e);
			}
		}
		if (!translations4l1Word.containsKey(l1Word)) {
			translations4l1Word.put(l1Word, new ArrayList<String>());
		}
		List<String> existingTranslations = translations4l1Word.get(l1Word);
		for (String aTranslation: newTranslations) {
			aTranslation = normalizeTerm(aTranslation, otherLang());
			if (!translations4l2Word.containsKey(aTranslation)) {
				translations4l2Word.put(aTranslation, new HashSet<String>());
			}
			Set<String> existingReverseTranslations = translations4l2Word.get(aTranslation);
			if (!aTranslation.equals("ALL")) {
				if (!existingTranslations.contains(aTranslation)) {
					existingTranslations.add(aTranslation);
				}
				existingReverseTranslations.add(l1Word);
			}
		}
		return;
	}

	public void addTranslation4Word(String l1Word, String translation) throws MachineGeneratedDictException {
		List<String> justOneTranslation = new ArrayList<String>();
		justOneTranslation.add(translation);
		addTranslations4Word(l1Word, justOneTranslation);
	}

	public boolean translationsInclude(String l1Word, String translation) throws MachineGeneratedDictException {
		l1Word = normalizeTerm(l1Word, lang);
		List<String> translations = translations4l1Word.get(l1Word);
		// To speed up, check if the list of translations is empty before
		// possibly transcoding the translation
		//
		boolean answer = (!translations.isEmpty());
		if (answer) {
			translation = normalizeTerm(translation, otherLang());
			answer = translations.contains(translation);
		}
		return answer;
	}

	private void addBilingualExamples(
		String translation, List<String[]> examples, Boolean forRelatedWord)
		throws MachineGeneratedDictException {
		addBilingualExamples(translation, examples, forRelatedWord, (String)null);
	}

	private void addBilingualExamples(
		String translation, List<String[]> examples, Boolean forRelatedWord,
		String relWord)
		throws MachineGeneratedDictException {
		for (String[] anExample: examples) {
			addBilingualExample(translation, anExample, relWord);
		}
	}

	public List<String[]> examples4Translation(String translation) throws MachineGeneratedDictException {
		try {
			translation = normalizeTerm(translation, otherLang());
		} catch (MachineGeneratedDictException e) {
			throw new MachineGeneratedDictException(e);
		}
		List<String[]> examples = new ArrayList<String[]>();
		if (translationExamplesIndex.containsKey(translation)) {
			examples = translationExamplesIndex.get(translation);
		}
		return examples;
	}

	public void addExample4translation(String translation, String[] example) throws MachineGeneratedDictException {
		try {
			translation = normalizeTerm(translation, otherLang());
		} catch (MachineGeneratedDictException e) {
			throw new MachineGeneratedDictException(e);
		}
		if (!translationExamplesIndex.containsKey(translation)) {
			translationExamplesIndex.put(translation, new ArrayList<String[]>());
		}
		translationExamplesIndex.get(translation).add(example);
	}

	public void removeExamples4Translation(String translation) throws MachineGeneratedDictException {
		try {
			translation = normalizeTerm(translation, otherLang());
		} catch (MachineGeneratedDictException e) {
			throw new MachineGeneratedDictException(e);
		}
		translationExamplesIndex.remove(translation);
	}

	public List<String[]> bilingualExamplesOfUse() throws MachineGeneratedDictException {
		List<String[]> allExamples = new ArrayList<String[]>();

		for (String translation: bestTranslations) {
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
	public List<String[]> bilingualExamplesOfUse(String translation) throws MachineGeneratedDictException {
		List<String[]> examples = new ArrayList<String[]>();
		if (translationExamplesIndex.containsKey(translation)) {
			examples = translationExamplesIndex.get(translation);
		}

		return examples;
	}

	public void sortAndPruneTranslations(
		int maxTranslations, Integer minRequiredPairs) throws MachineGeneratedDictException {
		Logger tLogger = LogManager.getLogger("org.iutools.worddict.MDictEntry.sortTranslations");
		try {
			if (_translationsNeedSorting) {
				tLogger.trace("sorting translations for word: "+this.wordRoman);

				TranslationComparator comparator =
					new TranslationComparator(otherLang(), this.humanTranslations, this.translationExamplesIndex);
				Collections.sort(bestTranslations, comparator);
				bestTranslations =
					pruneTranslations(
						bestTranslations, maxTranslations, minRequiredPairs);
			}
		} catch (RuntimeException e) {
			throw new MachineGeneratedDictException(e);
		}
		return;
	}

	private List<String> pruneTranslations(
	List<String> translations, int maxTranslations, Integer minRequiredPairs)
	throws MachineGeneratedDictException {
		List<String> pruned = new ArrayList<String>();
		if (!translations.isEmpty()) {
			int translationNum = 0;
			String topTranslation = translations.get(0);
			List<String[]> topTranslationExamples = examples4Translation(topTranslation);
			for (String aTranslation: translations) {
				translationNum++;
				List<String[]> aTranslationExamples = examples4Translation(aTranslation);
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
					removeExamples4Translation(aTranslation);
					continue;
				}
			}
		}

		return pruned;
	}

	public void addRelatedWordTranslations(MDictEntry entry) throws MachineGeneratedDictException {
		List<String> relatedWordTranslations = entry.bestTranslations;
		String relWord = entry.word;
		for (String translation: relatedWordTranslations) {
			List<String[]> examplesOfUse = entry.bilingualExamplesOfUse(translation);
			this.addBilingualExamples(translation, examplesOfUse, true, relWord);
		}
	}

	public int totalBilingualExamples() throws MachineGeneratedDictException {
		int total = 0;
		for (String aTranslation: translationExamplesIndex.keySet()) {
			if (aTranslation.equals("ALL")) {
				continue;
			}
			total += translationExamplesIndex.get(aTranslation).size();
		}
		return total;
	}

	public static class TranslationComparator implements java.util.Comparator<String> {

		private final Map<String, List<String[]>> _examplesForTranslation;
		private Set<String> humanTranslations = new HashSet<String>();
		private final Object lang;

		public TranslationComparator(
			String _lang, Map<String,List<String>> word2humanTranslation, Map<String, List<String[]>> _examplesForTranslation) {
			this._examplesForTranslation = _examplesForTranslation;
			for (String l1Word: word2humanTranslation.keySet()) {
				humanTranslations.addAll(word2humanTranslation.get(l1Word));
			}
			this.lang = _lang;
		}

		@Override
		public int compare(String t1, String t2) {
			Logger tLogger = LogManager.getLogger("org.iutools.worddict.MDictEntry.TranslationComparator.compare");
			tLogger.trace("t1="+t1+", t2="+t2);
			int comp = 0;

			// We prefer human translations to those that were generated by the
			// machine
			if (humanTranslations.contains(t1) && !humanTranslations.contains(t2)) {
				tLogger.trace("t1 was human translation but not t2");
				comp = -11;
			} else if (humanTranslations.contains(t2) && !humanTranslations.contains(t1)) {
				tLogger.trace("t2 was human translation but not t1");
				comp = 1;
			}

			if (comp == 0) {
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
				comp = Integer.compare(t2NumEx, t1NumEx);
				tLogger.trace("t1NumEx=" + t1NumEx + ", t2NumEx=" + t2NumEx + ": comp=" + comp);
			}

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
		throws MachineGeneratedDictException {
		Logger logger = LogManager.getLogger("org.iutools.worddict.MDictEntry.ensureScript");
		logger.trace("word="+ word +", script="+script);
		ensureScript_translations(script);
		ensureScript_relatedwords(script);
		ensureScript_BilingualExamples(script);
		ensureScript_word(script);
		if (logger.isTraceEnabled()) {
			logger.trace("upon exit, this="+ PrettyPrinter.print(this));
		}
	}

	private void ensureScript_word(TransCoder.Script script) throws MachineGeneratedDictException {
		try {
			if (lang.equals("iu") && word != null) {
				word = TransCoder.ensureScript(script, word);
				wordInOtherScript = TransCoder.inOtherScript(word);
			}
		} catch (TransCoderException e) {
			throw new MachineGeneratedDictException(e);
		}
	}

	private void ensureScript_translations(TransCoder.Script script) throws MachineGeneratedDictException {
		if (otherLang().equals("iu")) {
			// Input word is en and its translations are iu
			for (List<String> translations:
				new List[] {bestTranslations}) {
				CollectionTranscoder.transcodeList(script, translations);
			}
			for (String enWord: this.translations4l1Word.keySet()) {
				List<String> enWordTranslations = this.translations4l1Word.get(enWord);
				CollectionTranscoder.transcodeList(script, enWordTranslations);
				this.translations4l1Word.put(enWord, enWordTranslations);
			}
		} else {
			// Input word is iu and its translations are en
			try {
				// Transcode keys of the map that provides en translations for each
				// iu related word.
				CollectionTranscoder.transcodeKeys(script, this.translations4l1Word);
			} catch (TransCoderException e) {
				throw new MachineGeneratedDictException(e);
			}
		}
	}

	private void ensureScript_relatedwords(TransCoder.Script script) throws MachineGeneratedDictException {

//		try {
//			for (int ii = 0; ii < relatedWords.length; ii++) {
//				relatedWords[ii] = TransCoder.ensureScript(script, relatedWords[ii]);
//			}
//		} catch (TransCoderException e) {
//			throw new MachineGeneratedDictException(e);
//		}

		CollectionTranscoder.transcodeArray(script, relatedWords);
		return;
	}

	private void ensureScript_BilingualExamples(TransCoder.Script script) throws MachineGeneratedDictException {

		Map<String, List<String[]>>[] alignmentMaps =
			new Map[] {
			translationExamplesIndex
			};
		for (Map<String, List<String[]>> anAlignmentsMap: alignmentMaps) {
			ensureScript_alignmentMap(script, anAlignmentsMap);
		}
	}

	private void ensureScript_alignmentMap(
		TransCoder.Script script, Map<String, List<String[]>> anAlignmentsMap) throws MachineGeneratedDictException {
		Map<String, List<String[]>> convertedMap = new HashMap<String, List<String[]>>();
		for (String translation: anAlignmentsMap.keySet()) {
			String convertedTranslation = translation;
			if (otherLang().equals("iu")) {
				try {
					convertedTranslation = TransCoder.ensureScript(script, translation);
				} catch (TransCoderException e) {
					throw new MachineGeneratedDictException(e);
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
		TransCoder.Script script, String[] alignment) throws MachineGeneratedDictException {
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
			throw new MachineGeneratedDictException(e);
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
			(translationExamplesIndex != null && !translationExamplesIndex.isEmpty()) |
			(bestTranslations != null && !bestTranslations.isEmpty()) |
			(relatedWords != null && relatedWords.length > 0) |
			definition != null) {
			empty = false;
		}

		return empty;
	}
}