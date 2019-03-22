package ca.inuktitutcomputing.core;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import ca.nrc.datastructure.Pair;

import org.junit.*;

public class SpellCheckerTest {
	
	private SpellChecker checker = new SpellChecker();
	
	@Before
	public void setUp() {
		checker.addCorrectWord("inuktut");
		checker.addCorrectWord("inuk");
		checker.addCorrectWord("inukshuk");
		checker.addCorrectWord("nunavut");
	}

	@Test(expected=RuntimeException.class) // because file checker.json does not exist
	public void test__SpellChecker__Synopsis() throws IOException {
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
		// You can then use the check to get plausible corrections for
		// a badly spelled word.
		//
		String wordWithError = "inusuk";
		int nCorrections = 5;
		List<String> corrections = checker.correct(wordWithError, nCorrections);

	}
	
	/**********************************
	 * VERIFICATION TESTS
	 **********************************/
	
	@Test
	public void test__addCorrectWord__HappyPath() {
		SpellChecker checker = new SpellChecker();
		
		Assert.assertFalse(containsWord("inuktut", checker));
		checker.addCorrectWord("inuktut");
		Assert.assertTrue(containsWord("inuktut", checker));
		
		Assert.assertFalse(containsWord("nunavut", checker));
		checker.addCorrectWord("nunavut");
		Assert.assertTrue(containsWord("nunavut", checker));
	}
	
	@Test
	public void test__idf__HappyPath() {
		Assert.assertEquals("IDF was wrong for sequence that starts words ('inu')", new Long(3), checker.idf("inu"));
		Assert.assertEquals("IDF was wrong for sequence at the middle of words ('ks')", new Long(1), checker.idf("ks"));
		Assert.assertEquals("IDF was wrong for sequence at the end of words ('ktut')", new Long(1), checker.idf("ktut"));
		Assert.assertEquals("IDF was wrong for sequence with > 5 chars", new Long(0), checker.idf("nunavu"));
		Assert.assertEquals("IDF was wrong for sequence with =5 chars", new Long(1), checker.idf("unavu"));
		Assert.assertEquals("IDF was wrong for non-existant sequence",new Long(0), checker.idf("blah"));
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
	
	/*
	 *  checker.addCorrectWord("inuktut");
		checker.addCorrectWord("inuk");
		checker.addCorrectWord("inukshuk");
		checker.addCorrectWord("nunavut");

	 */
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
	public void test__correct() {
		List<String> corrections = checker.correct("inukkshuk");
		String[] expected = new String[] {"inukshuk","inuktut","inuk","nunavut"};
		Assert.assertEquals("The number of candidates is wrong.", 4, corrections.size());
		for (int i=0; i<expected.length; i++) {
			Assert.assertEquals("The element "+i+" of the list of corrections is not right.",expected[i],corrections.get(i));
		}
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
