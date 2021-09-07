package org.iutools.corpus;

import ca.nrc.testing.AssertString;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

public class WordInfoTest {

	///////////////////////////////////////
	// DOCUMENTATION TESTS
	///////////////////////////////////////

	@Test
	public void test__WordInfo__Synopsis() throws Exception {
		// WordInfo represents information about an Inuktitut word and its
		// occurences in a specific corpus
		//
		WordInfo winfo =
			new WordInfo("inuktitut")
			// Frequency of the word in the corpus
			.setFrequency(237)
			.setDecompositions(
				// Sample of all possible decompositions
				new String[][] {
					new String[] {"inuk/1n", "titut/tn-sim-p"},
					new String[] {"inuk/1n", "ktut/tn-sim-p-2s"},
					new String[] {"inuk/1n", "iq/1nn", "tut/tn-sim-s"}
				},
				// Total number of possible decompositions (may be greater than the
				// number of sample decomps.
				13
			)
		;

		// You can then use the object to get specific info about the word
		long freq = winfo.frequency;
		String[][] sampleDecomps = winfo.decompositionsSample;
		int totalDecomps = winfo.totalDecompositions;
		String wordInOriginalScript = winfo.word;
		String wordInOtherScript = winfo.getWordInOtherScript();
		String wordInRomanScript = winfo.getWordRoman();
		String wordInSyllabic = winfo.getWordSyllabic();
	}

	//////////////////////////////////
	// VERIFICATION TESTS
	//////////////////////////////////

	@Test
	public void test__WordInfo__GetWordInVariousScripts() throws Exception {
		WordInfo winfo = new WordInfo("inuk");

		AssertString.assertStringEquals(
			"Word in ORIG script not as expected",
			"inuk", winfo.word);

		AssertString.assertStringEquals(
			"Word in OTHER script not as expected",
			"ᐃᓄᒃ", winfo.getWordInOtherScript());

		AssertString.assertStringEquals(
			"Word in ROMAN script not as expected",
			"inuk", winfo.getWordRoman());

		AssertString.assertStringEquals(
			"Word in SYLLABIC script not as expected",
			"ᐃᓄᒃ", winfo.getWordSyllabic());
	}
}
