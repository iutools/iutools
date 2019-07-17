package ca.pirurvik.iutools;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import ca.nrc.datastructure.Pair;
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

//	@Test
//	public void test__DELETE_ME_LATER() throws Exception {
//		checker = new SpellChecker();
//		SpellingCorrection correction = checker.correctWord("nunavuttt");
//		AssertHelpers.assertDeepEquals("", null, correction);
//	}
	
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
	}
	
	@Test
	public void test__idf__HappyPath() {
		Assert.assertEquals("IDF was wrong for sequence that starts words ('inu')", new Long(5), checker.idf("inu"));
		Assert.assertEquals("IDF was wrong for sequence at the middle of words ('ks')", new Long(1), checker.idf("ks"));
		Assert.assertEquals("IDF was wrong for sequence at the end of words ('ktut')", new Long(1), checker.idf("ktut"));
		Assert.assertEquals("IDF was wrong for sequence ('uk')", new Long(5), checker.idf("uk"));
		Assert.assertEquals("IDF was wrong for sequence with > 5 chars", new Long(0), checker.idf("nunavu"));
		Assert.assertEquals("IDF was wrong for sequence with =5 chars", new Long(1), checker.idf("unavu"));
		Assert.assertEquals("IDF was wrong for non-existant sequence",new Long(0), checker.idf("blah"));
	}

	@Test
	public void test__rarestSequencesOf__HappyPath() throws Exception {
		List<Pair<String,Long>> rarest = checker.rarestSequencesOf("inukkshuk");
		List<Pair<String,Long>> expected = new ArrayList<Pair<String,Long>>();
		
		expected.add(new Pair<String,Long>("s",new Long(1)));
		expected.add(new Pair<String,Long>("h",new Long(1)));
		expected.add(new Pair<String,Long>("ks",new Long(1)));
		expected.add(new Pair<String,Long>("sh",new Long(1)));
		expected.add(new Pair<String,Long>("hu",new Long(1)));
		expected.add(new Pair<String,Long>("ksh",new Long(1)));
		expected.add(new Pair<String,Long>("shu",new Long(1)));
		expected.add(new Pair<String,Long>("huk",new Long(1)));
		expected.add(new Pair<String,Long>("kshu",new Long(1)));
		expected.add(new Pair<String,Long>("shuk",new Long(1)));
		expected.add(new Pair<String,Long>("kshuk",new Long(1)));
		expected.add(new Pair<String,Long>("i",new Long(5)));
		expected.add(new Pair<String,Long>("k",new Long(5)));
		expected.add(new Pair<String,Long>("in",new Long(5)));
		expected.add(new Pair<String,Long>("uk",new Long(5)));
		expected.add(new Pair<String,Long>("inu",new Long(5)));
		expected.add(new Pair<String,Long>("nuk",new Long(5)));
		expected.add(new Pair<String,Long>("inuk",new Long(5)));
		expected.add(new Pair<String,Long>("n",new Long(6)));
		expected.add(new Pair<String,Long>("u",new Long(6)));
		expected.add(new Pair<String,Long>("nu",new Long(6)));
		AssertHelpers.assertDeepEquals("The rarest sequence was ", expected, rarest);
	}
	
	@Test
	public void test__wordsContainingSequ() {
		String seq = "nuk";
		Set<String> wordsWithSeq = checker.wordsContainingSequ(seq);
		String[] expected = new String[] {"inukshuk","inuk","inuktut"};
			AssertHelpers.assertContainsAll("The list of words containing sequence "+seq+" was not as expected", 
					wordsWithSeq, expected);
	}
	
	@Test
	public void test__firstPassCandidates() throws Exception {
		String badWord = "inukkshuk";
		Set<String> candidates = checker.firstPassCandidates(badWord);
	
		
		// ALAIN: The expected list below contains some misspelled words that come before some 
		//   correctly spelled ones. But that does not matter as it is only a first pass.
		//   The second pass should re-sort the candidates, taking into account whether or not
		//   they were analyzed by the morphological segmenter.
		//     - 
		String[] expected = new String[] {"inuk","inukshuk","inukttut","inuktut","inukutt","nunavut"};		
		AssertHelpers.assertDeepEquals("The list of candidate corrections for word "+badWord+" was not as expected", 
				expected, candidates);
	}
	
	@Test
	public void test__correctWord__roman__MispelledInput() throws Exception {
		String word = "inukkshuk";
		SpellingCorrection gotCorrection = checker.correctWord(word, 5);
		assertCorrectionIsOK(gotCorrection, true, new String[] {"inukshuk", "inukttut", "inuktut", "inuk", "inukutt"});
	}

	@Test
	public void test__correctWord__roman__CorrectlySpellendInput() throws Exception {
		String word = "inuksuk";
		SpellingCorrection gotCorrection = checker.correctWord(word, 5);
		assertCorrectionIsOK(gotCorrection, false);
	}
	
	
	@Test
	public void test__correctWord__syllabic__MispelledInput() throws Exception {
		String[] correctWordsLatin = new String[] {"inuktut", "nunavummi", "inuk", "inuksut", "nunavuumi", "nunavut"};
		SpellChecker checker = new SpellChecker();
		checker.setVerbose(false);
		for (String aWord: correctWordsLatin) checker.addCorrectWord(aWord);
		String word = "ᓄᓇᕗᖕᒥ";
		SpellingCorrection gotCorrection = checker.correctWord(word, 5);
		assertCorrectionIsOK(gotCorrection, true, new String[] {"ᓄᓇᕘᒥ", "ᓄᓇᕗᒻᒥ", "ᓄᓇᕗᑦ" });
	}
	
	@Test 
	public void test__correctText_roman() throws Exception  {
		String text = "inuktut ninavut inuit inuktut";
		List<SpellingCorrection> gotCorrections = checker.correctText(text);
		
		int ii = 0;
		SpellingCorrection wordCorr = gotCorrections.get(ii);
		Assert.assertFalse("Word #"+ii+"="+wordCorr.orig+" should have deemed correctly spelled", wordCorr.wasMispelled);
		
		ii = 2;
		wordCorr = gotCorrections.get(ii);
		Assert.assertTrue("Word #"+ii+"="+wordCorr.orig+" should have deemed MISPELLED", wordCorr.wasMispelled);
		
		ii = 4;
		wordCorr = gotCorrections.get(ii);
		Assert.assertFalse("Word #"+ii+"="+wordCorr.orig+" should have deemd correctly spelled", wordCorr.wasMispelled);

		ii = 6;
		wordCorr = gotCorrections.get(ii);
		Assert.assertFalse("Word #"+ii+"="+wordCorr.orig+" should have deemd correctly spelled", wordCorr.wasMispelled);
		
		
	}	
	
	@Test
	public void test__isMispelled__CorreclySpelledWordFromCompiledCorpus() throws Exception  {
		String word = "inuktitut";
		Assert.assertFalse("Word "+word+" should have been deemed correctly spelled", checker.isMispelled(word));
	}

	public void test__isMispelled__CorreclySpelledWordNOTFromCompiledCorpus() throws Exception  {
		String word = "inuktut";
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

	private void assertCorrectionIsOK(SpellingCorrection gotCorrection, boolean expMispelled) throws IOException {
		assertCorrectionIsOK(gotCorrection, expMispelled, new String[] {});
	}
	
	private void assertCorrectionIsOK(SpellingCorrection gotCorrection, boolean expMispelled, String[] expSpellings) throws IOException {
		Assert.assertEquals("The misspelled status of the correction was not as expected.", 
				expMispelled, gotCorrection.wasMispelled);
		if (!gotCorrection.wasMispelled) {
			AssertHelpers.assertDeepEquals("Word was correctly spelled, but its list of possible spellings was NOT empty", 
					new String[] {}, gotCorrection.getPossibleSpellings());
		}
		
		List<String> gotPossibleSpellings = gotCorrection.getPossibleSpellings();
		AssertHelpers.assertContainsAll("The list of possible correct spellings did not contain all the expected alternatives", 
				gotPossibleSpellings, expSpellings);
		if (expSpellings.length > 0) {
			String expTopSpelling = expSpellings[0];
			AssertHelpers.assertStringEquals("The top spelling alternative was not as expected", 
					expTopSpelling, gotPossibleSpellings.get(0));
		}
		
	}
	

}
