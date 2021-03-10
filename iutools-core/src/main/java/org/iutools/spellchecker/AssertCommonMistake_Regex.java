package org.iutools.spellchecker;

import ca.nrc.testing.AssertString;
import ca.nrc.testing.Asserter;

public class AssertCommonMistake_Regex extends Asserter<CommonMistake_Regex > {
	public AssertCommonMistake_Regex(CommonMistake_Regex pattern) {
		super(pattern);
	}

	public AssertCommonMistake_Regex(CommonMistake_Regex _gotObject, String mess) {
		super(_gotObject, mess);
	}

	public AssertCommonMistake_Regex nothingToFix(String origWord) {
		String fixedWord = pattern().fixWord(origWord);
		AssertString.assertStringEquals(
			baseMessage+"\n\"Fixed\" word should have been identical to the original one.",
			fixedWord, origWord);
		return this;
	}

	public AssertCommonMistake_Regex fixesWord(String origWord, String expFixedWord) {
		String gotFixedWord = pattern().fixWord(origWord);
		AssertString.assertStringEquals(
			baseMessage+"\nWord was not fixed as expected.",
			expFixedWord, gotFixedWord);
		return this;
	}

	public CommonMistake_Regex pattern() {
		return this.gotObject;
	}
}
