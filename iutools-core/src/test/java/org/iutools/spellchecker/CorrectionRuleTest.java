package org.iutools.spellchecker;

import org.iutools.text.IUWord;
import org.junit.jupiter.api.Test;

public class CorrectionRuleTest {

	/////////////////////////////////////////////
	// DOCUMENTATION TESTS
	/////////////////////////////////////////////

	@Test
	public void test__CorrectionRule__Synopsis() throws Exception {
		// Use this class to describe an 'absolute' spelling mistake, i.e. a
		// pattern that is ALWAYS a mistake no matter the specifics of the word.
		//
		// For example, in Inuktitut it is impossible to see the character 'q'
		// followed by certain consonants one of jkmv. Yet you see this mistake
		// quite often. In those situations, the way to fix the problem is to
		// replace the 'q' with an 'r'.
		//
		CorrectionRule pattern =
			new CorrectionRule("q([jkmv])", "r$1");

		// You can use a mistake pattern to fix a mis-spelled word
		//
		String origWord = "inuqka";
		IUWord fixedWord = pattern.fixWord(new IUWord(origWord));
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
	public void test__fixWord() throws Exception {
		CorrectionRule pattern =
			new CorrectionRule("q([jmnv])", "r$1");

		AssertCorrectionRule asserter =
			new AssertCorrectionRule(pattern);
		asserter.nothingToFix("inuqtitut");
		asserter.fixesWord("inuqjuq", "inurjuq");
	}
}
