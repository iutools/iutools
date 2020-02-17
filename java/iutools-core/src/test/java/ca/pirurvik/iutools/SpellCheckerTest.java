package ca.pirurvik.iutools;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import ca.nrc.datastructure.Pair;
import ca.nrc.json.PrettyPrinter;
import ca.nrc.testing.AssertHelpers;
import ca.pirurvik.iutools.SpellChecker;

import org.junit.*;

public class SpellCheckerTest {
	
	private SpellChecker checker = null;
	private SpellChecker checkerSyll = null;
	
	@Before
	public void setUp() throws Exception {
		
		String[] correctWordsLatin = new String[] {"inuktut", "inukttut", "inuk", "inukutt", "inukshuk", "nunavut"};
		if (checker == null) {
			checker = new SpellChecker();
			checker.setVerbose(false);
			for (String aWord: correctWordsLatin) checker.addCorrectWord(aWord);
		}
	}

	@Test(expected=SpellCheckerException.class)
	public void test__SpellChecker__Synopsis() throws Exception {
		//
		// Before you can use a spell checker, you must first build its
		// dictionary of correct words. This can be done in 2 ways:
		// - directly by adding correct words to the dictionary;
		// - by specifying a corpus, the words of which will be added to the dictionary
		
		SpellChecker checker = new SpellChecker();
		checker.setVerbose(false);
		
		// 
		// For example
		//
		checker.addCorrectWord("inuktut");
		checker.addCorrectWord("inuk");
		checker.addCorrectWord("inuksuk");
		checker.addCorrectWord("nunavut");
		checker.addCorrectWord("1988-mut");
		// etc...
		
		// or
		
		checker.setDictionaryFromCorpus("a_corpus_name");
		
		//
		// Once the dictionary has been built, you can save the 
		// SpellChecker to file.
		//
		// Note: For the needs of this test, we use a temporary file that
		// will be deleted upon exit. But in a real use case, you would
		// save it to a permanent file.
		//
		File checkerFile = File.createTempFile("checker", "json");
		checkerFile.deleteOnExit();
		checker.saveToFile(checkerFile);
		
		//
		// Later on, you can recreate the spell checker from file
		//
		checker = new SpellChecker().readFromFile(checkerFile);
		
		//
		// You can then use the checker to see if a word is mis-spelled, and if so, 
		// get a list of plausible corrections for that word
		//
		String wordWithError = "inusuk";
		int nCorrections = 5;
		SpellingCorrection correction = checker.correctWord(wordWithError, nCorrections);
		if (correction.wasMispelled) {
			List<String> possibleCorrectSpellings = correction.getPossibleSpellings();
		}
		
		//
		// You can also get corrections for all words in a text
		//
		String text = "inuit inusuk nunnavut";
		List<SpellingCorrection> corrections2 = checker.correctText(text, nCorrections);

	}
	
	/**********************************
	 * VERIFICATION TESTS
	 **********************************/
	
