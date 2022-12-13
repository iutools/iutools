package org.iutools.spellchecker;

import ca.nrc.testing.AssertString;
import ca.nrc.testing.Asserter;
import org.iutools.text.IUWord;

public class AssertCorrectionRule extends Asserter<CorrectionRule> {
	public AssertCorrectionRule(CorrectionRule pattern) {
		super(pattern);
	}

	public AssertCorrectionRule(CorrectionRule _gotObject, String mess) {
		super(_gotObject, mess);
	}

	public AssertCorrectionRule nothingToFix(String origWord) throws Exception {
		IUWord fixedWord = rule().fixWord(new IUWord(origWord));
		AssertString.assertStringEquals(
			baseMessage+"\n\"Fixed\" word should have been identical to the original one.",
			fixedWord.word(), origWord);
		return this;
	}

	public AssertCorrectionRule fixesWord(String origWord, String expFixedWord)
		throws Exception {
		IUWord gotFixedWord = rule().fixWord(new IUWord(origWord));
		AssertString.assertStringEquals(
			baseMessage+"\nWord was not fixed as expected.",
			expFixedWord, gotFixedWord.word());
		return this;
	}

	public CorrectionRule rule() {
		return this.gotObject;
	}
}
