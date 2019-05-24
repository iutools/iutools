package ca.pirurvik.iutools;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import ca.nrc.datastructure.Pair;
import ca.nrc.json.PrettyPrinter;
import ca.nrc.testing.AssertHelpers;
import ca.pirurvik.iutools.SpellChecker;

import org.apache.commons.io.IOUtils;
import org.junit.*;

public class SpellCheckerTest {
	
	private SpellChecker checker;
	private SpellChecker checkerSyll;;
	
	@Before
	public void setUp() throws Exception {
		
		File tempCorpFileLatin = CompiledCorpusTest.compileToFile("inuktut inukshuk nunavut");		
		File tempCorpFileSyll = CompiledCorpusTest.compileToFile("ᓄᓇᕘᒥ ᓄᓇᕗᒻᒥ ᓄᓇᒥ ᓄᓇᕗᑦ ᓄᓇᕗᒻᒥᑦ ᐃᒡᓗ");		
		
		checker = new SpellChecker(tempCorpFileLatin);
		checkerSyll = new SpellChecker(tempCorpFileSyll);
		
		checker.addCorrectWord("inuktut");
		checker.addCorrectWord("inuk");
		checker.addCorrectWord("inukshuk");
		checker.addCorrectWord("nunavut");
		
		checkerSyll.addCorrectWord("ᓄᓇᕘᒥ");
		checkerSyll.addCorrectWord("ᓄᓇᕗᒻᒥ");
		checkerSyll.addCorrectWord("ᓄᓇᒥ");
		checkerSyll.addCorrectWord("ᓄᓇᕗᑦ");
		checkerSyll.addCorrectWord("ᓄᓇᕗᒻᒥᑦ");
		checkerSyll.addCorrectWord("ᐃᒡᓗ");
	}

	@Test
	public void test__SpellChecker__Synopsis() throws Exception {
		//
		// Before you can use a spell checker, you must first build its
		// dictioanary of correct words.
		// 
		// For example
		//
		SpellChecker checker = new SpellChecker();
		checker.addCorrectWord("inuktut");
		checker.addCorrectWord("inuk");
		checker.addCorrectWord("inuksuk");
		checker.addCorrectWord("nunavut");
		// etc...
		
		//
		// Once you have built its dictionary, you can save the 
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
		
		Assert.assertFalse(containsWord("inuktut", checker));
		checker.addCorrectWord("inuktut");
		Assert.assertTrue(containsWord("inuktut", checker));
		
		Assert.assertFalse(containsWord("nunavut", checker));
		checker.addCorrectWord("nunavut");
		Assert.assertTrue(containsWord("nunavut", checker));
		
		Assert.assertFalse(containsWord("ᐃᒡᓗ", checker));
		checker.addCorrectWord("ᐃᒡᓗ");
		Assert.assertTrue(containsWord("ᐃᒡᓗ", checker));
		
	}
	
	@Test
	public void test__idf__HappyPath() {
		Assert.assertEquals("IDF was wrong for sequence that starts words ('inu')", new Long(3), checker.idf("inu"));
		Assert.assertEquals("IDF was wrong for sequence at the middle of words ('ks')", new Long(1), checker.idf("ks"));
		Assert.assertEquals("IDF was wrong for sequence at the end of words ('ktut')", new Long(1), checker.idf("ktut"));
		Assert.assertEquals("IDF was wrong for sequence ('uk')", new Long(3), checker.idf("uk"));
		Assert.assertEquals("IDF was wrong for sequence with > 5 chars", new Long(0), checker.idf("nunavu"));
		Assert.assertEquals("IDF was wrong for sequence with =5 chars", new Long(1), checker.idf("unavu"));
		Assert.assertEquals("IDF was wrong for non-existant sequence",new Long(0), checker.idf("blah"));
	}

	@Test
	public void test__idf__HappyPath_syllabic() {
		System.out.println(PrettyPrinter.print(checkerSyll.idfStats));
		Assert.assertEquals("IDF was wrong for sequence that starts words ('ᓄᓇ')", new Long(5), checkerSyll.idf("ᓄᓇ"));
		Assert.assertEquals("IDF was wrong for sequence at the middle of words ('ᓇᕗ')", new Long(3), checkerSyll.idf("ᓇᕗ"));
		Assert.assertEquals("IDF was wrong for sequence ('ᒻᒥ')", new Long(2), checkerSyll.idf("ᒻᒥ"));
	}

