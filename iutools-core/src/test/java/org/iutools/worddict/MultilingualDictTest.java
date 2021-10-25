package org.iutools.worddict;

import ca.nrc.testing.AssertNumber;
import ca.nrc.testing.AssertSequence;
import ca.nrc.testing.AssertString;
import ca.nrc.testing.RunOnCases;
import ca.nrc.testing.RunOnCases.Case;
import org.apache.commons.lang3.tuple.Pair;
import org.iutools.linguisticdata.MorphemeHumanReadableDescr;
import org.iutools.script.TransCoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class MultilingualDictTest {

	MultilingualDictCase[] cases_entry4word = null;
	Case[] cases_search = null;

	@BeforeEach
	public void setUp() throws Exception {
		// Cases for search() function
		cases_search = new Case[] {
			new Case("iu-inuk-roman", "iu", "inuk", 200,
				new String[] {"inuk", "inukku", "inuksui", "inuksuk"}),
			new Case("iu-inuk-syll", "iu", "ᐃᓄᒃ", 200,
				new String[] {"ᐃᓄᒃ", "ᐃᓄᒃᑯ", "ᐃᓄᑯᓗᒃ"}),
		};

		// Cases for entry4word function
		cases_entry4word = new MultilingualDictCase[] {

			new MultilingualDictCase("iu-ammuumajuqsiuqtutik", "ammuumajuqsiuqtutik")
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

			new MultilingualDictCase("iu-ᐊᒻᒨᒪᔪᖅᓯᐅᖅᑐᑎᒃ", "ᐊᒻᒨᒪᔪᖅᓯᐅᖅᑐᑎᒃ")
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
			new MultilingualDictCase("iu-inuksssuk", "inuksssuk")
				.setOutOfVocab(true)
				.setOrigWordTranslations(new String[]{})
				.setMinExamples(0)
				.setRelatedWords(new String[]{}),

			// This word has a sentence pair whose word alignments are
			// faulty. Make sure it does not crash.
			new MultilingualDictCase("iu-umiarjuakkut", "umiarjuakkut")
				.setRelatedWords(
					"umiarjuanut", "umiarjuat", "umiarjuaq", "umiarjuarmut",
					"umiarjualirijikkut")
				.setMinExamples(5)
				.setOrigWordTranslations(new String[]{
					"sea", "ship", "shipping",
					"sealift arrives", "resupply ... dry ... cargo",
				}),

			new MultilingualDictCase("iu-kiugavinnga", "kiugavinnga")
				.setRelatedWords(
					"kiujjutit", "kiujjutik", "kiuvan", "kiujjutinga", "kiulugu")
				.setOrigWordTranslations(new String[]{
					"response", "answer", "answered", "direct answer",
					"minister ... answer"}),

			new MultilingualDictCase("iu-najugaq", "najugaq")
				.setRelatedWords(
					"najugangani", "najugaujunut", "najuganga", "najugaujumi",
					"najugauvattunut")
				.setRelWordTranslationsStartWith(new String[] {
					"home", "centres", "site", "area"}),

			new MultilingualDictCase("en-housing", "housing")
				.setL1("en")
				.setDecomp(null)
				.setOrigWordTranslations(
					"ᐃᒡᓗᖏᓐᓄᑦ", "ᐃᒡᓗᓕᕆᓂᕐᒥ", "ᐃᓪᓗᓕᕆᓂᕐᒧᑦ",
					"ᐃᒡᓗᒋᔭᐅᕙᒃᑐᓂᒃ",
					"ᐃᒡᓗᓕᕆᓂᕐᓕ ... ᐃᒡᓗᓕᕆᓂᕐᒧᑐᐃᓐᓈᕋᔭᖅᑐᖅ")
				.setMinExamples(10)
				.setRelatedWords(),

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
		MultilingualDict dict = MultilingualDict.getInstance();

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
				MultilingualDict.getInstance().entry4word(aCase.getLeft());
			new AssertMultilingualDictEntry(entry)
				.iuIsInScript(aCase.getRight())
			;

		}
	}

	@Test
	public void test__entry4word__VariousCases() throws Exception {

		Consumer<Case> runner = (uncastCase) -> {
			try {
				MultilingualDictCase aCase = (MultilingualDictCase)uncastCase;
				MultilingualDictEntry entry =
				MultilingualDict.getInstance()
					.entry4word(aCase.word, aCase.l1);

				String[] expL1Highlights = new String[0];
				String[] expTranslations = new String[0];

				if (!aCase.outOfVocab) {
					expL1Highlights = new String[]{aCase.word};
					expTranslations = aCase.expTranslations;
				}

				AssertMultilingualDictEntry asserter =
					new AssertMultilingualDictEntry(entry, aCase.descr);

				asserter
					.isForWord(aCase.word)
					.langIs(aCase.l1)
					.definitionEquals(aCase.expDefinition)
					.relatedWordsAre(aCase.expRelatedWords)
					.possibleTranslationsStartWith(expTranslations)
					.atLeastNExamples(aCase.expMinExamples)
					.highlightsAreSubsetOf(aCase.l1, true, expL1Highlights)
					.highlightsAreSubsetOf(aCase.l2, expTranslations)
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
//			.onlyCaseNums(7)
//			.onlyCasesWithDescr("iu-kiugavinnga")
			.run();
	}

	@Test
	public void test__search__HappyPath() throws Exception {
		String partialWord = "inuksu";
		Pair<Iterator<String>, Long> results = MultilingualDict.getInstance().searchIter(partialWord);
		assertSearchResultsStartWith(
			results.getLeft(), "inuksuk", "inuksuup", "inuksui");
	}

	@Test
	public void test__search__ENword() throws Exception {
		String partialWord = "housing";
		Pair<Iterator<String>, Long> results =
			MultilingualDict.getInstance().searchIter(partialWord, "en");
		assertSearchResultsStartWith(
			results.getLeft(), "housing");
	}
	@Test
	public void test__search__VariousCases() throws Exception {
		Consumer<Case> runner =
		(aCase) -> {
			try {
				String lang = (String) aCase.data[0];
				String query = (String) aCase.data[1];
				Integer expTotalWords = (Integer) aCase.data[2];
				String[] expTopMatches = (String[]) aCase.data[3];
				Pair<List<String>, Long> results2 =
				MultilingualDict.getInstance().search(query, lang, (Integer) null);
				AssertNumber.isGreaterOrEqualTo(
					aCase.descr,
					results2.getRight(), expTotalWords);
				new AssertSequence<String>(
					results2.getLeft().toArray(new String[0]),
					aCase.descr)
				.startsWith(expTopMatches);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
		new RunOnCases(cases_search, runner)
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
		public String[] expRelatedWords = null;
		public String[] expTranslations = null;
		public Integer expMinExamples = 0;
		public boolean outOfVocab = false;
		public String[] expRelatedTranslations = null;
		private String[] expOrigHighlights;

		public MultilingualDictCase(String _descr, String _word) {
			super(_descr, null);
			this.word = _word;
			this.expOrigHighlights = new String[] {_word};
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

		public MultilingualDictCase setRelatedWords(String... _expRelatedWords) {
			expRelatedWords = _expRelatedWords;
			return this;
		}

		public MultilingualDictCase setOrigWordTranslations(String... _expTranslations) {
			expTranslations = _expTranslations;
			return this;
		}

		public MultilingualDictCase setMinExamples(Integer _expMinExamples) {
			expMinExamples = _expMinExamples;
			return this;
		}

		public MultilingualDictCase setOutOfVocab(boolean _outOfVocab) {
			this.outOfVocab = true;
			return this;
		}

		public Object id() {
			return this.word;
		}

		public MultilingualDictCase setRelWordTranslationsStartWith(
			String[] _expRelatedTranslations) {
			this.expRelatedTranslations = _expRelatedTranslations;
			return this;
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
					MultilingualDict.getInstance().canonizeTranslation(lang, origText);
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

	private void assertSearchResultsStartWith(
		Iterator<String> wordsIter, String... expTopWords) throws Exception {
		assertSearchResultsStartWith((String)null, wordsIter, expTopWords);
	}

	private void assertSearchResultsStartWith(
		String mess, Iterator<String> wordsIter, String... expTopWords) throws Exception {
		final int MAX_WORDS = 100;
		if (mess == null) {
			mess = "";
		}
		List<String> gotWordsLst = new ArrayList<String>();
		while (wordsIter.hasNext()) {
			gotWordsLst.add(wordsIter.next());
		}
		String[] gotWords = new String[gotWordsLst.size()];
		for (int ii=0; ii < gotWords.length; ii++) {
			gotWords[ii] = gotWordsLst.get(ii);
		}

		new AssertSequence<String>(gotWords, mess)
			.startsWith(expTopWords);
	}
}
