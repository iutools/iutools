package org.iutools.spellchecker;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class CommonMistake_RegexTest {

	/////////////////////////////////////////////
	// DOCUMENTATION TESTS
	/////////////////////////////////////////////

	@Test
	public void test__CommonMistake_Regex__Synopsis() {
		// Use this class to describe common spelling mistake which can
		// be captured using simple Regex substitution patterns
		//
		// For example it is quite common for , when 'q' is followed by certain consonants
		// it should be replaced by 'r'.
		//
		CommonMistake_Regex pattern =
			new CommonMistake_Regex("q([jkmv])", "r$1");

		// You can use a mistake pattern to fix a mis-spelled word
		//
		String origWord = "inuqka";
		String fixedWord = pattern.fixWord(origWord);
		if (fixedWord.equals(origWord)) {
			// Means the orignal word had that spelling mistake
		} else {
			// Means the original word did not contain that specific spelling mistake
		}
	}

	/////////////////////////////////////////////
	// DOCUMENTATION TESTS
	/////////////////////////////////////////////

	@Test @Disabled
	public void test__fixWord() {
		CommonMistake_Regex pattern =
			new CommonMistake_Regex("q([jkmv])", "r$1");

		AssertCommonMistake_Regex asserter =
			new AssertCommonMistake_Regex(pattern);
		asserter.nothingToFix("inuqtitut");
		asserter.fixesWord("inuqjuq", "inurjuq");
	}
}