	@Test
	public void test__rarestSequencesOf__HappyPath() {
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
		expected.add(new Pair<String,Long>("i",new Long(3)));
		expected.add(new Pair<String,Long>("k",new Long(3)));
		expected.add(new Pair<String,Long>("in",new Long(3)));
		expected.add(new Pair<String,Long>("uk",new Long(3)));
		expected.add(new Pair<String,Long>("inu",new Long(3)));
		expected.add(new Pair<String,Long>("nuk",new Long(3)));
		expected.add(new Pair<String,Long>("inuk",new Long(3)));
		expected.add(new Pair<String,Long>("n",new Long(4)));
		expected.add(new Pair<String,Long>("u",new Long(4)));
		expected.add(new Pair<String,Long>("nu",new Long(4)));
		Assert.assertEquals("The number of rarest sequences is wrong.", 21, rarest.size());
		for (int i=0; i<expected.size(); i++) {
			Assert.assertEquals(i+". The string of the sequence is wrong.",
					((Pair<String,Long>)expected.get(i)).getFirst(),
					((Pair<String,Long>)rarest.get(i)).getFirst());
			Assert.assertEquals(i+". The string of the sequence is wrong.",
					((Pair<String,Long>)expected.get(i)).getSecond(),
					((Pair<String,Long>)rarest.get(i)).getSecond());
		}
	}
	
	@Test
	public void test__wordsContainingSequ() {
		String seq = "nuk";
		Set<String> wordsWithSeq = checker.wordsContainingSequ(seq);
		System.out.println(checker.allWords);
		Assert.assertEquals("The number of words containing the sequence is wrong.", 3, wordsWithSeq.size());
		String[] expected = new String[] {"inukshuk","inuk","inuktut"};
		for (int i=0; i<expected.length; i++)
			Assert.assertTrue("The element "+expected[i]+" is not in the returned list.",wordsWithSeq.contains(expected[i]));
	}
	
	@Test
	public void test__firstPassCandidates() {
		String badWord = "inukkshuk";
		Set<String> candidates = checker.firstPassCandidates(badWord);
		Assert.assertEquals("The number of candidates is wrong.", 4, candidates.size());
		String[] expected = new String[] {"inukshuk","inuk","inuktut","nunavut"};
		for (int i=0; i<expected.length; i++)
			Assert.assertTrue("The element "+expected[i]+" is not in the returned list.",candidates.contains(expected[i]));
	}
	
	@Test
	public void test__correct__roman__Mispelled_input() {
		List<String> corrections = checker.correctWord("inukkshuk").getPossibleSpellings();
		String[] expected = new String[] {"inukshuk","inuktut","inuk","nunavut"};
		Assert.assertEquals("The number of candidates is wrong.", 4, corrections.size());
		for (int i=0; i<expected.length; i++) {
			Assert.assertEquals("The element "+i+" of the list of corrections is not right.",expected[i],corrections.get(i));
		}
	}

	@Test
	public void test__correct__roman__CorrectlySpellendInput() throws Exception {
		SpellingCorrection gotCorrection = checker.correctWord("inukshuk");
		
		SpellingCorrection expCorrection = 
				new SpellingCorrection("inukshuk", new String[] {"inuktut","inuk","nunavut"}, false);
		AssertHelpers.assertDeepEquals("Correction was not as expected", expCorrection, gotCorrection);
	}
	
	
	@Test
	public void test__correct__syllabic__Mispelled_input() {
		List<String> corrections = checkerSyll.correctWord("ᓄᓇᕗᖕᒥ").getPossibleSpellings();
		String[] expected = new String[] {"ᓄᓇᕗᒻᒥ","ᓄᓇᕘᒥ","ᓄᓇᒥ","ᓄᓇᕗᑦ","ᓄᓇᕗᒻᒥᑦ"};
		Assert.assertEquals("The number of candidates is wrong.", expected.length, corrections.size());
		for (int i=0; i<expected.length; i++) {
			Assert.assertEquals("The element "+i+" of the list of corrections is not right.",expected[i],corrections.get(i));
		}
	}
	
	@Test 
	public void test__correctText_roman() {
		String text = "inuktut nunnavut inuit inuktut";
		List<SpellingCorrection> gotCorrections = checker.correctText(text);
		
		Assert.assertFalse("'inukshuk' should have deemd correctly spelled", gotCorrections.get(0).wasMispelled);
		
		Assert.assertTrue("'nunnavut' should have deemd mis-spelled", gotCorrections.get(1).wasMispelled);
		
		Assert.assertTrue("'inuit' should have deemd mis-spelled", gotCorrections.get(2).wasMispelled);

		Assert.assertTrue("'inuktut' should have deemd correctly spelled", gotCorrections.get(3).wasMispelled);
		
		fail("Now, need to check that the list of corrections is correct for the mis-spelled words");
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

}