	@Test
	public void test__addCorrectWord__HappyPath() throws Exception {
		SpellChecker checker = new SpellChecker();
		checker.setVerbose(false);
		
		Assert.assertFalse(containsWord("inuktut", checker));
		checker.addCorrectWord("inuktut");
		Assert.assertTrue(containsWord("inuktut", checker));
		
		Assert.assertFalse(containsWord("nunavut", checker));
		checker.addCorrectWord("nunavut");
		Assert.assertTrue(containsWord("nunavut", checker));
		
		Assert.assertFalse(containsNumericTerm("1988-mut", checker));
		checker.addCorrectWord("1988-mut");
		Assert.assertTrue(containsNumericTerm("1988-mut", checker));
		
		
	}
	
//	@Test - test removed; ngramStat() not used anymore
//	public void test__idf__HappyPath() {
//		Assert.assertEquals("IDF was wrong for sequence that starts words ('inu')", new Long(5), checker.ngramStat("inu"));
//		Assert.assertEquals("IDF was wrong for sequence at the middle of words ('ks')", new Long(1), checker.ngramStat("ks"));
//		Assert.assertEquals("IDF was wrong for sequence at the end of words ('ktut')", new Long(1), checker.ngramStat("ktut"));
//		Assert.assertEquals("IDF was wrong for sequence ('uk')", new Long(5), checker.ngramStat("uk"));
//		Assert.assertEquals("IDF was wrong for sequence with > 5 chars", new Long(0), checker.ngramStat("nunavu"));
//		Assert.assertEquals("IDF was wrong for sequence with =5 chars", new Long(1), checker.ngramStat("unavu"));
//		Assert.assertEquals("IDF was wrong for non-existant sequence",new Long(0), checker.ngramStat("blah"));
//	}

//	@Test - test removed; rarestSequencesOf() not used anymore
//	public void test__rarestSequencesOf__HappyPath() throws Exception {
//		List<Pair<String,Long>> rarest = checker.rarestSequencesOf("inukkshuk");
//		List<Pair<String,Long>> expected = new ArrayList<Pair<String,Long>>();
//		
//		expected.add(new Pair<String,Long>("s",new Long(1)));
//		expected.add(new Pair<String,Long>("h",new Long(1)));
//		expected.add(new Pair<String,Long>("ks",new Long(1)));
//		expected.add(new Pair<String,Long>("sh",new Long(1)));
//		expected.add(new Pair<String,Long>("hu",new Long(1)));
//		expected.add(new Pair<String,Long>("ksh",new Long(1)));
//		expected.add(new Pair<String,Long>("shu",new Long(1)));
//		expected.add(new Pair<String,Long>("huk",new Long(1)));
//		expected.add(new Pair<String,Long>("kshu",new Long(1)));
//		expected.add(new Pair<String,Long>("shuk",new Long(1)));
//		expected.add(new Pair<String,Long>("kshuk",new Long(1)));
//		expected.add(new Pair<String,Long>("i",new Long(5)));
//		expected.add(new Pair<String,Long>("k",new Long(5)));
//		expected.add(new Pair<String,Long>("in",new Long(5)));
//		expected.add(new Pair<String,Long>("uk",new Long(5)));
//		expected.add(new Pair<String,Long>("inu",new Long(5)));
//		expected.add(new Pair<String,Long>("nuk",new Long(5)));
//		expected.add(new Pair<String,Long>("inuk",new Long(5)));
//		expected.add(new Pair<String,Long>("n",new Long(6)));
//		expected.add(new Pair<String,Long>("u",new Long(6)));
//		expected.add(new Pair<String,Long>("nu",new Long(6)));
//		AssertHelpers.assertDeepEquals("The rarest sequence was ", expected, rarest);
//	}
	
	@Test
	public void test__wordsContainingSequ() {
		String seq = "nuk";
		checker.allWordsForCandidates = checker.allWords;
		Set<String> wordsWithSeq = checker.wordsContainingSequ(seq);
		String[] expected = new String[] {"inukshuk","inuk","inuktut"};
			AssertHelpers.assertContainsAll("The list of words containing sequence "+seq+" was not as expected", 
					wordsWithSeq, expected);
	}
	
	@Test
	public void test__wordsContainingSequ__Case_considering_extremities() throws Exception {
		SpellChecker checker = new SpellChecker();
		checker.setVerbose(false);
		checker.addCorrectWord("inuktitut");
		checker.addCorrectWord("inuksuk");
		checker.addCorrectWord("inuttitut");
		checker.addCorrectWord("inakkut");
		checker.addCorrectWord("takuinuit");
		checker.addCorrectWord("taku");
		checker.addCorrectWord("intakuinuit");
		
		String seq;
		String[] expected;
		Set<String> wordsWithSeq;
		
		checker.allWordsForCandidates = checker.allWords;

		seq = "inu";
		wordsWithSeq = checker.wordsContainingSequ(seq);
		expected = new String[] {"inuktitut","inuksuk","inuttitut","takuinuit"};
			AssertHelpers.assertContainsAll("The list of words containing sequence "+seq+" was not as expected", 
					wordsWithSeq, expected);
			
		seq = "^inu";
		wordsWithSeq = checker.wordsContainingSequ(seq);
		expected = new String[] {"inuktitut","inuksuk","inuttitut"};
		AssertHelpers.assertContainsAll("The list of words containing sequence "+seq+" was not as expected", 
					wordsWithSeq, expected);
		
	seq = "itut$";
	wordsWithSeq = checker.wordsContainingSequ(seq);
	expected = new String[] {"inuktitut","inuttitut"};
	AssertHelpers.assertContainsAll("The list of words containing sequence "+seq+" was not as expected", 
				wordsWithSeq, expected);
	
	seq = "^taku$";
	wordsWithSeq = checker.wordsContainingSequ(seq);
	expected = new String[] {"taku"};
	AssertHelpers.assertContainsAll("The list of words containing sequence "+seq+" was not as expected", 
				wordsWithSeq, expected);
	}
	
//	@Test - test removed; firstPassCandidates() not used anymore, replaced by firstPassCandidates_TFIDF
//	public void test__firstPassCandidates() throws Exception {
//		String badWord = "inukkshuk";
//		Set<String> candidates = checker.firstPassCandidates(badWord);
//	
//		
//		// ALAIN: The expected list below contains some misspelled words that come before some 
//		//   correctly spelled ones. But that does not matter as it is only a first pass.
//		//   The second pass should re-sort the candidates, taking into account whether or not
//		//   they were analyzed by the morphological segmenter.
//		//     - 
//		String[] expected = new String[] {"inuk","inukshuk","inukttut","inuktut","inukutt","nunavut"};		
//		AssertHelpers.assertDeepEquals("The list of candidate corrections for word "+badWord+" was not as expected", 
//				expected, candidates);
//	}
	
