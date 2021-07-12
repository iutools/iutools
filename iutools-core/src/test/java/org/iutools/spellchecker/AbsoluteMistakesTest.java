package org.iutools.spellchecker;

import org.junit.Test;

public class AbsoluteMistakesTest {

	///////////////////////////////////////
	// DOCUMENTATION TESTS
	///////////////////////////////////////

	@Test
	public void test__CommonMistakes__Synopsis() {
		AbsoluteMistakes mistakes = new AbsoluteMistakes();

		// Use this class to fix the most common patterns of spelling mistakes
		for (String origWord:
			new String[]{
				// This word is fine
				"inuqtitut",

				// This word has a common spelling mistake: 'qj' should be 'qr'
				// This is an ABSOLUTE mistake meaning that no Inuktitut word can
				// contain 'qj'.
				"inuqjuq",

				// This word contains a spelling mistake... 'uumik' should be 'ummik'.
				// However this is NOT a 'universal' mistake because there are
				// correct inuktitut words that contain the string 'uumik'
				"nunavuumik"
			}) {

			String fixedWord = mistakes.fixWord(origWord);
			if (fixedWord.equals(origWord)) {
				// Means there were no absolute mistakes in the original word
			} else {
				// Means there were one or more absolute mistakes and the fixes were
				// applie.d
				// Note that this does not mean that the word is now correctly
				// spelled. There may be other mistakes that are not of the 'absolute'
				// type.
			}
		}
	}

	///////////////////////////////////////
	// VERIFICATION TESTS
	///////////////////////////////////////

	@Test
	public void test__fixWord() {
		AbsoluteMistakes mistakes =
			new AbsoluteMistakes();

		AssertAbsoluteMistakes asserter =
			new AssertAbsoluteMistakes(mistakes);

		// This word is correctly spelled
		asserter.nothingToFix("inuqtitut");

		// This word has an absolute splling mistake: qj -> rj
		asserter.fixesWord("inuqjuq", "inurjuq");

		// This word has an absolute splling mistake: qk -> ll
		asserter.fixesWord("titiqkaq", "titiqqaq");

		// This word has a spelling mistake but it is not absolute
		asserter.nothingToFix("\"nunavuumik\"");
		;
	}


}
