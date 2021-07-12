package org.iutools.spellchecker;

import org.junit.jupiter.api.Test;

public class AbsoluteMistake_RegexTest {

	/////////////////////////////////////////////
	// DOCUMENTATION TESTS
	/////////////////////////////////////////////

	@Test
	public void test__AbsoluteMistake_Regex__Synopsis() {
		// Use this class to describe an 'absolute' spelling mistake, i.e. a
		// pattern that is ALWAYS a mistake no matter the specifics of the word.
		//
		// For example, in Inuktitut it is impossible to see the character 'q'
		// followed by certain consonants one of jkmv. Yet you see this mistake
		// quite often. In those situations, the way to fix the problem is to
		// replace the 'q' with an 'r'.
		//
		AbsoluteMistake_Regex pattern =
			new AbsoluteMistake_Regex("q([jkmv])", "r$1");

		// You can use a mistake pattern to fix a mis-spelled word
		//
		String origWord = "inuqka";
		String fixedWord = pattern.fixWord(origWord);
		if (!fixedWord.equals(origWord)) {
			// Means the orignal word had that spelling mistake
		} else {
			// Means the original word did not contain that specific spelling mistake
		}
	}

	/////////////////////////////////////////////
	// DOCUMENTATION TESTS
	/////////////////////////////////////////////

	@Test
	public void test__fixWord() {
		AbsoluteMistake_Regex pattern =
			new AbsoluteMistake_Regex("q([jmnv])", "r$1");

		AssertAbsoluteMistake_Regex asserter =
			new AssertAbsoluteMistake_Regex(pattern);
		asserter.nothingToFix("inuqtitut");
		asserter.fixesWord("inuqjuq", "inurjuq");
	}
}
