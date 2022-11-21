package org.iutools.worddict;

import ca.nrc.dtrc.elasticsearch.Document;
import ca.nrc.string.StringUtils;
import ca.nrc.testing.*;
import org.iutools.concordancer.tm.WordSpotter;
import org.iutools.linguisticdata.MorphemeHumanReadableDescr;
import org.iutools.worddict.MachineGeneratedDict;
import org.iutools.script.TransCoder;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;

import java.util.*;

public class AssertMultilingualDictEntry extends Asserter<MDictEntry> {

	public AssertMultilingualDictEntry(MDictEntry _gotObject) {
		super(_gotObject);
	}

	public AssertMultilingualDictEntry(MDictEntry _gotObject, String mess) {
		super(_gotObject, mess);
	}

	MDictEntry entry() {
		return (MDictEntry) gotObject;
	}

	private Set<String> entryRelatedWords() {
		Set<String> words = new HashSet<String>();
		for (String aWord : entry().relatedWords) {
			words.add(Document.removeType(aWord));
		}
		return words;
	}

	public AssertMultilingualDictEntry isForWord(String expWord) throws Exception {
		AssertString.assertStringEquals(
		baseMessage + "\nWord was not as expected",
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
		baseMessage + "\nDecomposition not as expected",
		expMorphemes, gotDecomp);
		return this;
	}

	public AssertMultilingualDictEntry hasAtLeastNTranslations(int expMinTranslations) {
		AssertNumber.isGreaterOrEqualTo(
			"Entry did not have the expected minimum number of translations",
			entry().bestTranslations.size(), expMinTranslations);
		return this;
	}

	public AssertMultilingualDictEntry hasTranslationsForOrigWord(boolean expHasTranslationsForOrigWord) {
		boolean hasOrigTransl = entry().hasTranslationsForOriginalWord();
		if (expHasTranslationsForOrigWord) {
			Assertions.assertTrue(
				hasOrigTransl,
				baseMessage+"\nEntry SHOULD have had translations for original word");
		} else {
			Assertions.assertFalse(
				hasOrigTransl,
				baseMessage+"\nEntry should NOT have had translations for original word");

		}

		return this;
	}


	public AssertMultilingualDictEntry bestTranslationsStartWith(String mess, String... expTopTranslations)
		throws Exception {
		List<String> gotTopTranslations = entry().bestTranslations;
		gotTopTranslations = gotTopTranslations
			.subList(0, Math.min(expTopTranslations.length, gotTopTranslations.size()));
		AssertObject.assertDeepEquals(
			mess + "\nTop translations were not as expected",
			expTopTranslations, gotTopTranslations
		);
		return this;
	}

	public AssertMultilingualDictEntry humanTranslationsAre(String... expHumanTranslations) {
		Assertions.fail("Implement this assertion");
		return this;
	}

	public AssertMultilingualDictEntry bestTranslationsAreAmong(
		String... possibleTranslations) throws Exception {

		if (possibleTranslations != null) {
			translationsSanityCheck();
			String otherLang = entry().otherLang();
			List<String> gotTranslations = entry().bestTranslations;
			String[] gotTranslationsArr = gotTranslations.toArray(new String[0]);
			gotTranslationsArr = lowerCaseStrings(gotTranslationsArr);
			possibleTranslations = lowerCaseStrings(possibleTranslations);

			// For some reason, the order of the best translations can be
			// unpredictable. So compare the two arrays as sets
			Set<String> gotTranslationsSet = new HashSet<String>();
			Collections.addAll(gotTranslationsSet, gotTranslationsArr);
			new AssertSet(gotTranslationsSet,
				baseMessage+ "\nTranslations were not among the list of possible translations")
				.isSubsetOf(possibleTranslations);
		}
		return this;
	}

	public AssertMultilingualDictEntry relatedWordsIsSubsetOf(
		String... expRelatedWordsArr)
		throws Exception {

		if (expRelatedWordsArr != null) {
			Set<String> gotRelatedWordsSet = entryRelatedWords();
			Set<String> expRelatedWordsSuperset = new HashSet<String>();
			Collections.addAll(expRelatedWordsSuperset, expRelatedWordsArr);

			new AssertSet(gotRelatedWordsSet, "\nRelated words not as expected.")
			.isSubsetOf(expRelatedWordsArr);
		}

		return this;
	}


	public void iuIsInScript(TransCoder.Script expScript) {
		MDictEntry entry = this.entry();
		String l1 = entry.lang;
		String relatedWords = String.join(", ", entry.relatedWords);
		TransCoder.Script gotScript = TransCoder.textScript(relatedWords);
		Assert.assertEquals(
			baseMessage+"Related words were in the wrong script.\nWords: "+relatedWords,
			expScript, gotScript
			);

		String examples = "";
		if (entry.examples4Translation != null) {
			List<String[]> iuExamples = entry.examples4Translation.get(l1);
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
				highlighted = MachineGeneratedDict.canonizeTranslation(lang, highlighted);
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
		gotTranslations.addAll(entry().bestTranslations);
		Assertions.assertTrue(gotTranslations.size() > 0,
			"List of translations should NOT have been empty");
		new AssertSet(gotTranslations,
			baseMessage+"\nList of translations did not contain the expected translations")
			.isSubsetOf(expTranslationsSuperset);

		return this;
	}

	public AssertMultilingualDictEntry translationsSanityCheck() throws Exception {
		String[] words = new String[] {entry().word};
		List<String> translations = entry().bestTranslations;
		Map<String, List<String[]>> examples = entry().examples4Translation;

		for (String aTranslation: translations) {
			List<String[]> aTranslExamples = examples.get(aTranslation);
			String mess = "(aTranslation="+aTranslation+")";
			Assertions.assertTrue(aTranslExamples != null,
				"Examples for translation should not have been null "+mess
				);
			Assertions.assertTrue(!aTranslExamples.isEmpty(),
				"Examples for translation should not have been empty "+mess
			);
		}

		return this;
	}
}
