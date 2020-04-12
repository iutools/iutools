package ca.inuktitutcomputing.morph.expAlain;

import org.junit.Test;

import ca.inuktitutcomputing.morph.expAlain.WrittenMorpheme;
import ca.nrc.testing.AssertString;

public class WrittenMorphemeTest {

	/////////////////////////////
	// SYNOPSIS TESTS
	/////////////////////////////	
	
	
	@Test
	public void test__WrittenMorpheme__Synopsis() throws Exception {
		// This class is used to represent one possible way in which a morpheme 
		// can express itself in written form
		//
		// For example, these are two ways in which morpheme inuk/1n can 
		// express itself
		//
		WrittenMorpheme form1 = new WrittenMorpheme("inuk/1n", "inuk");
		WrittenMorpheme form2 = new WrittenMorpheme("inuk/1n", "inu");

		// You can retrieve different info about the written form
		//
		@SuppressWarnings("unused")
		String morphemeID = form1.morphID;
		@SuppressWarnings("unused")
		String canonicalWrittenForm = form1.canonicalForm();
		@SuppressWarnings("unused")
		String actualWrittenForm = form1.writtenForm;
	}

	/////////////////////////////
	// SYNOPSIS TESTS
	/////////////////////////////	
	
	@Test
	public void test__WrittenMorpheme__RootMorpheme() throws Exception {
		WrittenMorpheme gotForm = new WrittenMorpheme("inuk/1n", "inu");
		AssertString.assertStringEquals("Canonical form not as expected", 
			"inuk", gotForm.canonicalForm());
		AssertString.assertStringEquals("Writen form not as expected", 
				"inu", gotForm.writtenForm);
		AssertString.assertStringEquals("Morpheme type not as expected", 
				"N", gotForm.type());
		AssertString.assertStringEquals(
				"Type that morpheme attaches to was not as expected", 
				"S", gotForm.atachesTo());
	}
	
	@Test
	public void test__WrittenMorpheme__SuffixMorpheme() throws Exception {
		WrittenMorpheme gotForm = new WrittenMorpheme("titut/nn", "titu");
		AssertString.assertStringEquals("Canonical form not as expected", 
			"titut", gotForm.canonicalForm());
		AssertString.assertStringEquals("Writen form not as expected", 
				"titu", gotForm.writtenForm);
		AssertString.assertStringEquals("Morpheme type not as expected", 
				"N", gotForm.type());
		AssertString.assertStringEquals(
				"Type that morpheme attaches to was not as expected", 
				"N", gotForm.atachesTo());
	}


}
