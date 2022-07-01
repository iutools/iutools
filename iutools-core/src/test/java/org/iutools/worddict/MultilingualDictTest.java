package org.iutools.worddict;

import ca.nrc.testing.*;
import ca.nrc.testing.RunOnCases.Case;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.iutools.linguisticdata.MorphemeHumanReadableDescr;
import org.iutools.script.TransCoder;
import org.iutools.utilities.StopWatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;


import java.util.*;
import java.util.function.Consumer;

public class MultilingualDictTest {

	MultilingualDictCase[] cases_entry4word = null;
	Case[] cases_search = null;

	@BeforeEach
	public void setUp() throws Exception {
		// Cases for search() function
		cases_search = new Case[] {

			new Case("iu-inuk-roman", "iu", "inuk", 180,
				new String[] {"inuk", "inukku",
					"inuksui", "inuksuk"}),
			new Case("iu-inuk-syll", "iu", "ᐃᓄᒃ", 180,
				new String[] {"ᐃᓄᒃ", "ᐃᓄᒃᑯ", "ᐃᓄᑯᓗᒃ"}),
			new Case("iu-single-hit", "iu", "nunavuttaarniq", 1,
				new String[] {"nunavuttaarniq"}, 1),
			new Case("iu-out-of-yet-valid-dict-word", "iu", "umiaqtulaaqtunga", 1,
				new String[] {"umiaqtulaaqtunga"}, 1),
		};

		// Cases for entry4word function
		cases_entry4word = new MultilingualDictCase[] {

			new MultilingualDictCase("iu-ammuumajuqsiuqtutik", "ammuumajuqsiuqtutik")
				.setDecomp(
					"ammut/1a", "u/1nv", "ma/1vv", "juq/1vn", "siuq/1nv",
					"jusik/tv-ger-2d")
				.relatedWordsShouldBeAmong(
					"ammuumajurniartiit", "ammuumajuqtarnirmut",
					"ammuumajuqtaqtiit", "ammuumajuqtaqtutik",
					"ammuumajurniarnirmut", "ammuumajuqsiuqtutik")
				.bestTranslationsAre("clam", "clams", "clam diving", "clam ... clams")
				.setMinExamples(5),
//				.additionalL2Highlights("clam ... clams", "clam diving"),

			new MultilingualDictCase("iu-ᐊᒻᒨᒪᔪᖅᓯᐅᖅᑐᑎᒃ", "ᐊᒻᒨᒪᔪᖅᓯᐅᖅᑐᑎᒃ")
				.setDecomp(
					"ammut/1a", "u/1nv", "ma/1vv", "juq/1vn", "siuq/1nv",
					"jusik/tv-ger-2d")
				.relatedWordsShouldBeAmong(
					"ᐊᒻᒨᒪᔪᕐᓂᐊᕐᑏᑦ", "ᐊᒻᒨᒪᔪᖅᑕᕐᓂᕐᒧᑦ", "ᐊᒻᒨᒪᔪᖅᑕᖅᑏᑦ", "ᐊᒻᒨᒪᔪᖅᑕᖅᑐᑎᒃ",
					"ᐊᒻᒨᒪᔪᕐᓂᐊᕐᓂᕐᒧᑦ")
				.bestTranslationsAre("clam", "clams", "clam diving", "clam ... clams")
				.setMinExamples(2),
//				.additionalL2Highlights("clam ... clams", "clam diving"),

			// This is an out of vocabulary word
			new MultilingualDictCase("iu-inuksssuk", "inuksssuk")
				.setOutOfVocab(true)
				.bestTranslationsAre(new String[0])
				.setMinExamples(0)
				.relatedWordsShouldBeAmong(new String[]{}),

			// This word has a sentence pair whose word alignments are
			// faulty. Make sure it does not crash.
			new MultilingualDictCase("iu-umiarjuakkut", "umiarjuakkut")
				.relatedWordsShouldBeAmong(
					"umiarjuanut", "umiarjuat", "umiarjuaq", "umiarjuarmut",
					"umiarjualirijikkut")
				.setMinExamples(5)
				.bestTranslationsAre(new String[]{
					"ship", "sealift", "shipped", "shipping", "shipping season"}),
//				.additionalL2Highlights(
//					"shipping", "shipping season"
//					),

			new MultilingualDictCase("iu-kiugavinnga", "kiugavinnga")
				.relatedWordsShouldBeAmong(
					"kiujjutit", "kiujjutik", "kiuvan", "kiujjutinga", "kiulugu")
				.bestTranslationsAre(
					"response", "answer", "answered", "direct answer", "minister ... answer"),
//				.additionalL2Highlights(),

			new MultilingualDictCase("iu-najugaq", "najugaq")
				.relatedWordsShouldBeAmong(
					"najugangani", "najugaujunut", "najuganga", "najugaujumi",
					"najugauvattunut")
				.bestTranslationsAre(
					"facility", "home", "units", "centres", "homes", "centres"),

			new MultilingualDictCase("en-housing", "housing")
				.setL1("en")
				.setDecomp(null)
				.bestTranslationsAre(
					"ᐃᓪᓗᐃᑦ", "ᐃᓪᓗᓂᒃ", "ᐃᓪᓗᓕᕆᔨᒃᑯᓐᓄᑦ", "ᐃᓪᓗᓕᕆᔨᓂ", "ᐃᓪᓗᓕᕆᔨᓂᒃ"
				)
				.setMinExamples(6)
				.relatedWordsShouldBeAmong(),

//			new MultilingualDictCase("iu-nuvarjuarnaq (=covid)", "nuvarjuarnaq")
//				.setL1("en")
//				.setDecomp(null)
//				.setOrigWordTranslations(
//					"ᐃᒡᓗᖏᓐᓄᑦ", "ᐃᒡᓗᓕᕆᓂᕐᒥ", "ᐃᓪᓗᓕᕆᓂᕐᒧᑦ",
//					"ᐃᒡᓗᒋᔭᐅᕙᒃᑐᓂᒃ",
//					"ᐃᒡᓗᓕᕆᓂᕐᓕ ... ᐃᒡᓗᓕᕆᓂᕐᒧᑐᐃᓐᓈᕋᔭᖅᑐᖅ")
//				.setMinExamples(10)
//				.setRelatedWords(),
		};
	}

