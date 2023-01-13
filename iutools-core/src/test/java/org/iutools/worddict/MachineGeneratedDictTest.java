package org.iutools.worddict;

import ca.nrc.data.file.ObjectStreamReader;
import ca.nrc.testing.*;
import ca.nrc.testing.RunOnCases.Case;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.iutools.corpus.WordInfo;
import org.iutools.linguisticdata.MorphemeHumanReadableDescr;
import org.iutools.script.TransCoder;
import ca.nrc.datastructure.CloseableIterator;
import org.iutools.sql.SQLLeakMonitor;
import org.iutools.utilities.StopWatch;
import org.junit.jupiter.api.*;


import java.io.File;
import java.util.*;
import java.util.function.Consumer;

public class MachineGeneratedDictTest {

	MultilingualDictCase[] cases_entry4word = null;
	Case[] cases_search = null;

	SQLLeakMonitor sqlLeakMonitor = null;

	@BeforeEach
	public void setUp() throws Exception {
		sqlLeakMonitor = new SQLLeakMonitor();

		// Cases for search() function
		cases_search = new Case[] {
			new Case("iu-inuk-roman", "iu", "inuk", 180,
				new String[] {"inuk", "inukku",
					"inuksui", "inuksuk"}),
			new Case("iu-inuk-syll", "iu", "ᐃᓄᒃ", 180,
				new String[] {"ᐃᓄᒃ", "ᐃᓄᒃᑯ", "ᐃᓄᑯᓗᒃ"}),
			new Case("iu-single-hit", "iu", "nunavuttaarniq", 1,
				new String[] {"nunavuttaarniq"}, 1),
			new Case("en-housing", "en", "housing", 1,
				new String[] {"housing"}),
			// Note: This syllabic word has a level 1 spelling error, namely,
			// it uses two characters ᕐ+ᑭ instead of single character ᕿ
			new Case("iu world with shallow spelling mistake --> should autocorrect",
				"iu", "ᐃᓕᓐᓂᐊᕐᑭᑎ", 1,
				new String[] {"ᐃᓕᓐᓂᐊᕿᑎ"}),
		};

		// Cases for entry4word function
		cases_entry4word = new MultilingualDictCase[] {

			new MultilingualDictCase(
				"Wikipedia glossary entry (nanitsikuujait) for which neither the word nor related words appear in the Hansard ",
				"nanitsikuujait")
				.relatedWordsShouldBeAmong("nanisijunnarutta", "nanijausimajut",  "nanisijunnaruma",
					"nanisijjummu", "nanijunnanginnakku")
				.humanTranslationsAre("corn flakes")
				.hasMinTranslation(1)
				.hasTranslationsForOrigWord(true)
				.bestTranslationsAreAmong("corn flakes")
				.noExamplesAtAll(),

			new MultilingualDictCase(
				"iu word with shallow spelling mistake (nunavuumik)", "nunavuumik")
				.hasStandardSpelling("nunavummik", "ᓄᓇᕗᒻᒥᒃ")
				.hasDecomp("nunavut/1n", "mik/tn-acc-s")
				// TODO: Is it normal that this word has no related words?
				.hasMinTranslation(4)
				.hasTranslationsForOrigWord(true)
				.bestTranslationsAreAmong(
					"land",  "nunavut", "nunavut ... nunavut", "today'... nunavut"
				)
				.hasMinExamples(30),

			new MultilingualDictCase("iu-word-with-glossary-entry (annuraanik=inuit clothing)", "annuraanik")
				.hasDecomp(
					"annuraaq/1n", "nik/tn-acc-p")
				.relatedWordsShouldBeAmong(
					"annuraanginnit", "annuraangit", "annuraanit", "annuraanut",
					"annuraat")
				.hasMinTranslation(5)
				.hasTranslationsForOrigWord(true)
				.bestTranslationsAreAmong(
					"inuit clothing", "clothing", "dry clothing", "fashions", "garments",
					"wash ... cloths"
				)
				.humanTranslationsAre("inuit clothing")
				.hasMinExamples(5),

			new MultilingualDictCase("iu-nunaqaqqaaqsimajut (=aboriginal people)", "nunaqaqqaaqsimajut")
				.hasDecomp(
					"nunaqaq/1v", "qqaaq/1vv", "sima/1vv", "jut/tv-ger-3p")
				.relatedWordsShouldBeAmong(
					"nunaqaqqaasimajut", "nunaqaqtunik", "nunaqaqtunut", "nunaqaqtut",
  					"nunaqaratta")
				.hasMinTranslation(4)
				.hasTranslationsForOrigWord(true)
				.bestTranslationsAreAmong(
					// Reasonable complete or partial translations
					"aboriginal", "aboriginal people", "aboriginal ... affairs",
				   "indigenous people", "indigenous peoples", "inuit", "nations",

					// These are not translation by they may appear in the list for
					// some reason
					"affirmative"
				)
				.hasMinExamples(5),

			new MultilingualDictCase("iu-ammuumajuq", "ammuumajuq")
				.hasDecomp(
					"ammut/1a", "u/1nv", "ma/1vv", "juq/1vn")
				.relatedWordsShouldBeAmong(
					"ammuumajurniartiit", "ammuumajuqtarnirmut",
					"ammuumajuqtaqtiit", "ammuumajuqtaqtutik",
					"ammuumajurniarnirmut", "ammuumajuqsiuqtutik")
				.hasMinTranslation(4)
				.hasTranslationsForOrigWord(false)
				.bestTranslationsAreAmong(
					"clam","clam divers", "clam diggers", "clam digging",
					"clam diving", "clams", "divers")
				.hasMinExamples(5),

			new MultilingualDictCase("iu-ammuumajuqsiuqtutik", "ammuumajuqsiuqtutik")
				.hasDecomp(
					"ammut/1a", "u/1nv", "ma/1vv", "juq/1vn", "siuq/1nv",
					"jusik/tv-ger-2d")
				.relatedWordsShouldBeAmong(
					"ammuumajurniartiit", "ammuumajuqtarnirmut",
					"ammuumajuqtaqtiit", "ammuumajuqtaqtutik",
					"ammuumajurniarnirmut", "ammuumajuqsiuqtutik")
				.hasMinTranslation(4)
				.bestTranslationsAreAmong("clam", "clams", "clam diving", "clam ... clams")
				.hasMinExamples(5),

			new MultilingualDictCase("iu-ᐊᒻᒨᒪᔪᖅᓯᐅᖅᑐᑎᒃ", "ᐊᒻᒨᒪᔪᖅᓯᐅᖅᑐᑎᒃ")
				.hasDecomp(
					"ammut/1a", "u/1nv", "ma/1vv", "juq/1vn", "siuq/1nv",
					"jusik/tv-ger-2d")
				.relatedWordsShouldBeAmong(
					"ᐊᒻᒨᒪᔪᕐᓂᐊᕐᑏᑦ", "ᐊᒻᒨᒪᔪᖅᑕᕐᓂᕐᒧᑦ", "ᐊᒻᒨᒪᔪᖅᑕᖅᑏᑦ", "ᐊᒻᒨᒪᔪᖅᑕᖅᑐᑎᒃ",
					"ᐊᒻᒨᒪᔪᕐᓂᐊᕐᓂᕐᒧᑦ")
				.hasMinTranslation(4)
				.bestTranslationsAreAmong("clam", "clams", "clam diving", "clam ... clams")
				.hasMinExamples(2),

			// This is an out of vocabulary word
			new MultilingualDictCase("iu-inuksssuk", "inuksssuk")
				.setOutOfVocab(true)
				.hasTranslationsForOrigWord(false)
				.bestTranslationsAreAmong(new String[0])
				.hasMinExamples(0)
				.relatedWordsShouldBeAmong(new String[]{}),

			// This word has a sentence pair whose word alignments are
			// faulty. Make sure it does not crash.
			new MultilingualDictCase("iu-umiarjuakkut", "umiarjuakkut")
				.relatedWordsShouldBeAmong(
					"umiarjuanut", "umiarjuat", "umiarjuaq", "umiarjuarmut",
					"umiarjualirijikkut")
				.hasMinExamples(5)
				.hasMinTranslation(5)
				.bestTranslationsAreAmong(new String[]{
					"barge", "sealift", "ship", "shipped", "shipping", "shipping season",
					"supply", "marine ... late", "sea cans", "ships", "vessels"}),

			new MultilingualDictCase("iu-kiugavinnga", "kiugavinnga")
				.relatedWordsShouldBeAmong(
					"kiujjutit", "kiujjutik", "kiuvan", "kiujjutinga", "kiulugu")
				.hasMinTranslation(5)
				.bestTranslationsAreAmong(
					"response", "answer", "answered", "answering ... question",
					"direct answer", "minister ... answer"),

			new MultilingualDictCase("iu-najugaq", "najugaq")
				.relatedWordsShouldBeAmong(
					"najugangani", "najugaujunut", "najuganga", "najugaujumi",
					"najugauvattunut")
				.hasMinTranslation(5)
				.bestTranslationsAreAmong(
					"centres", "facility", "group home", "home", "homes", "units",
					"residence",
					// Words below are not proper translations but for some reason
					// they can be returned by the algorithm
					"shows"
					),

			new MultilingualDictCase("en-housing", "housing")
				.setL1("en")
				.hasDecomp(null)
				.hasMinTranslation(5)
				.bestTranslationsAreAmong(
					"ᐃᓪᓗᐃᑦ", "ᐃᓪᓗᓂᒃ", "ᐃᓪᓗᓕᕆᔨᒃᑯᓐᓄᑦ", "ᐃᓪᓗᓕᕆᔨᓂ", "ᐃᓪᓗᓕᕆᔨᓂᒃ",
					// Note: These started appearing when we moved from ES to SQL
					// for the TM data store
					"ᐃᒡᓗᑖᕆᐊᖃᕐᓂᐊᕐᒪᖔᑕ", "ᐃᒡᓗᓕᕆᔨᐊᓛᒃᑯᑦ", "ᐃᓪᓗᒃᐸᑕ ... ᐃᓪᓗᖃᕐᓂᕐᒧᑦ",
					"ᐃᓪᓗᓕᕆᔨᕐᔪᐊᒃᑯᑦ","ᐃᓪᓗᖁᑎᖏᑦ", "ᐃᒡᓗᓕᕆᓂᕐᒥ", "ᐃᓪᓗᒃᓴᖏᑦ", "ᐃᓪᓗᓕᕆᓂᖅ",
					"ᐃᓪᓗᖏᑦ",
					// Hum... this one doesn't start with the same characters as
					// the rest of the translations
					"ᓄᓇᕗᒻᒥ"
				)
				.hasMinExamples(5)
				.relatedWordsShouldBeAmong(),

			new MultilingualDictCase("SYLLL word with a glossary entry",
				"ᐃᓕᓐᓂᐊᖅᑐᓕᕆᓂᖅ")
				.setL1("iu")
				.relatedWordsShouldBeAmong(
					"ᐃᓕᓐᓂᐊᖅᑐᓕᕆᓂᕐᒧᑦ", "ᐃᓕᓐᓂᐊᖅᑐᓕᕆᔨᒃᑯᑦ", "ᐃᓕᓐᓂᐊᕐᑐᓕᕆᔩᑦ",
					"ᐃᓕᓐᓂᐊᖅᑐᓕᕆᔨᒃᑯᓐᓄᑦ", "ᐃᓕᓐᓂᐊᕐᑐᓕᕆᔨᒃᑯᓐᓂᑦ")
				.bestTranslationsAreAmong(
					"education", "care ... education", "education system",
					"education issues", "education ... two", "education ... vital")
				.humanTranslationsAre("education"),


			new MultilingualDictCase(
				"inukssuk: Has Level1 spelling mistake that can only be highlighted",
				"inukssuk")
				.hasStandardSpelling("inu[ks]suk", "ᐃᓄ[ᒃᔅ]ᓱᒃ")
				.hasTranslationsForOrigWord(false)
				.hasMaxTranslation(0)
				.noExamplesAtAll(),
		};
	}

