package org.iutools.worddict;

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
	public String wordSyllabic;
	public String wordRoman;
	public String definition;
	public List<MorphemeHumanReadableDescr> morphDecomp;

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
		String translation, String[] example) {
		Logger tLogger = Logger.getLogger("org.iutools.worddict.IUWordDictEntry.addBilingualExample");
		tLogger.trace("translation='"+translation+"'");
		if (!examplesForTranslation.containsKey(translation)) {
			examplesForTranslation.put(
				translation, new ArrayList<String[]>());
		}
		examplesForTranslation.get(translation).add(example);
		return this;
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
}