	@Test
	public void test__firstPassCandidates_TFIDF() throws Exception {
		String badWord = "inukkshuk";
		checker.allWordsForCandidates = checker.allWords;
		checker.ngramStatsForCandidates = checker.ngramStats;
		Set<String> candidates = checker.firstPassCandidates_TFIDF(badWord);
	
		
		// ALAIN: The expected list below contains some misspelled words that come before some 
		//   correctly spelled ones. But that does not matter as it is only a first pass.
		//   The second pass should re-sort the candidates, taking into account whether or not
		//   they were analyzed by the morphological segmenter.
		//     - 
		String[] expected = new String[] {"inuk","inukshuk","inukttut","inuktut","inukutt"};		
		AssertHelpers.assertDeepEquals("The list of candidate corrections for word "+badWord+" was not as expected", 
				expected, candidates);
	}
	
	@Test
	public void test__correctWord__roman__MispelledInput() throws Exception {
		String word = "inukkshuk";
		SpellingCorrection gotCorrection = checker.correctWord(word, 5);
		assertCorrectionOK(gotCorrection, word, false, new String[] {"inukshuk", "inukttut", "inuktut", "inuk", "inukutt"});
	}


	@Test
	public void test__correctWord__roman__CorrectlySpellendInput() throws Exception {
		String word = "inuksuk";
		SpellingCorrection gotCorrection = checker.correctWord(word, 5);
		assertCorrectionOK(gotCorrection, word, true);
	}
	
	@Test
	public void test__correctWord__syllabic__MispelledInput() throws Exception {
		String[] correctWordsLatin = new String[] {"inuktut", "nunavummi", "inuk", "inuksut", "nunavuumi", "nunavut"};
		SpellChecker checker = new SpellChecker();
		checker.setVerbose(false);
		for (String aWord: correctWordsLatin) checker.addCorrectWord(aWord);
		String word = "ᓄᓇᕗᖕᒥ";
		SpellingCorrection gotCorrection = checker.correctWord(word, 5);
		assertCorrectionOK(gotCorrection, word, false, new String[] {"ᓄᓇᕘᒥ", "ᓄᓇᕗᒻᒥ", "ᓄᓇᕗᑦ" });
	}
	
	@Test
	public void test__correctWord__number__ShouldBeDeemedCorrectlySpelled() throws Exception {
		SpellChecker checker = new SpellChecker();
		checker.setVerbose(false);
		String word = "2019";
		SpellingCorrection gotCorrection = checker.correctWord(word, 5);
		assertCorrectionOK(gotCorrection, word, true, new String[] {});
	}