	@AfterEach
	public void tearDown() {
		sqlLeakMonitor.assertNoLeaks();
	}

	//////////////////////////////////
	// DOCUMENTATION TESTS
	//////////////////////////////////

	@Test
	public void test__MultilingualDict__Synopsis() throws Exception {
		// The dictionary is a singleton
		MachineGeneratedDict dict = new MachineGeneratedDict();

		// Given an inuktitut word, you can get its dictionary entry
		MDictEntry entry = dict.entry4word("inuksuk");

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
		Pair<CloseableIterator<String>, Long> result = dict.searchIter(partialWord);
		// Total number of matching words
		Long totalWords = result.getRight();
		try (CloseableIterator<String> wordsIter = result.getLeft()) {
			// Iterate through the matching words...
			while (wordsIter.hasNext()) {
				String matchingWord = wordsIter.next();
			}
		}

		// By default the dictionary assumes that the input word is in
		// Inuktitut. But you can also search for English words
		partialWord = "housing";
		result = dict.searchIter(partialWord, "en");
		try (CloseableIterator<String> wordsIter = result.getLeft()) {
			while (wordsIter.hasNext()) {
				String matchingWord = wordsIter.next();
				dict.entry4word(matchingWord, "en");
			}
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

			MDictEntry entry =
				new MachineGeneratedDict().entry4word(aCase.getLeft());
			new AssertMDictEntry(entry)
				.iuIsInScript(aCase.getRight())
			;

		}
	}

