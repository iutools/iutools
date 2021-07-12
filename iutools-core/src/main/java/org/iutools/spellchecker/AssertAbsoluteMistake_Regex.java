package org.iutools.spellchecker;

import ca.nrc.testing.AssertString;
import ca.nrc.testing.Asserter;

public class AssertAbsoluteMistake_Regex extends Asserter<AbsoluteMistake_Regex> {
	public AssertAbsoluteMistake_Regex(AbsoluteMistake_Regex pattern) {
		super(pattern);
	}

	public AssertAbsoluteMistake_Regex(AbsoluteMistake_Regex _gotObject, String mess) {
		super(_gotObject, mess);
	}

	public AssertAbsoluteMistake_Regex nothingToFix(String origWord) {
		String fixedWord = pattern().fixWord(origWord);
		AssertString.assertStringEquals(
			baseMessage+"\n\"Fixed\" word should have been identical to the original one.",
			fixedWord, origWord);
		return this;
	}

	public AssertAbsoluteMistake_Regex fixesWord(String origWord, String expFixedWord) {
		String gotFixedWord = pattern().fixWord(origWord);
		AssertString.assertStringEquals(
			baseMessage+"\nWord was not fixed as expected.",
			expFixedWord, gotFixedWord);
		return this;
	}

	public AbsoluteMistake_Regex pattern() {
		return this.gotObject;
	}
}
