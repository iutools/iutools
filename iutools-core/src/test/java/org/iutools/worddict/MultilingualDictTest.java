package org.iutools.worddict;

import ca.nrc.testing.*;
import ca.nrc.testing.RunOnCases.Case;
import org.apache.commons.lang3.tuple.Pair;
import org.iutools.linguisticdata.MorphemeHumanReadableDescr;
import org.iutools.script.TransCoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.util.*;
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
				.setOrigWordTranslations(
					"clam", "clam diving", "clam ... clams", "diving ... clams")
				.setMinExamples(3)
				.setRelatedWords(
					"ammuumajurniartiit", "ammuumajuqtarnirmut",
					"ammuumajuqtaqtiit", "ammuumajuqtaqtutik",
					"ammuumajurniarnirmut")
				.setExpRelatedTranslationsMap(
					new String[] {"ammuumajuqtaqtiit", "clam divers",
						"divers ... valid ... diving"},
					new String[] {"ammuumajuqtaqtutik", "clam divers",
						"classifications ... divers ... clam divers"},
					new String[] {"ammuumajuqtarnirmut", "clam", "clam diggers",
						"clam digging", "clam diggers ... commercial clam digging"},
					new String[] {"ammuumajurniartiit", "divers"}),

			new MultilingualDictCase("iu-ᐊᒻᒨᒪᔪᖅᓯᐅᖅᑐᑎᒃ", "ᐊᒻᒨᒪᔪᖅᓯᐅᖅᑐᑎᒃ")
				.setDecomp(
					"ammut/1a", "u/1nv", "ma/1vv", "juq/1vn", "siuq/1nv",
					"jusik/tv-ger-2d")
				.setOrigWordTranslations(
					"clam", "clam diving", "clam ... clams", "diving ... clams")
				.setMinExamples(3)
				.setRelatedWords(
					"ᐊᒻᒨᒪᔪᕐᓂᐊᕐᑏᑦ", "ᐊᒻᒨᒪᔪᖅᑕᕐᓂᕐᒧᑦ", "ᐊᒻᒨᒪᔪᖅᑕᖅᑏᑦ", "ᐊᒻᒨᒪᔪᖅᑕᖅᑐᑎᒃ",
					"ᐊᒻᒨᒪᔪᕐᓂᐊᕐᓂᕐᒧᑦ")
				.setExpRelatedTranslationsMap(
					new String[] {"ᐊᒻᒨᒪᔪᕐᓂᐊᕐᑏᑦ", "divers"},
					new String[] {"ᐊᒻᒨᒪᔪᖅᑕᕐᓂᕐᒧᑦ", "clam", "clam diggers",
						"clam digging", "clam diggers ... commercial clam digging"},
					new String[] {"ᐊᒻᒨᒪᔪᖅᑕᖅᑏᑦ", "clam divers",
						"divers ... valid ... diving"},
					new String[] {"ᐊᒻᒨᒪᔪᖅᑕᖅᑐᑎᒃ", "clam divers",
						"classifications ... divers ... clam divers"}),

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
					"sealift arrives", "resupply ... dry cargo",
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
				System.out.println("--** test__entry4word__VariousCases: aCase.word="+aCase.word);
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
					.relatedWordsIsSubsetOf(aCase.expRelatedWords)
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

				if (aCase.expRelatedTranslationsMap != null) {
					asserter.relatedTranslationsMapsEquals(aCase.expRelatedTranslationsMap);
				}


				if (aCase.expDecomp != null) {
					asserter.decompositionIs(aCase.expDecomp);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};

		new RunOnCases(cases_entry4word, runner)
//			.onlyCaseNums(2)
//			.onlyCasesWithDescr("iu-kiugavinnga")
			.run();
	}

	@Test
	public void test__search__HappyPath() throws Exception {
		String partialWord = "inuksu";
		Pair<Iterator<String>, Long> results =
			MultilingualDict.getInstance().searchIter(partialWord);
		assertSearchResultsInclude(
			results.getLeft(), "inuksuk", "inuksuup", "inuksui");
	}

	@Test
	public void test__search__ENword() throws Exception {
		String partialWord = "housing";
		Pair<Iterator<String>, Long> results =
			MultilingualDict.getInstance().searchIter(partialWord, "en");
		assertSearchResultsInclude(
			results.getLeft(), "housing");
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
				Pair<List<String>, Long> results2 =
					MultilingualDict.getInstance().search(query, lang, (Integer) null);
				AssertNumber.isGreaterOrEqualTo(
					aCase.descr,
					results2.getRight(), expMinWords);
				if (expMaxWords != null) {
					AssertNumber.isLessOrEqualTo(
						aCase.descr,
						results2.getRight(), expMaxWords);
				}
				new AssertSequence<String>(
					results2.getLeft().toArray(new String[0]),
					aCase.descr)
				.startsWith(expTopMatches);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
		new RunOnCases(cases_search, runner)
//			.onlyCaseNums(4)
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
		Map<String,List<String>> expRelatedTranslationsMap = null;

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

		public MultilingualDictCase setExpRelatedTranslationsMap(
			String[]... expMapEntries) {
			Map<String,List<String>> expMap = new HashMap<String,List<String>>();
			for (String[] anExpEntry: expMapEntries) {
				if (anExpEntry.length > 0) {
					String relWord = anExpEntry[0];
					String[] translArr = Arrays.copyOfRange(anExpEntry, 1, anExpEntry.length);
					List<String> transList = new ArrayList<String>();
					Collections.addAll(transList, translArr);
					expMap.put(relWord, transList);
				}
			}
			this.expRelatedTranslationsMap = expMap;
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

	private void assertSearchResultsInclude(
		Iterator<String> wordsIter, String... expTopWords) throws Exception {
		assertSearchResultsInclude((String)null, wordsIter, expTopWords);
	}

	private void assertSearchResultsInclude(
		String mess, Iterator<String> wordsIter, String... expWords) throws Exception {
		final int MAX_WORDS = 100;
		if (mess == null) {
			mess = "";
		}
		Set<String> expWordsSet = new HashSet<String>();
		for (int ii=0; ii < expWords.length; ii++) {
			expWordsSet.add(expWords[ii]);
		}

		Set<String> gotWordsSet = new HashSet<String>();
		while (wordsIter.hasNext()) {
			gotWordsSet.add(wordsIter.next());
		}
		AssertSet.assertContainsAll(mess, expWordsSet, gotWordsSet);
	}
}
