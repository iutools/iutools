package org.iutools.morph.l2rAlain;

import java.util.List;

import ca.nrc.string.StringUtils;
import org.iutools.linguisticdata.LinguisticData;
import org.iutools.linguisticdata.Morpheme;
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
		String remainingChars  = "inuktitut";
		List<WrittenMorpheme> gotMorphemes = 
			MorphemeWrittenForms.getInstance()
			.morphemesThatCanFollow(prevMorpheme, remainingChars);
		
		WrittenMorphemeCollectionAsserter.assertThat(gotMorphemes, "")
			.containsMorpheme("inuk/1n", "inuk")
			.containsMorpheme("inuk/1n", "inu")
			;
	}

	@Test
	public void test__morphemesThatCanFollow__MorphThatFollowsRoot() 
			throws Exception {
		WrittenMorpheme prevMorpheme = new WrittenMorpheme("inuk/1n", "inuk");
		String remainingChars  = "titut";
		List<String> forms = MorphemeWrittenForms.getInstance().surfaceForms();
		String formsConcat = StringUtils.join(forms.iterator(), "\n");
		List<WrittenMorpheme> gotMorphemes =
			MorphemeWrittenForms.getInstance()
			.morphemesThatCanFollow(prevMorpheme, remainingChars);
		
		WrittenMorphemeCollectionAsserter.assertThat(gotMorphemes, "")
			.containsMorpheme("titut/tn-sim-p", "titut")
			;
	}
}
