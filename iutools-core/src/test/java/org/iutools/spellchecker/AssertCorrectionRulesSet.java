package org.iutools.spellchecker;

import ca.nrc.testing.AssertString;
import ca.nrc.testing.Asserter;

public class AssertCorrectionRulesSet extends Asserter<CorrectionRulesSet> {
	public AssertCorrectionRulesSet(CorrectionRulesSet _gotObject) {
		super(_gotObject);
	}

	public AssertCorrectionRulesSet(CorrectionRulesSet _gotObject, String mess) {
		super(_gotObject, mess);
	}

	public AssertCorrectionRulesSet nothingToFix(String origWord)  throws Exception {
		String fixedWord = mistakes().fixWord(origWord);
		AssertString.assertStringEquals(
			baseMessage+"\n\"Fixed\" word should have been identical to the original one.",
			fixedWord, origWord);
		return this;
	}

	public AssertCorrectionRulesSet fixesWord(String origWord, String expFixedWord)
		throws Exception {
		String gotFixedWord = mistakes().fixWord(origWord);
		AssertString.assertStringEquals(
			baseMessage+"\nWord was not fixed as expected.",
			expFixedWord, gotFixedWord);
		return this;
	}


	protected CorrectionRulesSet mistakes() {
		return (CorrectionRulesSet)gotObject;
	}
}
