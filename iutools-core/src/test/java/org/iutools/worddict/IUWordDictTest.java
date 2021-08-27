package org.iutools.worddict;

import ca.nrc.testing.AssertSequence;
import org.apache.commons.lang3.tuple.Pair;
import org.iutools.linguisticdata.MorphemeHumanReadableDescr;
import org.iutools.script.TransCoder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IUWordDictTest {

	IUWordDictCase[] cases = null;

	@BeforeEach
	public void setUp() {
		cases = new IUWordDictCase[]{

		new IUWordDictCase("ammuumajuqsiuqtutik")
			.setDecomp(
				"ammut/1a", "u/1nv", "ma/1vv", "juq/1vn", "siuq/1nv",
				"jusik/tv-ger-2d")
			.setOrigWordTranslations(
				"clam diving", "clam ... clams", "diving ... clams")
			.setMinExamples(3)
			.setRelatedWords(
				"ammuumajurniartiit", "ammuumajuqtarnirmut",
				"ammuumajuqtaqtiit", "ammuumajuqtaqtutik",
				"ammuumajurniarnirmut"),

		new IUWordDictCase("ᐊᒻᒨᒪᔪᖅᓯᐅᖅᑐᑎᒃ")
			.setDecomp(
				"ammut/1a", "u/1nv", "ma/1vv", "juq/1vn", "siuq/1nv",
				"jusik/tv-ger-2d")
			.setOrigWordTranslations(
				"clam diving", "clam ... clams", "diving ... clams")
			.setMinExamples(3)
			.setRelatedWords(
				"ᐊᒻᒨᒪᔪᕐᓂᐊᕐᑏᑦ", "ᐊᒻᒨᒪᔪᖅᑕᕐᓂᕐᒧᑦ", "ᐊᒻᒨᒪᔪᖅᑕᖅᑏᑦ", "ᐊᒻᒨᒪᔪᖅᑕᖅᑐᑎᒃ",
				"ᐊᒻᒨᒪᔪᕐᓂᐊᕐᓂᕐᒧᑦ"),

		// This is an out of vocabulary word
		new IUWordDictCase("inuksssuk")
			.setOutOfVocab(true)
			.setOrigWordTranslations(new String[]{})
			.setMinExamples(0)
			.setRelatedWords(new String[]{}),

		// This word has a sentence pair whose word alignments are
		// faulty. Make sure it does not crash.
		new IUWordDictCase("umiarjuakkut")
			.setRelatedWords(
				"umiarjuanut", "umiarjuat", "umiarjuaq", "umiarjuarmut",
				"umiarjualirijikkut")
			.setMinExamples(5)
			.setOrigWordTranslations(new String[]{
				"sea", "ship", "shipping", "resupply ... dry ... cargo",
				"sealift arrives ... sealift",}),

		new IUWordDictCase("kiugavinnga")
			.setRelatedWords(
				"kiujjutit", "kiujjutik", "kiuvan", "kiujjutinga", "kiulugu")
			.setOrigWordTranslations(new String[]{
				"response", "for that answer", "for your answer",
				"for that response", "for ... response"}),

		new IUWordDictCase("najugaq")
			.setRelatedWords(
				"najugangani", "najugaujunut", "najuganga", "najugaujumi",
				"najugauvattunut")
			.setRelWordTranslationsStartWith(new String[] {"site", "homes", "centre"})
		};
	}

	//////////////////////////////////
	// DOCUMENTATION TESTS
	//////////////////////////////////

	@Test
	public void test__IUWordDict__Synopsis() throws Exception {
		// The dictionary is a singleton
		IUWordDict dict = IUWordDict.getInstance();

		// Given an inuktitut word, you can get its dictionary entry
		IUWordDictEntry entry = dict.entry4word("inuksuk");

		// The input word can be in latin or syllabic alphabet
		entry = dict.entry4word("ᐃᓄᒃᓱᒃ");

		// The entry contains a bunch of information about the word

		// Definition (may be null)
		String definition = entry.definition;

		// Morphological decomposition in human-readable form
		List<MorphemeHumanReadableDescr> decomp = entry.morphDecomp;

		// List of possible English translations with scores.
		for (Pair<String,Double> scoredTranslations: entry.enTranslations()) {
			String translation = scoredTranslations.getLeft();
			Double score = scoredTranslations.getRight();

			// For each possible translation, you can get a list of bilingual
			// sentences that use that particular translation
			List<String[]> examples = entry.bilingualExamplesOfUse(translation);
		}

		// You can also get bilingual examples of use for all of the possible
		// translations
		List<String[]> examples = entry.bilingualExamplesOfUse();

		// You can search for a word
		String partialWord = "inuksh";
		Pair<Iterator<String>, Long> result = dict.search(partialWord);
		// Total number of matching words
		Long totalWords = result.getRight();
		Iterator<String> wordsIter = result.getLeft();
		// Iterate through the matching words...
		while (wordsIter.hasNext()) {
			String matchingWord = wordsIter.next();
		}
	}


	//////////////////////////////////
	// VERIFICATION TESTS
	//////////////////////////////////

	@Test
	public void test__entry4word__IUIsInScriptOfInputWord() throws Exception {
		for (Pair<String, TransCoder.Script> aCase:
			new Pair[] {
				Pair.of("inuksuk", TransCoder.Script.ROMAN),
				Pair.of("ᐃᓄᒃᓱᒃ", TransCoder.Script.SYLLABIC),
			}) {

			IUWordDictEntry entry =
				IUWordDict.getInstance().entry4word(aCase.getLeft());
			new AssertIUWordDictEntry(entry)
				.iuIsInScript(aCase.getRight())
			;

		}
	}

	@Test
	public void test__entry4word__VariousCases()
		throws Exception {

		Integer focusOnCase = null;
//		focusOnCase = 0;

		boolean verbose = false;

		int caseNum = -1;
		for (IUWordDictCase aCase: cases) {
			caseNum++;
			if (verbose) {
				System.out.println("test__entry4word__VariousCases: case #"+caseNum+": "+aCase.id());
			}
			if (focusOnCase != null && focusOnCase != caseNum) {
				continue;
			}
			IUWordDictEntry entry =
				IUWordDict.getInstance().entry4word(aCase.word);

			String[] expIUHighlights = new String[0];
			String[] expTranslations = new String[0];

			if (!aCase.outOfVocab) {
				expIUHighlights = new String[] {aCase.word};
				expTranslations = aCase.expTranslations;
			}

			AssertIUWordDictEntry asserter =
				new AssertIUWordDictEntry(entry, "Case #"+caseNum+": "+aCase.id());

			asserter
				.isForWord(aCase.word)
				.definitionEquals(aCase.expDefinition)
				.relatedWordsAre(aCase.expRelatedWords)
				.possibleTranslationsStartWith("en", expTranslations)
				.atLeastNExamples(aCase.expMinExamples)
				.highlightsAreSubsetOf("iu", true, expIUHighlights)
				.highlightsAreSubsetOf("en", expTranslations)
				;

			if (
				(expTranslations == null || expTranslations.length == 0) &&
				aCase.expRelatedTranslations != null) {
				asserter.relatedTranslationsStartWith(aCase.expRelatedTranslations);

			}

			if (aCase.expDecomp != null) {
				asserter.decompositionIs(aCase.expDecomp);
			}
		}

		if (focusOnCase != null) {
			Assertions.fail("Test run on only one case. Make sure you set focusOnCase=null to run all tests");
		}
	}

	@Test
	public void test__search__HappyPath() throws Exception {
		String partialWord = "inuksu";
		Pair<Iterator<String>, Long> results = IUWordDict.getInstance().search(partialWord);
		assertSearchResultsStartWith(
			results.getLeft(), "inuksuk", "inuksuup", "inuksui");
	}

	//////////////////////////////////
	// TEST HELPERS
	//////////////////////////////////

	public static class IUWordDictCase {
		public String word = null;
		public String expDefinition = null;
		public String[] expDecomp = null;
		public String[] expRelatedWords = null;
		public String[] expTranslations = null;
		public Integer expMinExamples = 0;
		public boolean outOfVocab = false;
		public String[] expRelatedTranslations = null;

		public IUWordDictCase(String _word) {
			this.word = _word;
		}

		public IUWordDictCase setDefinition(String _expDefinition) {
			expDefinition = _expDefinition;
			return this;
		}

		public IUWordDictCase setDecomp(String... _expDecomp) {
			expDecomp = _expDecomp;
			return this;
		}

		public IUWordDictCase setRelatedWords(String... _expRelatedWords) {
			expRelatedWords = _expRelatedWords;
			return this;
		}

		public IUWordDictCase setOrigWordTranslations(String... _expTranslations) {
			expTranslations = _expTranslations;
			return this;
		}

		public IUWordDictCase setMinExamples(Integer _expMinExamples) {
			expMinExamples = _expMinExamples;
			return this;
		}

		public IUWordDictCase setOutOfVocab(boolean _outOfVocab) {
			this.outOfVocab = true;
			return this;
		}

		public Object id() {
			return this.word;
		}

		public IUWordDictCase setRelWordTranslationsStartWith(
			String[] _expRelatedTranslations) {
			this.expRelatedTranslations = _expRelatedTranslations;
			return this;
		}
	}

	////////////////////////////////////////
	// TEST HELPERS
	////////////////////////////////////////

	private void assertSearchResultsStartWith(
		Iterator<String> wordsIter, String... expTopWords) throws Exception {
		final int MAX_WORDS = 100;
		List<String> gotWordsLst = new ArrayList<String>();
		while (wordsIter.hasNext()) {
			gotWordsLst.add(wordsIter.next());
		}
		String[] gotWords = new String[gotWordsLst.size()];
		for (int ii=0; ii < gotWords.length; ii++) {
			gotWords[ii] = gotWordsLst.get(ii);
		}

		new AssertSequence<String>(gotWords)
			.startsWith(expTopWords);
	}

}