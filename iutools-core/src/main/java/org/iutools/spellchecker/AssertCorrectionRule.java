package org.iutools.spellchecker;

import ca.nrc.testing.AssertString;
import ca.nrc.testing.Asserter;

public class AssertCorrectionRule extends Asserter<CorrectionRule> {
	public AssertCorrectionRule(CorrectionRule pattern) {
		super(pattern);
	}

	public AssertCorrectionRule(CorrectionRule _gotObject, String mess) {
		super(_gotObject, mess);
	}

	public AssertCorrectionRule nothingToFix(String origWord) throws SpellCheckerException {
		String fixedWord = pattern().fixWord(origWord);
		AssertString.assertStringEquals(
			baseMessage+"\n\"Fixed\" word should have been identical to the original one.",
			fixedWord, origWord);
		return this;
	}

	public AssertCorrectionRule fixesWord(String origWord, String expFixedWord) throws SpellCheckerException {
		String gotFixedWord = pattern().fixWord(origWord);
		AssertString.assertStringEquals(
			baseMessage+"\nWord was not fixed as expected.",
			expFixedWord, gotFixedWord);
		return this;
	}

	public CorrectionRule pattern() {
		return this.gotObject;
	}
}
