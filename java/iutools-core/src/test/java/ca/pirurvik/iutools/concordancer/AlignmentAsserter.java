package ca.pirurvik.iutools.concordancer;

import org.junit.Assert;

public class AlignmentAsserter extends Asserter {

	public static AlignmentAsserter assertThat(Alignment _gotAlignment, 
			String mess) throws Exception {
		return new AlignmentAsserter(_gotAlignment, mess);
	}
	
	public AlignmentAsserter(Object _gotObject, String mess) throws Exception {
		super(_gotObject, mess, Alignment.class);
	}
	
	private Alignment gotAlignment() {
		return (Alignment)gotObject;
	}

	public AlignmentAsserter langSentencesEqual(String string, String[] strings) {
		Assert.fail("Not implemented");
		return this;
	}
}
