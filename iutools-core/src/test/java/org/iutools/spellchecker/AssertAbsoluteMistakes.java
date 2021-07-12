package org.iutools.spellchecker;

import ca.nrc.testing.AssertString;
import ca.nrc.testing.Asserter;

public class AssertAbsoluteMistakes extends Asserter<AbsoluteMistakes> {
	public AssertAbsoluteMistakes(AbsoluteMistakes _gotObject) {
		super(_gotObject);
	}

	public AssertAbsoluteMistakes(AbsoluteMistakes _gotObject, String mess) {
		super(_gotObject, mess);
	}

	public AssertAbsoluteMistakes nothingToFix(String origWord) {
		String fixedWord = mistakes().fixWord(origWord);
		AssertString.assertStringEquals(
			baseMessage+"\n\"Fixed\" word should have been identical to the original one.",
			fixedWord, origWord);
		return this;
	}

	public AssertAbsoluteMistakes fixesWord(String origWord, String expFixedWord) {
		String gotFixedWord = mistakes().fixWord(origWord);
		AssertString.assertStringEquals(
			baseMessage+"\nWord was not fixed as expected.",
			expFixedWord, gotFixedWord);
		return this;
	}


	protected AbsoluteMistakes mistakes() {
		return (AbsoluteMistakes)gotObject;
	}
}
