package org.iutools.spellchecker;


import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Consumer;

import org.iutools.config.IUConfig;
import org.iutools.corpus.*;
import ca.nrc.datastructure.CloseableIterator;
import org.iutools.sql.SQLLeakMonitor;
import org.iutools.utilities.StopWatch;
import ca.nrc.testing.*;

import static ca.nrc.testing.RunOnCases.Case;


import org.iutools.text.segmentation.IUTokenizer;
import org.junit.jupiter.api.*;

import static ca.nrc.testing.AssertIterator.assertContainsAll;

public class SpellCheckerTest {

	private TestInfo testInfo;

	private static enum TestOption {ENABLE_PARTIAL_CORRECTIONS};

	private static final String emptyCorpusName = "empty-corpus";

	protected SpellChecker checkerSyll = null;
	
	// Note: These are not "real" correct words in Inuktut.
	//  Just pretending that they are by putting them in the
	//  spell checker's explicitly correct dictionary
	//
	protected static final String[] explicitlyCorrectWordsLatin = new String[] {
		"inuktut", "inukttut", "inuk", "inukutt", "inukshuk", 
		"nunavut", "inuktitut"
	};

	protected static final String[] correctWordsNonExplicit = new String[] {
		"maligaliuqti", "juu", "niruaqtaukkannilauqpuq", "niruavigjuarnautillugu",
		"utupiri", "tallimanganni", "maligalirvingmit", "nunavummi"
	};

	String[] misspelledWords =
		new String[] {
			"nakuqmi", "nunavungmi", "nunavuumik", "nunavuumit",
			"ugaalautaa"};

	public static SQLLeakMonitor sqlLeakMonitor = null;

	@BeforeEach
	public void setUp(TestInfo _testInfo) throws Exception {
		this.testInfo = _testInfo;

		sqlLeakMonitor = new SQLLeakMonitor();
		
		// Make sure the ES indices are empty for the empty corpus name
		clearESIndices(new SpellChecker(emptyCorpusName, false));
		return;
	}

	@AfterEach
	public void tearDown() {
		sqlLeakMonitor.assertNoLeaks();
	}

	protected SpellChecker largeDictChecker() throws Exception {
		SpellChecker checker = new SpellChecker(CompiledCorpusRegistry.defaultCorpusName);
		return checker;
	}

	protected SpellChecker smallDictChecker() throws Exception {
		SpellChecker checker = new SpellChecker(emptyCorpusName, false);
		return checker;
	}

	private void clearESIndices(SpellChecker checker) throws Exception {
		if (!checker.corpusIndexName().equals(emptyCorpusName)) {
			throw new Exception(
					"You are only allowed to clear the ES index that corresponds to a corpus that is meant to be initially empty!!");
		}

		CompiledCorpus corpus = checker.corpus;
		corpus.deleteAll(true);

		corpus = checker.explicitlyCorrectWords;
		corpus.deleteAll(true);

		Thread.sleep(100);

		return;
	}

	protected SpellChecker makeCheckerEmptyDict() throws Exception {
		SpellChecker checker =
			new SpellChecker(
				emptyCorpus().getIndexName(), false);
		checker.setVerbose(false);
		return checker;
	}

	protected CompiledCorpus largeESCorpus() throws Exception {
		CompiledCorpus corpus =
			RW_CompiledCorpus.read(largeESCorpusFile());
		return corpus;
	}

	protected File largeESCorpusFile() throws Exception {
		File corpusFile = new File(IUConfig.getIUDataPath("data/compiled-corpuses/HANSARD-1999-2002.ES.json"));
		return corpusFile;
	}

	protected CompiledCorpus emptyCorpus() throws Exception {
		CompiledCorpus corpus = CompiledCorpusRegistry.makeCorpus("empty-corpus");
		corpus.deleteAll(true);
		return corpus;
	}

	protected SpellChecker largeDictCheckerWithTestWords() throws Exception {
		SpellChecker checker = largeDictChecker();
		checker.setVerbose(false);
		addCorrectWordsLatin(checker);
		return checker;
	}

	protected SpellChecker smallDictCheckerWithTestWords() throws Exception {
		SpellChecker checker = smallDictChecker();
		checker.setVerbose(false);
		addCorrectWordsLatin(checker);
		return checker;
	}

	private void addCorrectWordsLatin(SpellChecker checker) throws Exception {
		for (String aWord: explicitlyCorrectWordsLatin) {
			checker.addExplicitlyCorrectWord(aWord);
		}

		if (checker instanceof SpellChecker) {
			// Sleep a bit to allow the ElasticSearch index to synchronize
			Thread.sleep(100);
		}
	}

	//////////////////////////////////////////////////
	// DOCUMENTATION TESTS
	//////////////////////////////////////////////////

