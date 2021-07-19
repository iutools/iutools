package org.iutools.concordancer;

import org.junit.Assert;

import org.iutools.testing.Asserter;

public class AlignmentAsserter extends Asserter {

	public static AlignmentAsserter assertThat(SentencePair _gotAlignment,
															 String mess) throws Exception {
		return new AlignmentAsserter(_gotAlignment, mess);
	}
	
	public AlignmentAsserter(Object _gotObject, String mess) throws Exception {
		super(_gotObject, mess, SentencePair.class);
	}
	
	private SentencePair gotAlignment() {
		return (SentencePair)gotObject;
	}

	public AlignmentAsserter langSentencesEqual(String string, String[] strings) {
		Assert.fail("Not implemented");
		return this;
	}
}
