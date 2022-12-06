package org.iutools.spellchecker;

import org.junit.Test;

public class CorrectionRulesSetTest {

	///////////////////////////////////////
	// DOCUMENTATION TESTS
	///////////////////////////////////////

	@Test
	public void test__CommonMistakes__Synopsis() throws Exception {
		CorrectionRulesSet mistakes = new CorrectionRulesSet();

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
	public void test__fixWord() throws Exception {
		CorrectionRulesSet mistakes =
			new CorrectionRulesSet();

		AssertCorrectionRulesSet asserter =
			new AssertCorrectionRulesSet(mistakes);

		// This word is correctly spelled
		asserter.nothingToFix("inuqtitut");

		// This word has "shallow" mistake: qj -> rj
		asserter.fixesWord("inuqjuq", "inurjuq");

		// This word has "shallow" mistake: qk -> ll
		asserter.fixesWord("titiqkaq", "titiqqaq");

		// This word has a spelling mistake but it cannot be fixed with a
		// "shallow" rule
		asserter.fixesWord("nunavuumik", "nunavummik");
		;

		// This is a case where single character ('ᕿ') is written as two
		// characters that look the same as the single char ('ᕐ'+'ᑭ').
		asserter.fixesWord("ᐃᓕᓐᓂᐊᕐᑭ", "ᐃᓕᓐᓂᐊᕿ");
	}


}
