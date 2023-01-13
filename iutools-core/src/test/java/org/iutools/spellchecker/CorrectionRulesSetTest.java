package org.iutools.spellchecker;

import ca.nrc.testing.AssertObject;
import ca.nrc.testing.RunOnCases;
import static ca.nrc.testing.RunOnCases.Case;
import org.junit.Test;

import java.util.*;
import java.util.function.Consumer;

public class CorrectionRulesSetTest {

	///////////////////////////////////////
	// DOCUMENTATION TESTS
	///////////////////////////////////////

	@Test
	public void test__CorrectionRulesSet__Synopsis() throws Exception {
		CorrectionRulesSet rules = new CorrectionRulesSet();

		// Use this class to fix the most common patterns of spelling mistakes
		for (String origWord :
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

			String fixedWord = rules.fixWord(origWord);
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
	public void test__fixWord__RunOnSpellCheckerExamples() throws Exception {
		// Test fixWord() using all the examples used to evaluate the accuracy
		// of the SpellChecker. For each example, make sure that fixWord() does
		// not change any of he correct forms specified by that example
		Set<String> validWords = new HashSet<String>();
		for (SpellCheckerExample example : SpellCheckerAccuracyTest.examples_MostFrequenMisspelledWords) {
			for (String validWord : example.acceptableCorrections) {
				validWords.add(validWord);
			}
		}
		for (SpellCheckerExample example : SpellCheckerAccuracyTest.examples_RandomPageSample) {
			for (String validWord : example.acceptableCorrections) {
				validWords.add(validWord);
			}
		}

		CorrectionRulesSet rules = new CorrectionRulesSet();
		Map<String, String> affectedWords = new HashMap<String, String>();
		for (String word : validWords) {
			String fixedWord = rules.fixWord(word);
			if (!fixedWord.equals(word)) {
				affectedWords.put(word, fixedWord);
			}
		}

		AssertObject.assertDeepEquals(
				"Some valid words were modified by the rule set",
				new HashMap<String, String>(), affectedWords
		);
	}

	@Test
	public void test__somethingToFixOrNot__VariousCases() throws Exception {
		Case[] cases = new Case[]{
				// This word SHOULD be correctly spelled, as it is one of the
				//   correct suggestions for bad word nunavuumit in our SpellChecker
				//   gold standard
				new Case("nunavummit - NOTHING to fix", "nunavummit", null),

				// This word is correctly spelled
				new Case("inuqtitut - NOTHING to fix", "inuqtitut", null),

				// This word has "shallow" mistake: qj -> rj
				new Case("inuqjuq - SOMETHING to fix", "inuqjuq", "inurjuq"),

				// This word has "shallow" mistake: qk -> ll
				new Case("titiqkaq - SOMETHING to fix", "titiqkaq", "titiqqaq"),

				// This word has a spelling mistake but it cannot be fixed with a
				// "shallow" rule
				new Case("nunavuumik - SOMETHING to fix", "nunavuumik", "nunavummik"),

				// This word has TWO "shallow" mistake.
				// - First = single character ('ᕿ') is written as two characters that look
				//   the same as the single char ('ᕐ'+'ᑭ').
				// - Second, is a mistake that can be spotted but not fixed ('^ᐃᓕ')
				//
				new Case("ᐃᓕᓐᓂᐊᕐᑭ - SOMETHING to fix", "ᐃᓕᓐᓂᐊᕐᑭ", "ᐃᓕᓐᓂᐊᕿ"),
		};

		Consumer<Case> runner = (caze) -> {
			String origWord = (String)caze.data[0];
			String expCorrection = (String)caze.data[1];
			CorrectionRulesSet mistakes =
				new CorrectionRulesSet();

			AssertCorrectionRulesSet asserter =
				new AssertCorrectionRulesSet(mistakes);
			try {
				if (expCorrection != null) {
					asserter.fixesWord(origWord, expCorrection);
				} else {
					asserter.nothingToFix(origWord);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};

		new RunOnCases(cases, runner)
			.run();

	}
}
