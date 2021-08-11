package org.iutools.worddict;

import ca.nrc.datastructure.Cloner;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.iutools.linguisticdata.LinguisticData;
import org.iutools.linguisticdata.Morpheme;
import org.iutools.linguisticdata.MorphemeException;
import org.iutools.linguisticdata.MorphemeHumanReadableDescr;
import org.iutools.script.TransCoder;
import org.iutools.script.TransCoderException;

import java.util.*;

public class IUWordDictEntry {

	public static enum Field {
		DEFINITION, BILINGUAL_EXAMPLES, TRANSLATIONS, DECOMP, RELATED_WORDS
	}

	public String word = null;
	public String wordInOtherScript = null;

	public String wordSyllabic;
	public String wordRoman;
	public String definition;
	public List<MorphemeHumanReadableDescr> morphDecomp;

	public Set<String> origWordTranslations = new HashSet<String>();
	private Map<String, List<String>> relatedWordTranslationsMap =
		new HashMap<String,List<String>>();

	// Note: We store sentence pairs as String[] instead of Pair<String,String>
	//   because the latter is jsonified as a dictionary where
	//
	//     - key is the first sentence
	//     - value is the second sentence
	//
	//   and this turns out to be awkward to use on the client-side JavaScript
	//   code.
	public Map<String,List<String[]>> examplesForTranslation
		= new HashMap<String,List<String[]>>();

	public String[] relatedWords = new String[0];

	private List<Pair<String, Double>> _enTranslations = null;

	public List<Pair<String, Double>> enTranslations() {
		if (_enTranslations == null) {
			_enTranslations = new ArrayList<Pair<String, Double>>();
		}
		return _enTranslations;
	}

	public IUWordDictEntry() throws IUWordDictException {
		init_IUWordDictEntry((String)null);
	}

	public IUWordDictEntry(String _word) throws IUWordDictException {
		init_IUWordDictEntry(_word);
	}

	private void init_IUWordDictEntry(String _word) throws IUWordDictException {
		try {
			this.word = _word;
			this.wordInOtherScript = TransCoder.inOtherScript(_word);
			this.wordSyllabic =
				TransCoder.ensureScript(TransCoder.Script.SYLLABIC, _word);
		} catch (TransCoderException e) {
			throw new IUWordDictException(e);
		}
		this.wordRoman =
		TransCoder.ensureRoman(_word);
	}

	public void setDecomp(String[] morphemes) throws IUWordDictException {
		morphDecomp = new ArrayList<MorphemeHumanReadableDescr>();
		for (String morpheme: morphemes) {
			Morpheme morphInfo = LinguisticData.getInstance().getMorpheme(morpheme);
			if (morphInfo == null) {
				continue;
			}
			try {
				morphDecomp.add(
					new MorphemeHumanReadableDescr(
						morphInfo.id, morphInfo.englishMeaning));
			} catch (MorphemeException e) {
				throw new IUWordDictException(e);
			}
		}
	}

	public IUWordDictEntry addBilingualExample(
		String translation, String[] example, boolean forRelatedWord) {
		Logger tLogger = Logger.getLogger("org.iutools.worddict.IUWordDictEntry.addBilingualExample");
		tLogger.trace("translation="+translation);
		if (forRelatedWord) {
//			this.relatedWordTranslations.put(translation);
		} else {
			this.origWordTranslations.add(translation);
		}
		List<String[]> currentExamples = examplesForTranslation.get(translation);
		if (currentExamples == null) {
			currentExamples = new ArrayList<String[]>();
		}
		currentExamples.add(example);
		examplesForTranslation.put(translation, currentExamples);
		tLogger.trace("returning");

		return this;
	}

	private void addBilingualExamples(
		String translation, List<String[]> examples, Boolean forRelatedWord) {
		for (String[] anExample: examples) {
			addBilingualExample(translation, anExample, forRelatedWord);
		}
	}


	public List<String[]> bilingualExamplesOfUse() {
		List<String[]> allExamples = new ArrayList<String[]>();
		for (String translation: examplesForTranslation.keySet()) {
			allExamples.addAll(examplesForTranslation.get(translation));
		}
		return allExamples;
	}

	public List<String[]> bilingualExamplesOfUse(String translation) {
		List<String[]> examples = new ArrayList<String[]>();
		if (examplesForTranslation.containsKey(translation)) {
			examples = examplesForTranslation.get(translation);
		}

		return examples;
	}

	public void ensureIUScript(TransCoder.Script script) throws IUWordDictException {
		try {
			for (int ii = 0; ii < relatedWords.length; ii++) {
				relatedWords[ii] = TransCoder.ensureScript(script, relatedWords[ii]);
			}

			for (String translation : this.examplesForTranslation.keySet()) {
				List<String[]> examples = this.examplesForTranslation.get(translation);
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
			throw new IUWordDictException(e);
		}
		return;
	}

	public Set<String> possibleTranslationsIn(String lang) throws IUWordDictException {
		if (!lang.equals("en")) {
			throw new IUWordDictException(
				"Translations are currently only available for 'en'");
		}
		new HashMap<String,String>();
		Set<String> translations = examplesForTranslation.keySet();
		translations.remove("ALL");
		translations.remove("MISC");

		return translations;
	}

	public void addRelatedWordTranslations(IUWordDictEntry entry) throws IUWordDictException {
		String relatedWord = entry.word;
		Set<String> relatedWordTranslations = entry.possibleTranslationsIn("en");
		for (String translation: relatedWordTranslations) {
			List<String[]> examplesOfUse = entry.bilingualExamplesOfUse(translation);
//			this.addBilingualExamples(translation, examplesOfUse, true);
		}
	}

	public List<List<String>> relatedWordTranslations() throws IUWordDictException {
		List<List<String>> translationsInfo = new ArrayList<List<String>>();
		for (String aTranslation: relatedWordTranslationsMap.keySet()) {
			List<String> relWordTranslInfo = null;
			try {
				relWordTranslInfo = Cloner.clone(relatedWordTranslationsMap.get(aTranslation));
			} catch (Cloner.ClonerException e) {
				throw new IUWordDictException(e);
			}
			relWordTranslInfo.add(0, aTranslation);
			translationsInfo.add(relWordTranslInfo);
		}

		// Sort the related word translations by the number of bilingual examples
		// they apply to


		return translationsInfo;
	}


	public int totalBilingualExamples() {
		int total = bilingualExamplesOfUse("ALL").size();
		return total;
	}
}