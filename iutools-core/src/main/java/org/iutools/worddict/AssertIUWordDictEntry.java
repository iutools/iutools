package org.iutools.worddict;

import ca.nrc.testing.AssertObject;
import ca.nrc.testing.AssertSequence;
import ca.nrc.testing.AssertString;
import ca.nrc.testing.Asserter;
import org.apache.commons.lang3.tuple.Pair;
import org.iutools.linguisticdata.MorphemeHumanReadableDescr;

import java.util.ArrayList;
import java.util.List;

public class AssertIUWordDictEntry extends Asserter<IUWordDictEntry> {

	public AssertIUWordDictEntry(IUWordDictEntry _gotObject) {
		super(_gotObject);
	}

	public AssertIUWordDictEntry(IUWordDictEntry _gotObject, String mess) {
		super(_gotObject, mess);
	}

	IUWordDictEntry entry() {
		return (IUWordDictEntry)gotObject;
	}

	public AssertIUWordDictEntry romanWordIs(String expWord) {
		AssertString.assertStringEquals(
			baseMessage+"\nRoman form of word was not as expected",
			entry().wordRoman, expWord);
		return this;
	}

	public AssertIUWordDictEntry syllabicWordIs(String expWord) {
		AssertString.assertStringEquals(
			baseMessage+"\nRoman form of word was not as expected",
		entry().wordSyllabic, expWord);
		return this;

	}


	public AssertIUWordDictEntry definitionEquals(String expDef) {
		AssertString.assertStringEquals(expDef, entry().definition);
		return this;
	}

	public AssertIUWordDictEntry decompositionIs(String... expMorphemes)
		throws Exception {
		List<String> gotMorphemes = null;
		if (null != entry().morphDecomp) {
			gotMorphemes = new ArrayList<String>();
			for (MorphemeHumanReadableDescr morphDescr : entry().morphDecomp) {
				gotMorphemes.add(morphDescr.id);
			}
		}
		AssertObject.assertDeepEquals(
			baseMessage+"\nDecomposition not as expected",
			expMorphemes, gotMorphemes);
		return this;
	}

	public AssertIUWordDictEntry possibleTranslationsAre(String... expTranslations)
		throws Exception {
		String[] gotTranslations = new String[entry().enTranslations().size()];
		int ii = 0;
		for (Pair<String,Double> scoredTranslation: entry().enTranslations()) {
			gotTranslations[ii] = scoredTranslation.getLeft();
			ii++;
		}
		AssertObject.assertDeepEquals(
			baseMessage+"\nList of possible translations was not as expected",
			expTranslations, gotTranslations
		);
		return this;
	}

	public AssertIUWordDictEntry bilingualExamplesStartWith(
		String[]... expExamples) throws Exception {
		List<String[]> gotExamples = entry().bilingualExamplesOfUse();
		String[][] gotExamplesArr =
			gotExamples.toArray(new String[0][]);
		new AssertSequence(gotExamplesArr)
			.startsWith(expExamples);
		return this;
	}
}
