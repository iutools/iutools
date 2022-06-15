package org.iutools.worddict;

import ca.nrc.dtrc.elasticsearch.Document;
import ca.nrc.json.PrettyPrinter;
import ca.nrc.string.StringUtils;
import ca.nrc.testing.*;
import org.iutools.concordancer.tm.WordSpotter;
import org.iutools.linguisticdata.MorphemeHumanReadableDescr;
import org.iutools.script.TransCoder;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;

import java.util.*;

public class AssertMultilingualDictEntry extends Asserter<MultilingualDictEntry> {

	public AssertMultilingualDictEntry(MultilingualDictEntry _gotObject) {
		super(_gotObject);
	}

	public AssertMultilingualDictEntry(MultilingualDictEntry _gotObject, String mess) {
		super(_gotObject, mess);
	}

	MultilingualDictEntry entry() {
		return (MultilingualDictEntry)gotObject;
	}

	private Set<String> entryRelatedWords() {
		Set<String> words = new HashSet<String>();
		for (String aWord: entry().relatedWords) {
			words.add(Document.removeType(aWord));
		}
		return words;
	}

	public AssertMultilingualDictEntry isForWord(String expWord) throws Exception {
		AssertString.assertStringEquals(
			baseMessage+"\nWord was not as expected",
			expWord, entry().word);

		return this;
	}


	public AssertMultilingualDictEntry gaveEmptyWordEntry() {
		Assertions.assertTrue(entry().isEmpty());
		return this;
	}

	public AssertMultilingualDictEntry definitionEquals(String expDef) {
		AssertString.assertStringEquals(expDef, entry().definition);
		return this;
	}

	public AssertMultilingualDictEntry decompositionIs(String... expMorphemes)
		throws Exception {
		List<String> gotDecomp = new ArrayList<String>();
		if (null != entry().morphDecomp) {
			gotDecomp = new ArrayList<String>();
			for (MorphemeHumanReadableDescr morphDescr : entry().morphDecomp) {
				gotDecomp.add(morphDescr.id);
			}
		}
		AssertObject.assertDeepEquals(
			baseMessage+"\nDecomposition not as expected",
			expMorphemes, gotDecomp);
		return this;
	}


	public AssertMultilingualDictEntry bestTranslationsAre(
		String... expTranslationsArr) throws Exception {

		if (expTranslationsArr != null) {
			String otherLang = entry().otherLang();
			List<String> gotTranslations = entry().bestTranslations();
			String[] gotTranslationsArr = gotTranslations.toArray(new String[0]);
			gotTranslationsArr = lowerCaseStrings(gotTranslationsArr);
			expTranslationsArr = lowerCaseStrings(expTranslationsArr);

			// For some reason, the order of the best translations can be
			// unpredictable. So compare the two arrays as sets
			Set<String> gotTranslationsSet = new HashSet<String>();
			Collections.addAll(gotTranslationsSet, gotTranslationsArr);
			Set<String> expTranslationsSet = new HashSet<String>();
			Collections.addAll(expTranslationsSet, expTranslationsArr);
			new AssertSet(gotTranslationsSet,
				baseMessage+ "\nList of translations did not contain the expected translations")
				.assertEqual(expTranslationsSet);
		}
		return this;
	}

	public AssertMultilingualDictEntry relatedWordsIsSubsetOf(
		String... expRelatedWordsArr)
		throws Exception {

		Set<String> gotRelatedWordsSet = entryRelatedWords();
		Set<String> expRelatedWordsSuperset = new HashSet<String>();
		Collections.addAll(expRelatedWordsSuperset, expRelatedWordsArr);

		new AssertSet(gotRelatedWordsSet, "\nRelated words not as expected.")
			.isSubsetOf(expRelatedWordsArr);

		return this;
	}