	//////////////////////////////////
	// DOCUMENTATION TESTS
	//////////////////////////////////

	@Test
	public void test__MultilingualDict__Synopsis() throws Exception {
		// The dictionary is a singleton
		MultilingualDict dict = new MultilingualDict();

		// Given an inuktitut word, you can get its dictionary entry
		MultilingualDictEntry entry = dict.entry4word("inuksuk");

		// The input word can be in latin or syllabic alphabet
		entry = dict.entry4word("ᐃᓄᒃᓱᒃ");

		// The entry contains a bunch of information about the word

		// Definition (may be null)
		String definition = entry.definition;

		// Morphological decomposition in human-readable form
		List<MorphemeHumanReadableDescr> decomp = entry.morphDecomp;

		// List of possible English translations with scores.
		for (Pair<String,Double> scoredTranslations: entry.otherLangTranslations()) {
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
		Pair<Iterator<String>, Long> result = dict.searchIter(partialWord);
		// Total number of matching words
		Long totalWords = result.getRight();
		Iterator<String> wordsIter = result.getLeft();
		// Iterate through the matching words...
		while (wordsIter.hasNext()) {
			String matchingWord = wordsIter.next();
		}

		// By default the dictionary assumes that the input word is in
		// Inuktitut. But you can also search for English words
		partialWord = "housing";
		result = dict.searchIter(partialWord, "en");
		while (wordsIter.hasNext()) {
			String matchingWord = wordsIter.next();
			dict.entry4word(matchingWord, "en");
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

			MultilingualDictEntry entry =
				new MultilingualDict().entry4word(aCase.getLeft());
			new AssertMultilingualDictEntry(entry)
				.iuIsInScript(aCase.getRight())
			;

		}
	}

	@Test
	public void test__entry4word__SpeedTest(TestInfo testInfo) throws Exception {
		String[] words = new String[] {
			"amiq", "nunavut", "annuraanik", "qarasaujaq", "ilinniaqtuliriniq",
			"titiraujaq"
		};

		MultilingualDict dict = new MultilingualDict()
			.setMinMaxPairs(100, 100);
		long start = StopWatch.nowMSecs();
		for (String aWord: words) {
			dict.entry4word(aWord);
		}
		long elapsed = StopWatch.elapsedMsecsSince(start);
		double gotAvgSecs = elapsed / (1000.0 * words.length);
		AssertRuntime.runtimeHasNotChanged(
			gotAvgSecs, 0.20,
			"avg secs for retrieving a dict entry", testInfo);
	}

	@Test
	public void test__entry4word__VariousCases() throws Exception {
		// For some reason, this test fails intermittently if we don't sleep
		// before doing it.
		Thread.sleep(2*1000);
		Consumer<Case> runner = (uncastCase) -> {
			try {
				MultilingualDictCase aCase = (MultilingualDictCase)uncastCase;
				MultilingualDictEntry entry =
					new MultilingualDict()
						.entry4word(aCase.word, aCase.l1);

				String[] expL1Highlights = new String[0];
				String[] expTranslations = new String[0];

				if (!aCase.outOfVocab) {
					expL1Highlights = new String[]{aCase.word};
					expTranslations = aCase.expTranslations;
				}

				AssertMultilingualDictEntry asserter =
					new AssertMultilingualDictEntry(entry, aCase.descr);

				String[] expL2Highlights =
					ArrayUtils.addAll(aCase.expTranslations, aCase.expAdditionalL2Highlights);

				asserter
					.isForWord(aCase.word)
					.langIs(aCase.l1)
					.definitionEquals(aCase.expDefinition)
					.relatedWordsIsSubsetOf(aCase.expRelatedWordsSuperset)
					.bestTranslationsAre(expTranslations)
					.atLeastNExamples(aCase.expMinExamples)
					.highlightsAreSubsetOf(aCase.l1, true, expL1Highlights)
					.highlightsAreSubsetOf(aCase.l2, expL2Highlights)
					;

				if (aCase.l1.equals("iu")) {
					asserter.checkWordInOtherScript(aCase.word);
				}

				if (
				(expTranslations == null || expTranslations.length == 0) &&
				aCase.expRelatedTranslations != null) {
					asserter.relatedTranslationsStartWith(aCase.expRelatedTranslations);
				}

				if (aCase.expDecomp != null) {
					asserter.decompositionIs(aCase.expDecomp);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};

		new RunOnCases(cases_entry4word, runner)
//			.onlyCaseNums(1)
//			.onlyCasesWithDescr("en-housing")
			.run();
	}

	@Test
	public void test__search__HappyPath() throws Exception {
		String partialWord = "inuksu";
		Pair<Iterator<String>, Long> results =
			new MultilingualDict().searchIter(partialWord);

		new AssertDictSearchResults(results.getLeft(), results.getRight())
			.containsWords("inuksuk", "inuksuup", "inuksui")
			;
	}

	@Test
	public void test__search__ENword() throws Exception {
		String partialWord = "housing";
		Pair<Iterator<String>, Long> results =
			new MultilingualDict().searchIter(partialWord, "en");
		new AssertDictSearchResults(results.getLeft(), results.getRight())
			.containsWords("housing");
	}
	@Test
	public void test__search__VariousCases() throws Exception {
		Consumer<Case> runner =
		(aCase) -> {
			try {
				String lang = (String) aCase.data[0];
				String query = (String) aCase.data[1];
				Integer expMinWords = (Integer) aCase.data[2];
				String[] expTopMatches = (String[]) aCase.data[3];
				Integer expMaxWords = null;
				if (aCase.data.length > 4) {
					expMaxWords = (Integer) aCase.data[4];
				}
				Pair<List<String>, Long> results =
					new MultilingualDict().search(query, lang, (Integer) null);
				AssertDictSearchResults asserter =
					new AssertDictSearchResults(results, aCase.descr)
						.containsAtLeast(expMinWords);
				if (expMaxWords != null) {
					asserter.containsAtMost(expMaxWords);
				}
				asserter.hitsStartWith(expTopMatches);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
		new RunOnCases(cases_search, runner)
//			.onlyCaseNums(3)
			.run();
	}


	//////////////////////////////////
	// TEST HELPERS
	//////////////////////////////////

	public static class MultilingualDictCase extends Case {
		public String word = null;
		public String l1 = "iu";
		public String l2 = "en";
		public String expDefinition = null;
		public String[] expDecomp = null;
		public String[] expRelatedWordsSuperset = null;
		public String[] expTranslations = null;
		public Integer expMinExamples = 0;
		public boolean outOfVocab = false;
		public String[] expRelatedTranslations = null;
		private String[] expAdditionalL2Highlights = new String[0];
//		Map<String,List<String>> expRelatedTranslationsMap = null;

		public MultilingualDictCase(String _descr, String _word) {
			super(_descr, null);
			this.word = _word;
		}

		public MultilingualDictCase setL1(String _lang) throws RuntimeException {
			l1 = _lang;
			try {
				l2 = MultilingualDictEntry.otherLang(_lang);
			} catch (MultilingualDictException e) {
				throw new RuntimeException(e);
			}
			return this;
		}

		public MultilingualDictCase setDefinition(String _expDefinition) {
			expDefinition = _expDefinition;
			return this;
		}

		public MultilingualDictCase setDecomp(String... _expDecomp) {
			expDecomp = _expDecomp;
			return this;
		}

		public MultilingualDictCase relatedWordsShouldBeAmong(String... _expRelatedWords) {
			expRelatedWordsSuperset = _expRelatedWords;
			return this;
		}

		public MultilingualDictCase bestTranslationsAre(String... _expTranslations) {
			expTranslations = _expTranslations;
			return this;
		}

		public MultilingualDictCase setMinExamples(Integer _expMinExamples) {
			expMinExamples = _expMinExamples;
			return this;
		}

//		/**
//		 * Provide additional highlights for the l2 bits. These will be added to
//		 * the expected list of best translations.
//		 */
//		public MultilingualDictCase additionalL2Highlights(String... l2Highlights) {
//			this.expAdditionalL2Highlights = l2Highlights;
//			return this;
//		}

		public MultilingualDictCase setOutOfVocab(boolean _outOfVocab) {
			this.outOfVocab = true;
			return this;
		}

		public Object id() {
			return this.word;
		}
	}

	@Test
	public void test__canonizeTranslation__VariousCases() throws Exception {
		Case[] cases = new Case[] {
			new Case("en:happy path", "en", "Joy to the world", "Joy ... world"),
			new Case("en:some sws are uppercased", "en", "Joy To The World", "Joy ... World"),
			new Case("en:elipsis followed or preceded by sw", "en",
				"Joy ... To The ... World", "Joy ... World"),
			new Case("en:leading elipsis", "en", "... World", "World"),
			new Case("en:tailing elipsis", "en", "World ...", "World"),
			new Case("en:null text", "en", null, null),
			new Case("en:null text", "en", null, null),
			new Case("iu:ᐃᒡᓗᓕᕆᓂᕐᓕ ... ᐃᒡᓗᓕᕆᓂᕐᒧᑐᐃᓐᓈᕋᔭᖅᑐᖅ", "iu",
				"ᐃᒡᓗᓕᕆᓂᕐᓕ ... ᐃᒡᓗᓕᕆᓂᕐᒧᑐᐃᓐᓈᕋᔭᖅᑐᖅ", "ᐃᒡᓗᓕᕆᓂᕐᓕ ... ᐃᒡᓗᓕᕆᓂᕐᒧᑐᐃᓐᓈᕋᔭᖅᑐᖅ")
		};
		Consumer<Case> runner = (aCase) -> {
			String lang = (String) aCase.data[0];
			String origText =(String)aCase.data[1];
			String expText =(String)aCase.data[2];
			try {
				String gotText =
					new MultilingualDict().canonizeTranslation(lang, origText);
				AssertString.assertStringEquals(
					aCase.descr, expText, gotText
				);
			} catch (MultilingualDictException e) {
				throw new RuntimeException(e);
			}
		};
		new RunOnCases(cases, runner)
//			.onlyCaseNums(8)
//			.onlyCasesWithDescr("en:leading elipsis")
			.run();
	}

	////////////////////////////////////////
	// TEST HELPERS
	////////////////////////////////////////


}
