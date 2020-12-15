package org.iutools.morph.expAlain;

import java.util.List;

import org.junit.Test;

public class MorphemeWrittenFormsTest {

	///////////////////////////////
	// DOCUMENTATION TESTS
	///////////////////////////////
	
	@Test
	public void test__MorphemeWrittenForms__Synopsis() throws Exception {
		// Use this class to find all the morphemes that:
		//
		// - Can follow a given morpheme
		// AND
		// - Have a written form that matches the start of a string
		//
		WrittenMorpheme prevMorpheme = new WrittenMorpheme("inuk/1n", "inuk");
		String remainingChars  = "titut";
		List<WrittenMorpheme> morphemes = 
			MorphemeWrittenForms.getInstance()
			.morphemesThatCanFollow(prevMorpheme, remainingChars);
	}
	
	
	///////////////////////////////
	// VERIFICATION TESTS
	///////////////////////////////
	
	@Test
	public void test__morphemesThatCanFollow__PrevMorphemeIsHead__ReturnsOnlyRoots() 
			throws Exception {
		WrittenMorpheme prevMorpheme = WrittenMorpheme.head;
		String remainingChars  = "Sinuktitut";
		List<WrittenMorpheme> gotMorphemes = 
			MorphemeWrittenForms.getInstance()
			.morphemesThatCanFollow(prevMorpheme, remainingChars);
		
		WrittenMorphemeCollectionAsserter.assertThat(gotMorphemes, "")
			.containsMorpheme("inuk/1n", "inuk")
			;
	}

	@Test
	public void test__morphemesThatCanFollow__MorphThatFollowsRoot() 
			throws Exception {
		WrittenMorpheme prevMorpheme = new WrittenMorpheme("inuk/1n", "inuk");
		String remainingChars  = "titut";
		List<WrittenMorpheme> gotMorphemes = 
			MorphemeWrittenForms.getInstance()
			.morphemesThatCanFollow(prevMorpheme, remainingChars);
		
		WrittenMorphemeCollectionAsserter.assertThat(gotMorphemes, "")
			.containsMorpheme("titut/nn", "titut")
			;
	}

}
