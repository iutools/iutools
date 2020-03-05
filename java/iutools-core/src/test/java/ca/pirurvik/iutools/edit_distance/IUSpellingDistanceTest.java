package ca.pirurvik.iutools.edit_distance;


import org.junit.Before;
import org.junit.Test;

import ca.nrc.string.diff.DiffCosting;
import ca.nrc.testing.AssertNumber;

import org.junit.Assert;

public class IUSpellingDistanceTest {
	
	IUSpellingDistance distCalculator = null;
	
	@Before
	public void setUp() {
		distCalculator = new IUSpellingDistance();
	}

	////////////////////////
	// VERIFICATION TESTS
	////////////////////////
	
	@Test
	public void test__distance__HappyPath() throws Exception {
		String word1 = "tamaini";
		String word2 = "tamainni";
		double gotDist = distCalculator.distance(word1, word2);
		double expDist = 2.0;
		Assert.assertEquals("Spelling distance not as expected", 
				expDist, gotDist, 0.01);
	}

	@Test
	public void test__distance__WordsDifferInFirst3Chars__InfiniteCost() throws Exception {
		String word1 = "qallunaatitut";
		String word2 = "alputatitut";
		double gotDist = distCalculator.distance(word1, word2);
		AssertNumber.isGreaterOrEqualTo("Changing leading 3 chars of a word should have yielded an 'infinite' cost.", 
				gotDist, DiffCosting.INFINITE);
	}

	@Test
	public void test__distance__anniaq__AllowCertainTransfInFirst3Chars() throws Exception {
		String word1 = "nniaqamangittulirijiit";
		String word2 = "anniaqarnangittulirijiit";
		double gotDist = distCalculator.distance(word1, word2);
		AssertNumber.isLessOrEqualTo("Removing leading character 'a' 'anniaq*' should NOT have yielded an 'infinite' cost.",
				gotDist, DiffCosting.INFINITE-1);
	}
	
	
}
