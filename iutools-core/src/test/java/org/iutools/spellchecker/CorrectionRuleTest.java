package org.iutools.spellchecker;

import ca.nrc.testing.AssertString;
import ca.nrc.testing.RunOnCases;
import static ca.nrc.testing.RunOnCases.Case;
import org.iutools.text.IUWord;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

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
	// VERIFICATION TESTS
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

	@Test
	public void test__constructor__VariousCases() throws Exception {
		Case[] cases = new Case[] {
			new Case("\\b in left-hand regexp --> raise exceptino",
		"\\b", null,
				"'\\b' --> '[$0]': \\b is not allowed in left-hand regexp. Use ^ or $ depending on whether you are looking at the start or end of a word."),
			new Case("\\W in left-hand regexp --> raise exceptino",
		"\\W", null,
				"'\\W' --> '[$0]': \\W is not allowed in left-hand regexp. Use ^ or $ depending on whether you are looking at the start or end of a word."),
			new Case("\\w in left-hand regexp --> raise exceptino",
		"\\w", null,
				"'\\w' --> '[$0]': \\w is not allowed in left-hand regexp. Use . instead."),
		};

		Consumer<Case> runner = (caze) -> {
			String leftRegex = (String)caze.data[0];
			String rightRegex = (String)caze.data[1];
			String expException = (String)caze.data[2];
			String expLeftRegex = null;
			if (expException == null) {
				expLeftRegex = (String)caze.data[3];
			}

			CorrectionRule rule = null;
			Exception gotException = null;
			try {
				rule = new CorrectionRule(leftRegex, rightRegex);
			} catch (Exception e) {
				gotException = e;
			}
			if (expException == null && gotException != null) {
				throw new RuntimeException(gotException);
			}
			if (expException != null) {
				if (gotException == null) {
					throw new RuntimeException("Should have raised exception with message: " + expException);
				} else {
					AssertString.assertStringEquals(
						"Raised exception did not have the correct message",
							expException, gotException.getMessage()
					);
				}
			} else {
				// We wren't expecting an exception. Make sure that the left-hand regex
				// was propertly massaged
				AssertString.assertStringEquals(
						"left-hand regex was not as expected",
						expLeftRegex, rule.regexBad);
			}
		};

		new RunOnCases(cases, runner)
//			.onlyCaseNums(2)
			.run();

	}
}
