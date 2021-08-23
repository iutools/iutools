package org.iutools.worddict;

import ca.nrc.string.StringUtils;
import ca.nrc.testing.*;
import org.iutools.concordancer.tm.WordSpotter;
import org.iutools.linguisticdata.MorphemeHumanReadableDescr;
import org.iutools.script.TransCoder;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;

import java.util.*;

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

	public AssertIUWordDictEntry isForWord(String expWord) throws Exception {
		AssertString.assertStringEquals(
			baseMessage+"\nWord was not as expected",
			expWord, entry().word);
		String expWordOtherScript = TransCoder.inOtherScript(expWord);
		AssertString.assertStringEquals(
			baseMessage+"\nWord in other script was not as expected",
			expWordOtherScript, entry().wordInOtherScript);

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

	public AssertIUWordDictEntry possibleTranslationsSubsetOf(
		String lang, String... expTranslationsArr)
		throws Exception {
		if (expTranslationsArr != null) {
			String[] gotTranslationsArr =
				entry().possibleTranslationsIn(lang).toArray(new String[0]);
			gotTranslationsArr = lowerCaseStrings(gotTranslationsArr);
			expTranslationsArr = lowerCaseStrings(expTranslationsArr);
			new AssertSequence(gotTranslationsArr,
				baseMessage+ "\nList of translations was not a subset of the expected translations")
				.startsWith(expTranslationsArr);
		}
		return this;
	}

	public AssertIUWordDictEntry relatedWordsAre(String... expRelatedWords)
		throws Exception {
		AssertObject.assertDeepEquals(
			baseMessage+"\nRelated words not as expected.",
			expRelatedWords, entry().relatedWords
		);
		return this;
	}

	public void iuIsInScript(TransCoder.Script expScript) {
		IUWordDictEntry entry = this.entry();
		String relatedWords = String.join(", ", entry.relatedWords);
		TransCoder.Script gotScript = TransCoder.textScript(relatedWords);
		Assert.assertEquals(
			baseMessage+"Related words were in the wrong script.\nWords: "+relatedWords,
			expScript, gotScript
			);

		String examples = "";
		if (entry.examplesForOrigWordTranslation != null) {
			List<String[]> iuExamples = entry.examplesForOrigWordTranslation.get("iu");
			if (iuExamples != null) {
				String txtIUExamples = StringUtils.join(iuExamples.iterator(), "\n");
				gotScript = TransCoder.textScript(txtIUExamples);
			Assert.assertEquals(
				baseMessage+"IU examples were in the wrong script.\nWords: "+txtIUExamples,
				expScript, gotScript
				);
			}
		}
	}

	public AssertIUWordDictEntry highlightsAreSubsetOf(
		String lang, String... expHighlightsArr) throws Exception {
		return highlightsAreSubsetOf(lang, (Boolean)null, expHighlightsArr);
	}

	public AssertIUWordDictEntry highlightsAreSubsetOf(
		String lang, Boolean ignoreRepetitions, String... expHighlightsArr)
		throws Exception {

		if (expHighlightsArr != null) {
			Set<String> expHighlights = new HashSet<String>();
			Collections.addAll(expHighlights, expHighlightsArr);
			Set<String> gotHighlights = new HashSet<String>();
			List<String[]> bilingExamples = entry().bilingualExamplesOfUse();
			List<String> langExamples = new ArrayList<String>();
			for (String[] aBilEx : bilingExamples) {
				String text = aBilEx[0];
				if (!lang.equals("iu")) {
					text = aBilEx[1];
				}
				String highlighted = WordSpotter.spotHighlight("strong", text, ignoreRepetitions);
				if (highlighted != null) {
					gotHighlights.add(highlighted);
				}
			}
			AssertSet.isSubsetOf(
				baseMessage + "\nList of highlights was not a subset of the expected highlights",
				lowerCaseStrings(expHighlights), lowerCaseStrings(gotHighlights),
				false);
		}
		return this;
	}

	public AssertIUWordDictEntry atLeastNExamples(Integer expMinExamples) {
		int gotHits = entry().bilingualExamplesOfUse().size();
		Assertions.assertTrue(
			gotHits >= expMinExamples,
			baseMessage+"\nNumber of hits was too low ("+gotHits+" < "+expMinExamples+")");
		return this;
	}


	protected Set<String> lowerCaseStrings(Set<String> orig) {
		Set<String> lowercased = new HashSet<String>();
		for (String anOrig: orig) {
			lowercased.add(anOrig.toLowerCase());
		}
		return lowercased;
	}

	protected String[] lowerCaseStrings(String[] orig) {
		String[] lowercased = new String[orig.length];
		int ii=0;
		for (String anOrig: orig) {
			lowercased[ii] = anOrig.toLowerCase();
			ii++;
		}
		return lowercased;
	}


	public AssertIUWordDictEntry relatedTranslationsStartWith(
		String[] expRelatedTranslationsArr) throws Exception {

		new AssertSequence<String>(
			this.entry().relatedWordTranslations.toArray(new String[0]),
			baseMessage+"\nRelated words translations were not as expected")
		.startsWith(expRelatedTranslationsArr);

		return this;
	}
}