	@Test
	public void test__correctWord__numeric_term_mispelled() throws Exception {
		String[] correctWordsLatin = new String[] {
				"inuktut", "nunavummi", "inuk", "inuksut", "nunavuumi", "nunavut",
				"1988-mut", "$100-mik", "100-nginnik", "100-ngujumik"
				};
		SpellChecker checker = new SpellChecker();
		checker.setVerbose(false);
		for (String aWord: correctWordsLatin) checker.addCorrectWord(aWord);
		String word = "1987-muti";
		SpellingCorrection gotCorrection = checker.correctWord(word, 5);
		assertCorrectionOK(gotCorrection, word, false, new String[] { "1987-mut" });
	}
	

	
	@Test 
	public void test__correctText__roman() throws Exception  {
		String text = "inuktut ninavut inuit inuktut";
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
		Assert.assertTrue("Word #"+wordNum+"="+wordCorr.orig+" should have deemed MISPELLED", wordCorr.wasMispelled);
		AssertHelpers.assertDeepEquals("Corrections for word#"+wordNum+"="+wordCorr.orig+" were not as expected", 
				new String[] {"nunavut"}, 
				wordCorr.getPossibleSpellings());

		wordNum = 2;
		ii = 2*wordNum;
		wordCorr = gotCorrections.get(ii);
		Assert.assertFalse("Word #"+ii+"="+wordCorr.orig+" should have deemd correctly spelled", wordCorr.wasMispelled);

		wordNum = 3;
		ii = 2*wordNum;
		wordCorr = gotCorrections.get(ii);
		Assert.assertFalse("Word #"+ii+"="+wordCorr.orig+" should have deemd correctly spelled", wordCorr.wasMispelled);
	}	
	


	@Test 
	public void test__correctText__syllabic() throws Exception  {
		String text = "ᐃᓄᑦᒧᑦ ᑕᑯᔪᖅ ᐃᒡᓗᑦᒥᒃ ᐊᕐᕌᒍᒥ";
		List<SpellingCorrection> gotCorrections = checker.correctText(text);
		
		
		int wordNum = 0;
		int ii = 2*wordNum;
		SpellingCorrection wordCorr = gotCorrections.get(ii);
		assertCorrectionOK("Correction for word#"+wordNum+"="+wordCorr.orig+" was not as expected",
				wordCorr, "ᐃᓄᑦᒧᑦ", false, 
				// Note: The correct spelling is not in this list of corrections, but that's OK (kinda). 
				//   This test mostly aims at testing the mechanics of SpellText.
				//  				
				new String[] {"ᐃᓄᒃᑦᑐᑦ", "ᐃᓄᒃᑐᑦ", "ᐃᓄᑯᑦᑦ", "ᐃᓄᒃ", "ᐃᓄᒃᔅᓱᒃ"});
		
		wordNum = 1;
		ii = 2*wordNum;
		wordCorr = gotCorrections.get(ii);
		assertCorrectionOK("Correction for word#"+wordNum+"="+wordCorr.orig+" was not as expected",
				wordCorr, "ᑕᑯᔪᖅ", true);

		wordNum = 2;
		ii = 2*wordNum;
		wordCorr = gotCorrections.get(ii);
		assertCorrectionOK("Correction for word#"+wordNum+"="+wordCorr.orig+" was not as expected",
				wordCorr, "ᐃᒡᓗᑦᒥᒃ", false, 
				// Note: The correct spelling is not in this list of corrections, but that's OK (kinda). 
				//   This test mostly aims at testing the mechanics of SpellText.
				//  				
				new String[] {});

		wordNum = 3;
		ii = 2*wordNum;
		wordCorr = gotCorrections.get(ii);
		assertCorrectionOK("Correction for word#"+wordNum+"="+wordCorr.orig+" was not as expected",
				wordCorr, "ᐊᕐᕌᒍᒥ", true);
	}	


	@Test
	public void test__correctText_ampersand() throws SpellCheckerException {
		String text = "inuktut sinik&uni";
		List<SpellingCorrection> gotCorrections = checker.correctText(text);
		Assert.assertEquals("The number of corrections is not as expected.",3,gotCorrections.size());
	}
	
	@Test
	public void test__isMispelled__CorreclySpelledWordFromCompiledCorpus() throws Exception  {
		String word = "inuktitut";
		Assert.assertFalse("Word "+word+" should have been deemed correctly spelled", checker.isMispelled(word));
	}

	@Test
	public void test__isMispelled__CorreclySpelledWordNOTFromCompiledCorpus() throws Exception  {
		String word = "inuktut";
		Assert.assertFalse("Word "+word+" should have been deemed correctly spelled", checker.isMispelled(word));
	}

	@Test
	public void test__isMispelled__CorreclySpelledWordNumber() throws Exception  {
		String word = "2018";
		Assert.assertFalse("Word "+word+" should have been deemed correctly spelled", checker.isMispelled(word));
	}

	@Test
	public void test__isMispelled__MispelledWordFromCompiledCorpus() throws Exception  {
		String word = "inukutt";
		Assert.assertTrue("Word "+word+" should have been deemed mis-spelled", checker.isMispelled(word));
	}