	public void iuIsInScript(TransCoder.Script expScript) {
		MultilingualDictEntry entry = this.entry();
		String l1 = entry.lang;
		String relatedWords = String.join(", ", entry.relatedWords);
		TransCoder.Script gotScript = TransCoder.textScript(relatedWords);
		Assert.assertEquals(
			baseMessage+"Related words were in the wrong script.\nWords: "+relatedWords,
			expScript, gotScript
			);

		String examples = "";
		if (entry.examplesForOrigWordTranslation != null) {
			List<String[]> iuExamples = entry.examplesForOrigWordTranslation.get(l1);
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

	public AssertMultilingualDictEntry highlightsAreSubsetOf(
		String lang, String... expHighlightsArr) throws Exception {
		return highlightsAreSubsetOf(lang, (Boolean)null, expHighlightsArr);
	}

	public AssertMultilingualDictEntry highlightsAreSubsetOf(
		String lang, Boolean ignoreRepetitions, String... expHighlightsArr)
		throws Exception {

		if (expHighlightsArr != null) {
			String l1 = entry().lang;
			Set<String> expHighlights = new HashSet<String>();
			Collections.addAll(expHighlights, expHighlightsArr);
			Set<String> gotHighlights = new HashSet<String>();
			List<String[]> bilingExamples = entry().bilingualExamplesOfUse();
			List<String> langExamples = new ArrayList<String>();
			for (String[] aBilEx : bilingExamples) {
				String text = aBilEx[0];
				if (!lang.equals(l1)) {
					text = aBilEx[1];
				}
				String highlighted = WordSpotter.spotHighlight("strong", text, ignoreRepetitions);
				highlighted = MultilingualDict.canonizeTranslation(lang, highlighted);
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

	public AssertMultilingualDictEntry atLeastNExamples(Integer expMinExamples)
		throws Exception {
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


	public AssertMultilingualDictEntry relatedTranslationsStartWith(
		String[] expRelatedTranslationsArr) throws Exception {

		new AssertSequence<String>(
			this.entry().relatedWordTranslations.toArray(new String[0]),
			baseMessage+"\nRelated words translations were not as expected")
		.startsWith(expRelatedTranslationsArr);

		return this;
	}

	public AssertMultilingualDictEntry relatedTranslationsMapsEquals(
		Map<String, List<String>> expRelatedTranslationsMap) throws Exception {
		Map<String, List<String>> gotTranslMap = this.entry().relatedWordTranslationsMap;
		AssertObject.assertDeepEquals(
			baseMessage+"\nRelated word translations map not as expected",
			expRelatedTranslationsMap, gotTranslMap
		);
		return this;
	}


	public AssertMultilingualDictEntry langIs(String expLang) {
		AssertString.assertStringEquals(
			baseMessage+"\nLanguage of entry not as expected",
			expLang, entry().lang
		);
		return this;
	}

	public AssertMultilingualDictEntry checkWordInOtherScript(String origWord)
		throws Exception {
		String expWordOtherScript = TransCoder.inOtherScript(origWord);
		AssertString.assertStringEquals(
			baseMessage+"\nWord in other script was not as expected",
			expWordOtherScript, entry().wordInOtherScript);

		return this;
	}

	public AssertMultilingualDictEntry translationsAreNonEmptySubsetOf(
		String[] expTranslationsSuperset) {
		Set<String> gotTranslations = new HashSet<String>();
		gotTranslations.addAll(entry().origWordTranslations);
		if (gotTranslations.isEmpty()) {
			// We only include related word translations if there were no
			// translations for the original word
			gotTranslations.addAll(entry().relatedWordTranslations);
		}
		Assertions.assertTrue(gotTranslations.size() > 0,
			"List of translations should NOT have been empty");
		new AssertSet(gotTranslations,
			baseMessage+"\nList of translations did not contain the expected translations")
			.isSubsetOf(expTranslationsSuperset);

		gotTranslations = new HashSet<String>();
		gotTranslations.addAll(entry().examplesForOrigWordTranslation.keySet());
		if (gotTranslations.isEmpty()) {
			// We only include examples for related word translations if there were no
			// translations for the original word
			gotTranslations.addAll(entry().examplesForRelWordsTranslation.keySet());
		}
		gotTranslations.remove("ALL");
		new AssertSet(gotTranslations,
			baseMessage+"\nList of keys for translation did not contain the expected translations")
			.isSubsetOf(expTranslationsSuperset);

		return this;
	}
}