	@Test
	public void test__entry4word__InputWordIsEN() throws Exception {
		MDictEntry entry =
			new MachineGeneratedDict().entry4word("project", "en");
		new AssertMDictEntry(entry)
			.isForWord("project")
			// For EN terms, we shouldn't be setting the various fields related
			// to the different IU scripts
			.wordInOtherScriptIs(null)
			.wordRomanIs(null)
			.wordSyllIs(null)
		;
	}

	@Test
	public void test__entry4word__SpeedTest(TestInfo testInfo) throws Exception {
		String[] words = new String[] {
			// All of these words have direct translations
			"amiq", "nunavut", "annuraanik", "qarasaujaq", "ilinniaqtuliriniq",
			"titiraujaq", "ammuumajuqsiuqtutik", "umiarjuakkut", "kiugavinnga", "najugaq"
		};

		MachineGeneratedDict dict = new MachineGeneratedDict()
			.setMinMaxPairs(100, 100);

		StopWatch sw = new StopWatch().start();
		System.out.println("Time for different words");
		for (String aWord: words) {
			System.out.println("Processing aWord="+aWord);
			dict.entry4word(aWord);
			System.out.println("  "+aWord+": "+sw.lapTime()+" msecs");
		}
		long elapsed = sw.totalTime();
		double gotAvgSecs = elapsed / (1000.0 * words.length);
		System.out.println("Avg secs per word: "+gotAvgSecs);
		AssertRuntime.runtimeHasNotChanged(
			gotAvgSecs, 0.4,
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
				Long start = System.currentTimeMillis();
				MDictEntry entry =
					new MachineGeneratedDict()
						.entry4word(aCase.word, aCase.l1);
				double elapsed = 1.0 * (System.currentTimeMillis() - start) / 1000;
				System.out.println("   ran in "+elapsed+" seconds");
				aCase.registerRunningTime(elapsed);

				String[] expL1Highlights = new String[0];
				String[] expTranslations = new String[0];

				List<String> words = new ArrayList<String>();
				words.add(aCase.word);
				Collections.addAll(words, entry.relatedWords);

				if (aCase.producesSomeExamples && !aCase.outOfVocab) {
					List<String> expL1HighlightsLst = new ArrayList<String>();
					expL1HighlightsLst.add(aCase.word);
					if (aCase.expRelatedWordsSuperset != null) {
						Collections.addAll(expL1HighlightsLst, aCase.expRelatedWordsSuperset);
					}
					expL1Highlights = expL1HighlightsLst.toArray(new String[0]);
				}
				if (aCase.producesSomeExamples || aCase.expHumanTranslations != null) {
					expTranslations = aCase.expTranslationsAmong;
				}

				AssertMDictEntry asserter =
					new AssertMDictEntry(entry, aCase.descr);

				String[] expL2Highlights =
					ArrayUtils.addAll(aCase.expTranslationsAmong, aCase.expAdditionalL2Highlights);

				asserter
					.isForWord(aCase.word)
					.langIs(aCase.l1)
					.definitionEquals(aCase.expDefinition);
				if (aCase.expRelatedWordsSuperset == null) {
					asserter.hasNoRelatedWords();
				} else {
					asserter.relatedWordsIsSubsetOf(aCase.expRelatedWordsSuperset);
				}

				if (aCase.expStdRoman != null) {
					asserter
						.hasStandardizedSpelling(
							aCase.expStdRoman, aCase.expStdSyll);
				}

				if (aCase.expMinTranslations != null) {
					asserter.hasAtLeastNTranslations(aCase.expMinTranslations);
				}

				if (aCase.expMaxTranslations != null) {
					asserter.hasAtLeastNTranslations(aCase.expMaxTranslations);
				}

				asserter
					.hasTranslationsForOrigWord(aCase.expHasTranslationsForOrigWord)
					.bestTranslationsAreAmong(aCase.translationsWithNoExamples, expTranslations);
				if (aCase.producesSomeExamples) {
					asserter.atLeastNExamples(aCase.expMinExamples)
						.highlightsAreSubsetOf(aCase.l1, true, expL1Highlights)
						.highlightsAreSubsetOf(aCase.l2, expL2Highlights)
						;
					}

				if (aCase.expHumanTranslations != null) {
					asserter
						.bestTranslationsStartWith(
							"Best translations should start with the human translations",
							aCase.expHumanTranslations)
						.humanTranslationsAre(aCase.expHumanTranslations);
				}

				if (aCase.l1.equals("iu")) {
					asserter.checkWordInOtherScript(aCase.word);
				}

				if (aCase.expDecomp != null) {
					asserter.decompositionIs(aCase.expDecomp);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};

		new RunOnCases(cases_entry4word, runner)
//			.onlyCaseNums(14)
//			.onlyCasesWithDescr("word with a glossary entry")
			.run();
	}

	@Test
	public void test__search__HappyPath() throws Exception {
		String partialWord = "inuksu";
		Pair<CloseableIterator<String>, Long> results =
			new MachineGeneratedDict().searchIter(partialWord);

		try (CloseableIterator<String> wordsIter = results.getLeft()) {
			new AssertDictSearchResults(results.getLeft(), results.getRight())
				.containsWords("inuksuk", "inuksuup", "inuksui")
			;
		}
	}

	@Test
	public void test__search__ENword() throws Exception {
		String partialWord = "housing";
		Pair<CloseableIterator<String>, Long> results =
			new MachineGeneratedDict().searchIter(partialWord, "en");
		try (CloseableIterator<String> wordsIter = results.getLeft()) {
			new AssertDictSearchResults(wordsIter, results.getRight())
			.containsWords("housing");
		}
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
				WordDictSearchResult results =
					new MachineGeneratedDict().search(query, lang, (Integer) null);
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
//			.onlyCaseNums(5)
//			.onlyCasesWithDescr("iu-out-of-vocab-yet-valid-dict-word")
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

		public String expStdRoman = null;
		public String expStdSyll = null;

		public String[] expTranslationsAmong = null;
		public String[] expHumanTranslations = null;
		public Integer expMinTranslations = null;
		public Integer expMaxTranslations = null;
		private boolean expHasTranslationsForOrigWord = true;
		public Integer expMinExamples = 0;
		public boolean producesSomeExamples = true;
		public Set<String> translationsWithNoExamples = new HashSet<String>();
		public boolean outOfVocab = false;
		private String[] expAdditionalL2Highlights = new String[0];

		public static Map<Object,Double> casesRunningTime = new HashMap<Object,Double>();

		public MultilingualDictCase(String _descr, String _word) {
			super(_descr, null);
			this.word = _word;
		}

		public MultilingualDictCase setL1(String _lang) throws RuntimeException {
			l1 = _lang;
			try {
				l2 = MDictEntry.otherLang(_lang);
			} catch (MachineGeneratedDictException e) {
				throw new RuntimeException(e);
			}
			return this;
		}

		public MultilingualDictCase setDefinition(String _expDefinition) {
			expDefinition = _expDefinition;
			return this;
		}

		public MultilingualDictCase hasDecomp(String... _expDecomp) {
			expDecomp = _expDecomp;
			return this;
		}

		public MultilingualDictCase relatedWordsShouldBeAmong(String... _expRelatedWords) {
			expRelatedWordsSuperset = _expRelatedWords;
			return this;
		}

		public MultilingualDictCase hasTranslationsForOrigWord(boolean exp) {
			this.expHasTranslationsForOrigWord = exp;
			return this;
		}

		public MultilingualDictCase bestTranslationsAreAmong(String... _expTranslations) {
			expTranslationsAmong = _expTranslations;
			return this;
		}

		public MultilingualDictCase hasMinTranslation(int _expMinTranslations) {
			expMinTranslations = _expMinTranslations;
			return this;
		}
		public MultilingualDictCase hasMaxTranslation(int _expMaxTranslations) {
			expMaxTranslations = _expMaxTranslations;
			return this;
		}

		public MultilingualDictCase hasMinExamples(Integer _expMinExamples) {
			expMinExamples = _expMinExamples;
			return this;
		}

		public MultilingualDictCase noExamplesForTranslations(String... _translationsWithNoExamples) {
			Collections.addAll(translationsWithNoExamples, _translationsWithNoExamples);
			return this;
		}

		public MultilingualDictCase noExamplesAtAll() {
			this.producesSomeExamples = false;
			return this;
		}

		public MultilingualDictCase humanTranslationsAre(String... _expHumanTranslations) {
			this.expHumanTranslations = _expHumanTranslations;
			return this;
		}

		public MultilingualDictCase setOutOfVocab(boolean _outOfVocab) {
			this.outOfVocab = true;
			return this;
		}

		public Object id() {
			return this.word;
		}

		public void registerRunningTime(Double time) {
			casesRunningTime.put(id(), time);
		}

		public MultilingualDictCase hasStandardSpelling(
			String _expStdRoman, String _expStdSyll) {
			this.expStdRoman = _expStdRoman;
			this.expStdSyll = _expStdSyll;
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
					new MachineGeneratedDict().canonizeTranslation(lang, origText);
				AssertString.assertStringEquals(
					aCase.descr, expText, gotText
				);
			} catch (MachineGeneratedDictException e) {
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
