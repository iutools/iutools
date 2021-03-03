package org.iutools.concordancer;

import ca.nrc.testing.Asserter;
import org.junit.Assert;

import java.util.List;

public class AssertAlignment_ESList extends Asserter<List<Alignment_ES>> {
	public AssertAlignment_ESList(List<Alignment_ES> _gotObject) {
		super(_gotObject);
	}

	public AssertAlignment_ESList(List<Alignment_ES> _gotObject, String mess) {
		super(_gotObject, mess);
	}

	protected List<Alignment_ES> alignments() {
		return this.gotObject;
	}

	public AssertAlignment_ESList allHitsMatchQuery(
		String sourceLang, String sourceExpr) {
//		Assert.fail("Implement this assertion");
		return this;
	}
}