	@Test
	public void test__isMispelled__MispelledWordNOTFromCompiledCorpus() throws Exception  {
		String word = "inuktuttt";
		Assert.assertTrue("Word "+word+" should have been deemed mis-spelled", checker.isMispelled(word));
	}
	
	@Test
	public void test__isMispelled__WordIsSingleInuktitutCharacter() throws Exception  {
		String word = "ti";
		Assert.assertFalse("Word "+word+" should have been deemed correctly spelled", checker.isMispelled(word));
		word = "t";
		Assert.assertFalse("Word "+word+" should have been deemed correctly spelled", checker.isMispelled(word));
		word = "o";
		Assert.assertTrue("Word "+word+" should have been deemed mis-spelled", checker.isMispelled(word));
	}
	
	@Test
	public void test__isMispelled__WordIsNumericTermWithValidEnding() throws Exception  {
		String word = "1988-mut";
		Assert.assertFalse("Word "+word+" should have been deemed correctly spelled", checker.isMispelled(word));
		word = "1988-muti";
		Assert.assertTrue("Word "+word+" should have been deemed correctly spelled", checker.isMispelled(word));
	}
	
	@Test
	public void test__WordContainsMoreThanTwoConsecutiveConsonants() throws Exception  {
		String word = "imglu";
		Assert.assertTrue("Word "+word+" should have been acknowledged as having more than 2 consecutive consonants", checker.wordContainsMoreThanTwoConsecutiveConsonants(word));
		word = "inglu";
		Assert.assertFalse("Word "+word+" should have been acknowledged as not having more than 2 consecutive consonants", checker.wordContainsMoreThanTwoConsecutiveConsonants(word));
		word = "innglu";
		Assert.assertTrue("Word "+word+" should have been acknowledged as having more than 2 consecutive consonants", checker.wordContainsMoreThanTwoConsecutiveConsonants(word));
	}
	
	@Test
	public void test__wordIsNumberWithSuffix() throws Exception  {
		String word = "34-mi";
		String[] numericTermParts = checker.wordIsNumberWithSuffix(word);
		Assert.assertTrue("Word "+word+" should have been acknowledged as a number-based word", numericTermParts != null);
		Assert.assertEquals("The 'number' part is not as expected.", "34-", numericTermParts[0]);
		Assert.assertEquals("The 'ending' part is not as expected.", "mi", numericTermParts[1]);
		word = "$34,000-mi";
		numericTermParts = checker.wordIsNumberWithSuffix(word);
		Assert.assertTrue("Word "+word+" should have been acknowledged as a number-based word", numericTermParts != null);
		Assert.assertEquals("The 'number' part is not as expected.", "$34,000-", numericTermParts[0]);
		Assert.assertEquals("The 'ending' part is not as expected.", "mi", numericTermParts[1]);
		word = "4:30-mi";
		numericTermParts = checker.wordIsNumberWithSuffix(word);
		Assert.assertTrue("Word "+word+" should have been acknowledged as a number-based word", numericTermParts != null);
		Assert.assertEquals("The 'number' part is not as expected.", "4:30-", numericTermParts[0]);
		Assert.assertEquals("The 'ending' part is not as expected.", "mi", numericTermParts[1]);
		word = "5.5-mi";
		numericTermParts = checker.wordIsNumberWithSuffix(word);
		Assert.assertTrue("Word "+word+" should have been acknowledged as a number-based word", numericTermParts != null);
		Assert.assertEquals("The 'number' part is not as expected.", "5.5-", numericTermParts[0]);
		Assert.assertEquals("The 'ending' part is not as expected.", "mi", numericTermParts[1]);
		word = "5,500.33-mi";
		numericTermParts = checker.wordIsNumberWithSuffix(word);
		Assert.assertTrue("Word "+word+" should have been acknowledged as a number-based word", numericTermParts != null);
		Assert.assertEquals("The 'number' part is not as expected.", "5,500.33-", numericTermParts[0]);
		Assert.assertEquals("The 'ending' part is not as expected.", "mi", numericTermParts[1]);
		word = "bla";
		numericTermParts = checker.wordIsNumberWithSuffix(word);
		Assert.assertTrue("Word "+word+" should have been acknowledged as a number-based word", numericTermParts == null);
	}
	
