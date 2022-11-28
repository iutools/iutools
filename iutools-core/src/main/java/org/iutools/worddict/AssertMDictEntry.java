package org.iutools.worddict;

import ca.nrc.dtrc.elasticsearch.Document;
import ca.nrc.string.StringUtils;
import ca.nrc.testing.*;
import org.iutools.concordancer.tm.WordSpotter;
import org.iutools.linguisticdata.MorphemeHumanReadableDescr;
import org.iutools.script.TransCoder;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.util.*;

public class AssertMDictEntry extends Asserter<MDictEntry> {

	public AssertMDictEntry(MDictEntry _gotObject) {
		super(_gotObject);
		init__AssertMDictEntry();
	}

	public AssertMDictEntry(MDictEntry _gotObject, String mess) {
		super(_gotObject, mess);
		init__AssertMDictEntry();
	}

	private void init__AssertMDictEntry()  {
		assertSanityCheck();
	}

	private void assertSanityCheck()  {
		try {
			Assertions.assertTrue(
				entry().getLang() != null,
				baseMessage + "\nentry.lang should NOT have been null");
			Assertions.assertTrue(
				entry().otherLang() != null,
				baseMessage + "\nentry.otherLang() should NOT have been null");

			Assertions.assertTrue(
				entry().getWord() != null,
				baseMessage + "\nentry.word should NOT have been null");
			if (entry().getLang().equals("iu")) {
				Assertions.assertTrue(
				entry().getWordInOtherScript() != null,
				baseMessage + "\nentry.wordInOtherScript should NOT have been null");
				Assertions.assertTrue(
				entry().getWordRoman() != null,
				baseMessage + "\nentry.wordRoman should NOT have been null");
				Assertions.assertTrue(
				entry().getWordSyllabic() != null,
				baseMessage + "\nentry.wordSyllabic should NOT have been null");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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

	public AssertMDictEntry isForWord(String expWord) throws Exception {
		AssertString.assertStringEquals(
			baseMessage + "\nWord was not as expected",
			expWord, entry().getWord());

		return this;
	}

	public AssertMDictEntry wordInOtherScriptIs(String expWord) {
		AssertString.assertStringEquals(
			baseMessage + "\nWord in other script was not as expected",
			expWord, entry().getWordInOtherScript());
		return this;
	}


	public AssertMDictEntry wordRomanIs(String expWord) {
		AssertString.assertStringEquals(
			baseMessage + "\nRoman word was not as expected",
			expWord, entry().getWordRoman());
		return this;
	}
	public AssertMDictEntry wordSyllIs(String expWord) {
		AssertString.assertStringEquals(
			baseMessage + "\nSyllabic word was not as expected",
			expWord, entry().getWordSyllabic());
		return this;
	}


	public AssertMDictEntry gaveEmptyWordEntry() {
		Assertions.assertTrue(entry().isEmpty());
		return this;
	}

	public AssertMDictEntry definitionEquals(String expDef) {
		AssertString.assertStringEquals(expDef, entry().definition);
		return this;
	}

	public AssertMDictEntry decompositionIs(String... expMorphemes)
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

	public AssertMDictEntry hasAtLeastNTranslations(int expMinTranslations) {
		AssertNumber.isGreaterOrEqualTo(
			"Entry did not have the expected minimum number of translations",
			entry().bestTranslations.size(), expMinTranslations);
		return this;
	}

	public AssertMDictEntry hasTranslationsForOrigWord(boolean expHasTranslationsForOrigWord) {
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


	public AssertMDictEntry bestTranslationsStartWith(String mess, String... expTopTranslations)
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

	public AssertMDictEntry humanTranslationsAre(String... expHumanTranslations) throws IOException {
		Set<String> humanTranslations = new HashSet<String>();
		for (String l1Word: entry().humanTranslations.keySet()) {
			humanTranslations.addAll(entry().humanTranslations.get(l1Word));
		}
		AssertSet.assertEquals(
			baseMessage+"\nHuman translations not as expected",
			expHumanTranslations, humanTranslations);

		return this;
	}

	public AssertMDictEntry bestTranslationsAreAmong(
		String... possibleTranslations) throws Exception {
		return bestTranslationsAreAmong((Set<String>)null, possibleTranslations);
	}


	public AssertMDictEntry bestTranslationsAreAmong(
		Set<String> translationWithoutExamples, String... possibleTranslations) throws Exception {

		if (translationWithoutExamples == null) {
			translationWithoutExamples = new HashSet<String>();
		}
		if (possibleTranslations != null) {
			translationsSanityCheck(translationWithoutExamples);
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

	public AssertMDictEntry relatedWordsIsSubsetOf(
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
		String l1 = entry.getLang();
		String relatedWords = String.join(", ", entry.relatedWords);
		TransCoder.Script gotScript = TransCoder.textScript(relatedWords);
		Assert.assertEquals(
			baseMessage+"Related words were in the wrong script.\nWords: "+relatedWords,
			expScript, gotScript
			);

		String examples = "";
		if (entry.translationExamplesIndex != null) {
			List<String[]> iuExamples = entry.translationExamplesIndex.get(l1);
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

	public AssertMDictEntry highlightsAreSubsetOf(
		String lang, String... expHighlightsArr) throws Exception {
		return highlightsAreSubsetOf(lang, (Boolean)null, expHighlightsArr);
	}

	public AssertMDictEntry highlightsAreSubsetOf(
		String lang, Boolean ignoreRepetitions, String... expHighlightsArr)
		throws Exception {

		if (expHighlightsArr != null) {
			String l1 = entry().getLang();
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

	public AssertMDictEntry atLeastNExamples(Integer expMinExamples)
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

	public AssertMDictEntry langIs(String expLang) {
		AssertString.assertStringEquals(
			baseMessage+"\nLanguage of entry not as expected",
			expLang, entry().getLang()
		);
		return this;
	}

	public AssertMDictEntry checkWordInOtherScript(String origWord)
		throws Exception {
		String expWordOtherScript = TransCoder.inOtherScript(origWord);
		AssertString.assertStringEquals(
			baseMessage+"\nWord in other script was not as expected",
			expWordOtherScript, entry().getWordInOtherScript());

		return this;
	}

	public AssertMDictEntry translationsAreNonEmptySubsetOf(
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

	public AssertMDictEntry translationsSanityCheck(Set<String> translationWithoutExamples) throws Exception {
		String[] words = new String[] {entry().getWord()};
		List<String> translations = entry().bestTranslations;

		for (String aTranslation: translations) {
			List<String[]> aTranslExamples = entry().examples4Translation(aTranslation);
			String mess = "(aTranslation="+aTranslation+")";
			Assertions.assertTrue(aTranslExamples != null,
				"Examples for translation should not have been null "+mess
				);
			if (!translationWithoutExamples.contains(aTranslation)) {
				Assertions.assertTrue(!aTranslExamples.isEmpty(),
				"Examples for translation should not have been empty " + mess
				);
			}
		}

		return this;
	}
}
