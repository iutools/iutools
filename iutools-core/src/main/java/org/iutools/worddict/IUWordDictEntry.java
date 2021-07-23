package org.iutools.worddict;

import org.apache.commons.lang3.tuple.Pair;
import org.iutools.linguisticdata.LinguisticData;
import org.iutools.linguisticdata.Morpheme;
import org.iutools.linguisticdata.MorphemeException;
import org.iutools.linguisticdata.MorphemeHumanReadableDescr;
import org.iutools.script.TransCoder;
import org.iutools.script.TransCoderException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IUWordDictEntry {
	public String wordSyllabic;
	public String wordRoman;
	public String definition;
	public List<MorphemeHumanReadableDescr> morphDecomp;
	private Map<String,List<Pair<String,String>>> examplesForTranslation
		= new HashMap<String,List<Pair<String,String>>>();

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
		String translation, Pair<String,String> example) {
		if (!examplesForTranslation.containsKey(translation)) {
			examplesForTranslation.put(
				translation, new ArrayList<Pair<String,String>>());
		}
		examplesForTranslation.get(translation).add(example);
		return this;
	}

	public List<Pair<String, String>> bilingualExamplesOfUse() {
		return bilingualExamplesOfUse("ALL");
	}

	public List<Pair<String, String>> bilingualExamplesOfUse(String translation) {
		List<Pair<String, String>> examples = new ArrayList<Pair<String, String>>();
		if (examplesForTranslation.containsKey(translation)) {
			examples = examplesForTranslation.get(translation);
		}

		return examples;
	}

}