	@Test
	public void test__SpellChecker__Synopsis() throws Exception {
	 	//
		// Use this class to spell check Inuktut words
		//
		SpellChecker checker = new SpellChecker();

		// SpellChecker requires a CompiledCorpus to do its work.
		// If no specific corpus is provided, it uses the default
		// corpus (as defined in the CompiledCorpusRegistry).
		//
		// However, you can provide the checker with a specific
		// corpus name:
		//
		checker = new SpellChecker(CompiledCorpusRegistry.defaultCorpusName);

		//
		// You can then use the checker to see if a word is mis-spelled, and if so, 
		// get a list of plausible corrections for that word
		//
		String wordWithError = "inusuk";
		int nCorrections = 5;
		SpellingCorrection correction = checker.correctWord(wordWithError, nCorrections);
		if (correction.wasMispelled) {
			List<String> possibleCorrectSpellings = correction.getDeepSuggestions();
			// This is the longest leading string of the word which might 
			// be correctly spelled
			String correctLead = correction.getCorrectLead();
			// This is the longest tailing string of the word which might 
			// be correctly spelled
			String correctTail = correction.getCorrectTail();
		}
		
		//
		// You can also get corrections for all words in a text
		//
		String text = "inuit inusuk nunnavut";
		List<SpellingCorrection> corrections2 = checker.correctText(text, nCorrections);

		//
		// If the SpellChecker mistakenly labels a word as being
		// mis-spelled, you can explicitly tell the checker that the
		// word is OK.
		//
		// For example...
		//
		checker.addExplicitlyCorrectWord("inuktut");
		checker.addExplicitlyCorrectWord("inuk");
		checker.addExplicitlyCorrectWord("inuksuk");
		checker.addExplicitlyCorrectWord("nunavut");
		checker.addExplicitlyCorrectWord("1988-mut");

		// There are 3 possible levels of spellchecking. As the level increases,
		// the spellchecker will catch more mistakes and provide better suggestions,
		// but it also gets slower.
		//
		// For more details about what the different levels do, see Javadoc for
		// SpellChecker.checkLevel atrribute.
		//
		// By default, the SpelleChecker uses Level 3, which is the maximum.
		// But you can change it.
		//
		checker.setCheckLevel(1);
	}
	
		
	/**********************************
	 * VERIFICATION TESTS
	 **********************************/

	@Test
	public void test__addCorrectWord__HappyPath() throws Exception {
		SpellChecker checker = makeCheckerEmptyDict();
		Thread.sleep(1000);
		checker.setVerbose(false);
		
		String word = "inukkkutttt";
		Assertions.assertTrue(
			checker.isMispelled(word),
			"Initially, word "+word+" should have been deemed mis-spelled");
		
		checker.addExplicitlyCorrectWord(word);
		Thread.sleep(1000);
		Assertions.assertFalse(
			checker.isMispelled(word),
			"After being explicitly labelled as correct, word "+word+
						" should NOT have been deemed mis-spelled");
	}
	
	@Test 
	public void test__wordsContainingNgram() throws Exception {
		String seq = "nukt";
		
		SpellChecker checker = largeDictCheckerWithTestWords();
		
		try (CloseableIterator<String> wordsWithSeq =
			  	checker.wordsContainingNgram(seq)) {
			String[] expected = new String[]{"inuktut", "inuktitut"};
			assertContainsAll(
			"The list of words containing sequence " + seq + " was not as expected",
			expected, wordsWithSeq);
		}
	}
	
