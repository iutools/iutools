package org.iutools.concordancer;

import ca.nrc.testing.Asserter;
import org.junit.jupiter.api.Assertions;

public class AssertAlignmentSpec extends Asserter<AlignmentSpec> {
	public AssertAlignmentSpec(AlignmentSpec _gotObject) {
		super(_gotObject);
	}

	public AssertAlignmentSpec(AlignmentSpec _gotObject, String mess) {
		super(_gotObject, mess);
	}

	private AlignmentSpec spec() {
		return this.gotObject;
	}

	public AssertAlignmentSpec assertL1SentsEqual(int start, int end) {
		Assertions.assertEquals(spec().l1SentStart, start,
			"Start index of L1 sentences was wrong");
		Assertions.assertEquals(spec().l1SentEnd, end,
			"End index of L1 sentences was wrong");
		return this;
	}

	public AssertAlignmentSpec assertL2SentsEqual(int start, int end) {
		Assertions.assertEquals(spec().l2SentStart, start,
			"Start index of L2 sentences was wrong");
		Assertions.assertEquals(spec().l2SentEnd, end,
			"End index of L2 sentences was wrong");
		return this;
	}
}
