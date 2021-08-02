package org.iutools.worddict;

import ca.nrc.string.StringUtils;
import ca.nrc.testing.*;
import org.iutools.linguisticdata.MorphemeHumanReadableDescr;
import org.iutools.script.TransCoder;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	public AssertIUWordDictEntry possibleTranslationsAreIn(
		String lang, String... expTranslationsArr)
		throws Exception {
		Set<String> gotTranslations = entry().possibleTranslationsIn(lang);
		Set<String> expTranslations = new HashSet<String>();
		Collections.addAll(expTranslations, expTranslationsArr);
		AssertSet.isSubsetOf(
			baseMessage+"\nList of translations was not a subset of the expected translations",
			expTranslations, gotTranslations, false);
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
		if (entry.examplesForTranslation != null) {
			List<String[]> iuExamples = entry.examplesForTranslation.get("iu");
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

	public AssertIUWordDictEntry highlightsAre(
		String lang, String... expHighlights) throws Exception {
		Set<String> gotHighlights = new HashSet<String>();
		List<String[]> bilingExamples = entry().bilingualExamplesOfUse();
		List<String> langExamples = new ArrayList<String>();
		Pattern pattHighlight = Pattern.compile("<strong>([^<]*)</strong>");
		for (String[] aBilEx: bilingExamples) {
			String text = aBilEx[0];
			if (!lang.equals("iu")) {
				text = aBilEx[1];
			}
			Matcher matcher = pattHighlight.matcher(text);
			while (matcher.find()) {
				gotHighlights.add(matcher.group(1).toLowerCase());
			}
		}
		AssertObject.assertDeepEquals(
			baseMessage+"\nHighglights were not as expected for language "+lang,
			expHighlights, gotHighlights
		);
		return this;
	}

	public AssertIUWordDictEntry atLeastNExamples(Integer expMinExamples) {
		int gotHits = entry().bilingualExamplesOfUse().size();
		Assertions.assertTrue(
			gotHits >= expMinExamples,
			baseMessage+"\nNumber of hits was too low ("+gotHits+" < "+expMinExamples+")");
		return this;
	}
}