	@Test 
	public void test__wordsContainingNgram__Case_considering_extremities() throws Exception {
		SpellChecker checker = largeDictCheckerWithTestWords();
		checker.setVerbose(false);
		checker.addExplicitlyCorrectWord("inuktitut");
		checker.addExplicitlyCorrectWord("inuksuk");
		checker.addExplicitlyCorrectWord("inuttitut");
		checker.addExplicitlyCorrectWord("inakkut");
		checker.addExplicitlyCorrectWord("takuinuit");
		checker.addExplicitlyCorrectWord("taku");
		checker.addExplicitlyCorrectWord("intakuinuit");
		
		String seq;
		String[] expected;
		String[] unexpected;

		seq = "inukt";
		try (CloseableIterator<String> wordsWithSeq = checker.wordsContainingNgram(seq)) {
			expected = new String[] {"inuktitut","inuktaluk","inuktigut"};
			assertContainsAll(
			"The list of words containing sequence "+seq+" was not as expected",
				expected, wordsWithSeq);
		}

		seq = "^inukt";
		try (CloseableIterator<String>wordsWithSeq = checker.wordsContainingNgram(seq)) {
			expected = new String[]{"inuktitut", "inuktaluk", "inuktigut"};
			assertContainsAll(
			"The list of words containing sequence " + seq + " was not as expected",
			expected, wordsWithSeq);
			// This word contains inukt, but not at the start of the word
			unexpected = new String[]{"qaujijumatuinnaqtungainuktituunganingit"};
			AssertIterator.assertContainsNoneOf(
			"The list of words containing sequence " + seq + " was not as expected",
			unexpected, wordsWithSeq);
		}

		seq = "itut$";
		try (CloseableIterator<String> wordsWithSeq = checker.wordsContainingNgram(seq)) {
			expected = new String[]{"inuktitut", "inuttitut"};
			assertContainsAll(
			"The list of words containing sequence " + seq + " was not as expected",
			expected, wordsWithSeq);
			// This word contains itut, but not at the end
			unexpected = new String[]{"jiiqatigiituta"};
			AssertIterator.assertContainsNoneOf(
			"The list of words containing sequence " + seq + " was not as expected",
			unexpected, wordsWithSeq);
		}

		seq = "^taku$";
		try (CloseableIterator<String> wordsWithSeq = checker.wordsContainingNgram(seq)) {
			expected = new String[]{"taku"};
			assertContainsAll(
			"The list of words containing sequence " + seq + " was not as expected",
			expected, wordsWithSeq);

			// This word contains taku, but it is not bounded by start and end of word
			unexpected = new String[]{"aktakuniglu"};
			AssertIterator.assertContainsNoneOf(
			"The list of words containing sequence " + seq + " was not as expected",
			unexpected, wordsWithSeq);
		}
	}
	
	
	@Test 
	public void test__firstPassCandidates_TFIDF() throws Exception {
		SpellChecker checker = smallDictCheckerWithTestWords();
		
		String badWord = "inukkshuk";
		List<ScoredSpelling> candidates =
			checker.candidatesWithSimilarNgrams(badWord, false);

		String[] expected = new String[] {
			"inuk", "inukshuk", "inuktitut", "inukttut", "inuktut",
			"inukutt"};
		AssertSpellingCorrection
			.candidatesEqual(expected, candidates);
	}