	@Test
	public void test__assessEndingWithIMA() throws Exception  {
		String ending = "mi";
		boolean goodEnding = checker.assessEndingWithIMA(ending);
		Assert.assertTrue("Ending "+ending+" should have been acknowledged as valid word ending", goodEnding);
		ending = "mitiguti";
		goodEnding = checker.assessEndingWithIMA(ending);
		Assert.assertFalse("Ending "+ending+" should have been rejected as valid word ending", goodEnding);
		ending = "gumut";
		goodEnding = checker.assessEndingWithIMA(ending);
		Assert.assertFalse("Ending "+ending+" should have been rejected as valid word ending", goodEnding);
	}
	
	@Test
	public void test__wordIsPunctuation() throws Exception  {
		String word = "-";
		Assert.assertTrue("Word "+word+" should have been acknowledged as punctuation", checker.wordIsPunctuation(word));
		word = "–";
		Assert.assertTrue("Word "+word+" should have been acknowledged as punctuation", checker.wordIsPunctuation(word));
	}
	
	
	
	/**********************************
	 * TEST HELPERS
	 **********************************/
	
	private boolean containsWord(String word, SpellChecker checker) {
		boolean answer = false;
		if (checker.allWords.indexOf(","+word+",") >= 0) {
			answer = true;
		}
		return answer;
	}

	private boolean containsNumericTerm(String numericTerm, SpellChecker checker) {
		boolean answer = false;
		String[] numericTermParts = checker.wordIsNumberWithSuffix(numericTerm);
		String normalizedNumericTerm = "0000"+numericTermParts[1];
		if (checker.allNormalizedNumericTerms.indexOf(","+normalizedNumericTerm+",") >= 0) {
			answer = true;
		}
		return answer;
	}

//	private void assertCorrectionOK(String mess, SpellingCorrection wordCorr, boolean expMispelled) {
//		assertCorrectionOK(mess, wordCorr, expMispelled, null);		
//	}
//
//	
//	private void assertCorrectionOK(SpellingCorrection gotCorrection, String expOrig, boolean expMispelled) throws IOException {
//		assertCorrectionOK(gotCorrection, expOrig, expMispelled, new String[] {});
//	}
//	
//	private void assertCorrectionOK(SpellingCorrection gotCorrection, String expOrig, boolean expMispelled, String[] expSpellings) throws IOException {
//		Assert.assertEquals("The orignal word was not as expected.", 
//				expOrig, gotCorrection.orig);
//		
//		Assert.assertEquals("The misspelled status of the correction was not as expected.", 
//				expMispelled, gotCorrection.wasMispelled);
//		if (!gotCorrection.wasMispelled) {
//			AssertHelpers.assertDeepEquals("Word was correctly spelled, but its list of possible spellings was NOT empty", 
//					new String[] {}, gotCorrection.getPossibleSpellings());
//		}
//		
//		List<String> gotPossibleSpellings = gotCorrection.getPossibleSpellings();
//		AssertHelpers.assertContainsAll("The list of possible correct spellings did not contain all the expected alternatives", 
//				gotPossibleSpellings, expSpellings);
//		if (expSpellings.length > 0) {
//			String expTopSpelling = expSpellings[0];
//			AssertHelpers.assertStringEquals("The top spelling alternative was not as expected", 
//					expTopSpelling, gotPossibleSpellings.get(0));
//		}
//	}

	private void assertCorrectionOK(String mess, SpellingCorrection wordCorr, String expOrig, boolean expOK) throws Exception {
		assertCorrectionOK(mess, wordCorr, expOrig, expOK, null);
		
	}
	
	private void assertCorrectionOK(SpellingCorrection wordCorr, String expOrig, boolean expOK, String[] expSpellings) throws Exception {
		assertCorrectionOK("", wordCorr, expOrig, expOK, expSpellings);
	}
	
	private void assertCorrectionOK(SpellingCorrection wordCorr, String expOrig, boolean expOK) throws Exception {
		assertCorrectionOK("", wordCorr, expOrig, expOK, new String[] {});
	}
	
	private void assertCorrectionOK(String mess, SpellingCorrection wordCorr, String expOrig, boolean expOK, String[] expSpellings) throws Exception {
		if (expSpellings == null) {
			expSpellings = new String[] {};
		}
		Assert.assertEquals(mess+"\nThe input word was not as expected",expOrig, wordCorr.orig);
		Assert.assertEquals(mess+"\nThe correctness status was not as expected", expOK, !wordCorr.wasMispelled);
		AssertHelpers.assertDeepEquals(mess+"\nThe list of spellings was not as expected", expSpellings, wordCorr.getPossibleSpellings());
	}
}