	@Test
	public void test__correctWord__VariousCases() throws Exception {
		CaseCorrectWord[] cases = new CaseCorrectWord[] {

			new CaseCorrectWord("Number - Level 2",
				"1987", false),

			new CaseCorrectWord("SYLL mis-spelled (nunavungmi) - Level 2",
				"ᓄᓇᕗᖕᒥ", true)
				.usingCheckLevel(2)
				.expectCorrections("ᓄᓇᕗᒻᒥ", "ᓄᓇᕘᒻᒥ", "ᓄᓇᕕᐊᓗᖕᒥ", "ᓄᓇᕗᖓ",
					"ᓄᓇᕗᒻᒥᑦ"),

			new CaseCorrectWord("ROMAN correctly spelled - Level 2",
				"inuksuk", false)
				.usingCheckLevel(2),

			new CaseCorrectWord("numeric expression - Level 2", "1987-mut", false)
				.usingCheckLevel(2),

			new CaseCorrectWord("Level 3 - Correct leading and tailing portions DO NOT overlap", "inuktigtut", true)
				.usingCheckLevel(3)
				.expectCorrections(
					"inukti[g]tut", "inukti[gtu]t", "inuktitut", "inukkitut", "inuktut", "inuktikut",
					"qinuktitut"),

			new CaseCorrectWord("Level 3 - Correct leading and tailing portions OVERLAP", "ujaranniarvimmi", true)
				.usingCheckLevel(3)
				.expectCorrections(
					"ujararniarvimmi", "ujararniarvimmik", "ujararniarvimmit",
					"ujararniarvimmut", "ujarattarniarvimmi"),

			// This case has been known to cause problems because it's correted form
			// mi[sta]/ᒥ[ᔅᑕ] has a large proportion of non-IU chars.
			// This can cause problems when we transcode between ROMAN and SYLL
			// when we apply rules that are written in different scripts
			// (the transcoder doesn't transcode the word if it doesn't look like
			// it's an IU word.
			//
			new CaseCorrectWord("Level 1 - very short ROMAN word whose spelling will be flagged with [], possibly causing it to NOT look like an inuktitut word", "mista", true)
				.usingCheckLevel(1)
				.expectCorrections(
					"mi[sta]"),
		};

		Consumer<Case> runner = (caseNoCast) -> {
			try {
				CaseCorrectWord aCase = (CaseCorrectWord) caseNoCast;
				SpellChecker checker =
					new SpellChecker()
						.setCheckLevel(aCase.checkLevel);
				SpellingCorrection gotCorrection =
					checker.correctWord(aCase.origWord);
				AssertSpellingCorrection asserter =
					new AssertSpellingCorrection(gotCorrection,
						"Correction not as expected for case: "+aCase.descr)
					.misspelledStatusWas(aCase.expMisspelled);
				if (aCase.expSuggestions != null) {
					asserter.providesSuggestions(aCase.expSuggestions);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};

		new RunOnCases(cases, runner)
//			.onlyCaseNums(5)
//			.onlyCasesWithDescr("Level 1 - ROMAN word that has a Level 1 mistake which operates on SYLL form")
			.run();
	}

	@Test
	public void test__correctText__roman() throws Exception  {
		String text = "inuktut ninavut inuit inuktut";
		
		SpellChecker checker = smallDictCheckerWithTestWords();
		
		List<SpellingCorrection> gotCorrections = checker.correctText(text);
		
		// Note we skip every other "correction" because they are just blank spaces
		// between words
		
		int wordNum = 0;
		int ii = 2*wordNum;
		SpellingCorrection wordCorr = gotCorrections.get(ii);
		assertCorrectionOK("Correction for word #"+wordNum+"="+wordCorr.orig+" was not as expected", wordCorr,  "inuktut", true);
		

		wordNum = 1;
		ii = 2*wordNum;
		wordCorr = gotCorrections.get(ii);
		Assertions.assertTrue(wordCorr.wasMispelled, "Word #"+wordNum+"="+wordCorr.orig+" should have deemed MISPELLED");
		AssertObject.assertDeepEquals("Corrections for word#"+wordNum+"="+wordCorr.orig+" were not as expected", 
				new String[] {"nunavut"}, 
				wordCorr.getDeepSuggestions());

		wordNum = 2;
		ii = 2*wordNum;
		wordCorr = gotCorrections.get(ii);
		Assertions.assertFalse(wordCorr.wasMispelled, "Word #"+ii+"="+wordCorr.orig+" should have deemd correctly spelled");

		wordNum = 3;
		ii = 2*wordNum;
		wordCorr = gotCorrections.get(ii);
		Assertions.assertFalse(wordCorr.wasMispelled, "Word #"+ii+"="+wordCorr.orig+" should have deemd correctly spelled");
	}	
	


	@Test  
	public void test__correctText__syllabic() throws Exception  {
		SpellChecker checker = smallDictCheckerWithTestWords();
		
		String text = "ᐃᓄᑦᒧᑦ ᑕᑯᔪᖅ ᐃᒡᓗᑦᒥᒃ ᐊᕐᕌᒍᒥ";
		List<SpellingCorrection> gotCorrections = checker.correctText(text);
		
		
		int wordNum = 0;
		int ii = 2*wordNum;
		SpellingCorrection wordCorr = gotCorrections.get(ii);
		assertCorrectionOK("Correction for word #"+wordNum+"="+wordCorr.orig+" was not as expected",
				wordCorr, "ᐃᓄᑦᒧᑦ", false, 
				// Note: The correct spelling is not in this list of corrections, but that's OK (kinda). 
				//   This test mostly aims at testing the mechanics of SpellText.
				//  				
				new String[] {
					"ᐃᓄᒃᑐᑦ",
				  	"ᐃᓄᑯᑦᑦ",
				  	"ᐃᓄᒃᑎᑐᑦ",
				  	"ᐃᓄᒃ",
					"ᐃᓄᒃᑦᑐᑦ"
				});
		
		wordNum = 1;
		ii = 2*wordNum;
		wordCorr = gotCorrections.get(ii);
		assertCorrectionOK("Correction for word #"+wordNum+"="+wordCorr.orig+" was not as expected",
				wordCorr, "ᑕᑯᔪᖅ", true);

		wordNum = 2;
		ii = 2*wordNum;
		wordCorr = gotCorrections.get(ii);
		assertCorrectionOK("Correction for word #"+wordNum+"="+wordCorr.orig+" was not as expected",
				wordCorr, "ᐃᒡᓗᑦᒥᒃ", false, 
				// Note: The correct spelling is not in this list of corrections, but that's OK (kinda). 
				//   This test mostly aims at testing the mechanics of SpellText.
				//  				
				new String[] {});

		wordNum = 3;
		ii = 2*wordNum;
		wordCorr = gotCorrections.get(ii);
		assertCorrectionOK("Correction for word #"+wordNum+"="+wordCorr.orig+" was not as expected",
				wordCorr, "ᐊᕐᕌᒍᒥ", true);
	}	


	@Test 
	public void test__correctText_ampersand() throws Exception {
		SpellChecker checker = largeDictCheckerWithTestWords();
		String text = "inuktut sinik&uni";
		List<SpellingCorrection> gotCorrections = checker.correctText(text);
		Assertions.assertEquals(
			3,gotCorrections.size(),
		"The number of corrections is not as expected.");
	}

	@Test
	public void test__isMispelled__VariousCases() throws Exception {
		CaseIsMispelled[] cases = new CaseIsMispelled[] {

			new CaseIsMispelled("ROMAN, correctly spelled", "inuit")
				.usingCheckLevel(2)
				.expectMisspelled(false),

			new CaseIsMispelled(
				"SYLL text that uses two chars ᓕ+ᓐ instead of just one", "ᐃᓕᓐᓂᐊᕐᑭᑎ")
				.usingCheckLevel(1)
				.expectMisspelled(true),

			new CaseIsMispelled(
				"English word", "computing")
				.usingCheckLevel(1)
				.expectMisspelled(false),
		};

		Consumer<Case> runner = (caseNoCast) -> {
			try {
				CaseIsMispelled aCase = (CaseIsMispelled) caseNoCast;
				SpellChecker checker =
					new SpellChecker()
						.setCheckLevel((aCase.useDeepChecking?2: 1));
				boolean gotAnswer = checker.isMispelled(aCase.word);
				Assertions.assertEquals(aCase.expMisspelled, gotAnswer,
					"Mis-spelled status not as expected for word: "+aCase.word);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
		new RunOnCases(cases, runner)
//			.onlyCaseNums(3)
			.run();
	}

	@Test
	public void test__isMispelled__WordContainsAnAbsoluteSpellingMistake() throws Exception  {
		// This word contains an 'absolute' spelling mistake: 'qj' can NEVER
		// happen in a correctly spelled world.
		String word = "inuqjuq";
		Assertions.assertTrue(largeDictCheckerWithTestWords().isMispelled(word),
		"Word "+word+" should NOT have been deemed correctly spelled");
	}


	@Test 
	public void test__isMispelled__CorreclySpelledWordFromCompiledCorpus() throws Exception  {
		String word = "inuktitut";
		Assertions.assertFalse(largeDictCheckerWithTestWords().isMispelled(word),
		"Word "+word+" should have been deemed correctly spelled");
	}

	@Test 
	public void test__isMispelled__CorreclySpelledWordNOTFromCompiledCorpus() throws Exception  {
		SpellChecker checker = largeDictCheckerWithTestWords();
		
		String word = "inuktut";
		Assertions.assertFalse(checker.isMispelled(word),
		"Word "+word+" should have been deemed correctly spelled");
	}

	@Test 
	public void test__isMispelled__CorreclySpelledWordNumber() throws Exception  {
		String word = "2018";
		Assertions.assertFalse(largeDictCheckerWithTestWords().isMispelled(word),
		"Word "+word+" should have been deemed correctly spelled");
	}

	@Test
	public void test__isMispelled__WordThatContainsAnAbsoluteMistake() throws Exception  {
		String word = "titiqkaq";
		Assertions.assertTrue(largeDictCheckerWithTestWords().isMispelled(word),
		"Word "+word+" should have been deemed misspelled");
	}

	@Test 
	public void test__isMispelled__MispelledWordFromCompiledCorpus() throws Exception  {
		SpellChecker checker = largeDictCheckerWithTestWords();
		
		String word = "inukkkkutt";
		Assertions.assertTrue(checker.isMispelled(word),
		"Word "+word+" should have been deemed mis-spelled");
	}

	@Test 
	public void test__isMispelled__MispelledWordNOTFromCompiledCorpus() throws Exception  {
		SpellChecker checker = largeDictCheckerWithTestWords();
		
		String word = "inuktuttt";
		Assertions.assertTrue(checker.isMispelled(word),
		"Word "+word+" should have been deemed mis-spelled");
	}
	
	@Test 
	public void test__isMispelled__WordIsSingleInuktitutCharacter() throws Exception  {
//		SpellChecker checker = makeCheckerLargeDict();
		SpellChecker checker = smallDictCheckerWithTestWords();
		
		String word = "ti";
		Assertions.assertFalse(checker.isMispelled(word),
		"Word "+word+" should have been deemed correctly spelled");
		word = "t";
		Assertions.assertFalse(checker.isMispelled(word), "Word "+word+" should have been deemed correctly spelled");
	}
	
	@Test 
	public void test__isMispelled__WordIsNumericTermWithValidEnding() throws Exception  {
		SpellChecker checker = largeDictCheckerWithTestWords();
		
		String word = "1988-mut";
		Assertions.assertFalse(checker.isMispelled(word), "Word "+word+" should have been deemed correctly spelled");
		word = "1988-muti";
		Assertions.assertTrue(checker.isMispelled(word), "Word "+word+" should have been deemed correctly spelled");
	}
	
	@Test 
	public void test__WordContainsMoreThanTwoConsecutiveConsonants() throws Exception  {
		SpellChecker checker = largeDictCheckerWithTestWords();
		String word = "imglu";
		Assertions.assertTrue(checker.wordContainsMoreThanTwoConsecutiveConsonants(word),
		"Word "+word+" should have been acknowledged as having more than 2 consecutive consonants");
		word = "inglu";
		Assertions.assertFalse(checker.wordContainsMoreThanTwoConsecutiveConsonants(word),
		"Word "+word+" should have been acknowledged as not having more than 2 consecutive consonants");
		word = "innglu";
		Assertions.assertTrue(checker.wordContainsMoreThanTwoConsecutiveConsonants(word),
		"Word "+word+" should have been acknowledged as having more than 2 consecutive consonants");
	}
	
	@Test 
	public void test__wordIsNumberWithSuffix() throws Exception  {
		SpellChecker checker = largeDictCheckerWithTestWords();
		
		String word = "34-mi";
		String[] numericTermParts = checker.splitNumericExpression(word);
		Assertions.assertTrue(numericTermParts != null, "Word "+word+" should have been acknowledged as a number-based word");
		AssertString.assertStringEquals("The 'number' part is not as expected.", "34-", numericTermParts[0]);
		AssertString.assertStringEquals("The 'ending' part is not as expected.", "mi", numericTermParts[1]);
		word = "$34,000-mi";
		numericTermParts = checker.splitNumericExpression(word);
		Assertions.assertTrue(numericTermParts != null, "Word "+word+" should have been acknowledged as a number-based word");
		AssertString.assertStringEquals("The 'number' part is not as expected.", "$34,000-", numericTermParts[0]);
		AssertString.assertStringEquals("The 'ending' part is not as expected.", "mi", numericTermParts[1]);
		word = "4:30-mi";
		numericTermParts = checker.splitNumericExpression(word);
		Assertions.assertTrue(numericTermParts != null, "Word "+word+" should have been acknowledged as a number-based word");
		AssertString.assertStringEquals("The 'number' part is not as expected.", "4:30-", numericTermParts[0]);
		AssertString.assertStringEquals("The 'ending' part is not as expected.", "mi", numericTermParts[1]);
		word = "5.5-mi";
		numericTermParts = checker.splitNumericExpression(word);
		Assertions.assertTrue(numericTermParts != null, "Word "+word+" should have been acknowledged as a number-based word");
		AssertString.assertStringEquals("The 'number' part is not as expected.", "5.5-", numericTermParts[0]);
		AssertString.assertStringEquals("The 'ending' part is not as expected.", "mi", numericTermParts[1]);
		word = "5,500.33-mi";
		numericTermParts = checker.splitNumericExpression(word);
		Assertions.assertTrue(numericTermParts != null, "Word "+word+" should have been acknowledged as a number-based word");
		AssertString.assertStringEquals("The 'number' part is not as expected.", "5,500.33-", numericTermParts[0]);
		AssertString.assertStringEquals("The 'ending' part is not as expected.", "mi", numericTermParts[1]);
		word = "bla";
		numericTermParts = checker.splitNumericExpression(word);
		Assertions.assertTrue(numericTermParts == null, "Word "+word+" should have been acknowledged as a number-based word");
		word = "34–mi";
		numericTermParts = checker.splitNumericExpression(word);
		Assertions.assertTrue(numericTermParts != null, "Word "+word+" should have been acknowledged as a number-based word");
		AssertString.assertStringEquals("The 'number' part is not as expected.", "34–", numericTermParts[0]);
		AssertString.assertStringEquals("The 'ending' part is not as expected.", "mi", numericTermParts[1]);
		word = "40−mi";
		numericTermParts = checker.splitNumericExpression(word);
		Assertions.assertTrue(numericTermParts != null, "Word "+word+" should have been acknowledged as a number-based word");
		AssertString.assertStringEquals("The 'number' part is not as expected.", "40−", numericTermParts[0]);
		AssertString.assertStringEquals("The 'ending' part is not as expected.", "mi", numericTermParts[1]);
	}
	
	@Test 
	public void test__assessEndingWithIMA() throws Exception  {
		SpellChecker checker = largeDictCheckerWithTestWords();
		
		String ending = "mi";
		boolean goodEnding = checker.assessEndingWithIMA(ending);
		Assertions.assertTrue(goodEnding, "Ending "+ending+" should have been acknowledged as valid word ending");
		ending = "mitiguti";
		goodEnding = checker.assessEndingWithIMA(ending);
		Assertions.assertFalse(goodEnding, "Ending "+ending+" should have been rejected as valid word ending");
		ending = "gumut";
		goodEnding = checker.assessEndingWithIMA(ending);
		Assertions.assertFalse(goodEnding, "Ending "+ending+" should have been rejected as valid word ending");
	}
	
	@Test 
	public void test__wordIsPunctuation() throws Exception  {
		SpellChecker checker = largeDictCheckerWithTestWords();
		
		String word = "-";
		Assertions.assertTrue(checker.wordIsPunctuation(word), "Word "+word+" should have been acknowledged as punctuation");
		word = "–";
		Assertions.assertTrue(checker.wordIsPunctuation(word), "Word "+word+" should have been acknowledged as punctuation");
	}
	
	@Test 
	public void test__addWord__HappyPath() throws Exception {
		SpellChecker checker = makeCheckerEmptyDict();
		String word = "tamainni";
		assertWordUnknown(word, checker);
		
		checker.addExplicitlyCorrectWord(word);
		assertWordIsKnown(word, checker);
	}	
	
	@Test
	public void test__SpeedTest__AllOKWords() throws Exception {
		// Spell checking should be pretty fast for words that are
		// correctly spelled
		//
		speedTest("All correctly spelled words",
			correctWordsNonExplicit);
	}

	@Test
	public void test__SpeedTest__AllMisspelledWords__DisablePartialCorrections() throws Exception {
		// Spell checking is somewhat slow when dealing with words that are
		// mis-spelled. This is even true if we do not enable computation of
		// partial corrections.
		//
		speedTest(
			"All misspelled words",
			misspelledWords);
	}

	@Test
	public void test__SpeedTest__AllMisspelledWords__EnablePartialCorrections() throws Exception {
		speedTest(
			"All misspelled words",
			misspelledWords, TestOption.ENABLE_PARTIAL_CORRECTIONS);
	}

	private void speedTest(String dataSetName, String[] words,
		TestOption... options) throws Exception {
		SpellChecker checker = largeDictCheckerWithTestWords();

		long startAll = StopWatch.nowMSecs();

		double totalSecs = 0;
		int totalWords = words.length;
		if (totalWords == 0) {
			throw new SpellCheckerException("Empty data set");
		}
		boolean enablePartialCorrections =
			Arrays.stream(options)
				.anyMatch(TestOption.ENABLE_PARTIAL_CORRECTIONS::equals);
			checker.setCheckLevel((enablePartialCorrections?3:2));
			for (String word: words) {
				Long start = System.currentTimeMillis();
				SpellingCorrection gotCorrection = checker.correctWord(word);
				long end = System.currentTimeMillis();
				double elapsedSecsThisWord =
					1.0 * (end - start) 	/ 1000;
				totalSecs += elapsedSecsThisWord;
				totalWords++;
			}

			long endAll = StopWatch.elapsedMsecsSince(startAll);
			System.out.println("Test took "+endAll/1000+" seconds for "+words.length+" words");

			String baseMess =
				"With partial correction set to "+
				enablePartialCorrections+"...\n"+
				"On dataset: "+dataSetName+"...\n";

			Double gotAvgSecs= avgSecs(totalSecs, totalWords);

			Double percTolerance = 0.50;
			AssertRuntime.runtimeHasNotChanged(
				gotAvgSecs, percTolerance, "avg spell check time", testInfo);
		}

	private Double avgSecs(double totalSecs, int totalWords) {
		double avg = 0.0;
		if (totalWords != 0) {
			avg = totalSecs / totalWords;
			BigDecimal bd = BigDecimal.valueOf(avg);
			bd = bd.setScale(4, RoundingMode.HALF_UP);
			avg = bd.doubleValue();
		}
		return avg;
	}

	@Test 
	public void test__computeCorrectPortions__HappyPath() throws Exception {
		SpellChecker checker = smallDictCheckerWithTestWords();
		
		String badWord = "inuktiqtut";
		SpellingCorrection correction = 
				new SpellingCorrection(badWord, new String[0], true);
		checker.computeCorrectPortions(correction);
		AssertSpellingCorrection.assertThat(correction, "")
			.highlightsIncorrectTail("inukti")
			.highlightsIncorrectLead("tut")
			.highlightsIncorrectMiddle("inukti[q]tut")
		;
	}
	
	@Test 
	public void test__leadRespectsMorphemeBoundaries__HappyPath() throws Exception {
		String word = "inuktitut";
		SpellChecker checker = smallDictCheckerWithTestWords();
		
		String leadChars = "inuk";
		Assertions.assertTrue(
		checker.leadRespectsMorphemeBoundaries(leadChars, word),
		"Lead morphemes for word "+word+" SHOULD have matched "+leadChars);
		
		leadChars = "inukt";
		Assertions.assertFalse(
		checker.leadRespectsMorphemeBoundaries(leadChars, word),
		"Lead morphemes for word "+word+" should NOT have matched "+leadChars);
	}

	@Test 
	public void test__tailRespectsMorphemeBoundaries__HappyPath() throws Exception {
		String word = "inuktitut";
		SpellChecker checker = smallDictCheckerWithTestWords();
		
		String tailChars = "tut";
		Assertions.assertTrue(
		checker.tailRespectsMorphemeBoundaries(tailChars, word),
		"Tail morphemes for word "+word+" SHOULD have matched "+tailChars);
		
		tailChars = "itut";
		Assertions.assertFalse(
		checker.tailRespectsMorphemeBoundaries(tailChars, word),
		"Tail morphemes for word "+word+" should NOT have matched "+tailChars);
	}

	@Test
	public void test__winfosContainingNgram__HappyPath() throws Exception {
		SpellChecker checker = largeDictCheckerWithTestWords();
		String ngram = "^nuna";
		String[] expWords = new String[] {
			"nunavu", "nunalinni", "nunavummi"
		};
		try (CloseableIterator<WordInfo> iter = checker.winfosContainingNgram(ngram)) {
			assertContainsWords(expWords, iter, 10);
		}
	}

	private void assertContainsWords(String[] expWords, Iterator<WordInfo> iter, int inTopN) {
		Set<String> gotWords = new HashSet<String>();
		while (iter.hasNext() && gotWords.size() <= inTopN) {
			WordInfo nextWord = iter.next();
			gotWords.add(nextWord.word);
		}
		AssertCollection.assertContainsAll(
			"WordInfo iterator did not include the expected words",
			expWords, gotWords);
	}

	/**********************************
	 * TEST HELPERS
	 **********************************/
	
	private boolean containsNumericTerm(String numericTerm, SpellChecker checker) {
		String[] numericTermParts = checker.splitNumericExpression(numericTerm);
		boolean answer = (numericTermParts != null);
		return answer;
	}

	private void assertCorrectionOK(String mess, SpellingCorrection wordCorr, String expOrig, boolean expOK) throws Exception {
		assertCorrectionOK(mess, wordCorr, expOrig, expOK, null);
		
	}
	
	private void assertCorrectionOK(SpellingCorrection wordCorr, String expOrig, 
			boolean expOK, String[] expSpellings) throws Exception {
		assertCorrectionOK("", wordCorr, expOrig, expOK, expSpellings, 
				null, null);
	}
	
	private void assertCorrectionOK(SpellingCorrection wordCorr, String expOrig, 
			boolean expOK) throws Exception {
		assertCorrectionOK("", wordCorr, expOrig, expOK, new String[] {});
	}
	
	private void assertCorrectionOK(String mess, SpellingCorrection wordCorr, 
			String expOrig, boolean expOK, String[] expSpellings) 
					throws Exception {
		assertCorrectionOK(mess, wordCorr, 
				expOrig, expOK, expSpellings, null, null);
	}
	
	private void assertCorrectionOK(String mess, SpellingCorrection wordCorr, 
			String expOrig, boolean expOK, String[] expTopSpellings,
			String expCorrLead, String expCorrTail) throws Exception {
		if (expTopSpellings == null) {
			expTopSpellings = new String[] {};
		}
		Assertions.assertEquals(
			expOrig, wordCorr.orig,
			mess+"\nThe input word was not as expected");
		Assertions.assertEquals(
			expOK, !wordCorr.wasMispelled,
			mess+"\nThe correctness status was not as expected");

		List<String> gotTopSpellings = wordCorr.getDeepSuggestions();
		gotTopSpellings =
			gotTopSpellings
				.subList(
					0,
					Math.min(gotTopSpellings.size(), expTopSpellings.length));
		AssertObject.assertDeepEquals(
				mess+"\nThe list of spellings was not as expected", 
				expTopSpellings, gotTopSpellings);
		
		if (expCorrLead != null) {
			Assertions.assertEquals(
					"Longest Correctly Spelled leading string was not as expected", 
					expCorrLead, wordCorr.getCorrectLead());
		}

		if (expCorrTail != null) {
			Assertions.assertEquals(
					"Longest Correctly tailing string was not as expected", 
					expCorrLead, wordCorr.getCorrectTail());
		}
	}

	private void assertWordIsKnown(String word, SpellChecker checker)
		throws Exception {
		Assertions.assertTrue(checker.knowsWord(word),
		"Spell checker dictionary did not know about word '"+word+"'");
	}

	private void assertWordUnknown(String word, SpellChecker checker)
		throws SpellCheckerException {
		Assertions.assertFalse(checker.knowsWord(word),
		"Spell checker dictionary should NOT have known about word '"+word+"'");
	}

	protected List<String> wordsInText(String text) {
		IUTokenizer iutokenizer = new IUTokenizer();
		List<String> words = iutokenizer.tokenize(text);
		return words;
	}

	public static class CaseIsMispelled extends Case {

		public String word = null;
		public Integer checkLevel = null;
		public boolean expMisspelled = false;
		public boolean useDeepChecking = true;

		public CaseIsMispelled(String _descr, String _word) {
			super(_descr, null);
			this.word = _word;
		}

		public CaseIsMispelled usingCheckLevel(int level) {
			this.checkLevel = level;
			return this;
		}

		public CaseIsMispelled expectMisspelled(boolean expected) {
			this.expMisspelled = expected;
			return this;
		}
	}

	public static class CaseCorrectWord extends Case {

		String origWord = null;
		boolean expMisspelled = false;
		String[] expSuggestions = null;
		int checkLevel = 3;

		public CaseCorrectWord(String _descr, String _origWord, boolean _expMisspelled) {
			super(_descr, null);
			origWord = _origWord;
			expMisspelled = _expMisspelled;
		}

		public CaseCorrectWord expectCorrections(String... _expCorrections) {
			if (!expMisspelled && _expCorrections.length > 0) {
				throw new RuntimeException("Cannot provide expected corrections if we expect the word to be correctly spelled!");
			}
			expSuggestions = _expCorrections;
			return this;
		}

		public CaseCorrectWord usingCheckLevel(int level) {
			checkLevel = level;
			return this;
		}
	}
}
