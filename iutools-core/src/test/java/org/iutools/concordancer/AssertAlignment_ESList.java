package org.iutools.concordancer;

import ca.nrc.json.PrettyPrinter;
import ca.nrc.testing.AssertNumber;
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
		String mess =
			"Expected:\n"+
			"  source lang : "+sourceLang+"\n"+
			"  source expr : "+sourceExpr+"\n"+
			"Got alignments:\n"+
			PrettyPrinter.print(alignments())+"\n"+
			"\n";

		for (Alignment_ES anAlignment: alignments()) {
			String failureReason = null;
			if (!anAlignment.sentences.containsKey(sourceLang)) {
				failureReason = "did not have a sentence for the source language";
			}
			if (failureReason == null &&
				!anAlignment.sentences.get(sourceLang).contains(sourceExpr)) {
				failureReason = "did not contain the source expression";
			}
			if (failureReason != null) {
				mess =
					"At least one alignment "+failureReason+"\n"+
					mess;
				Assert.fail(mess);
			}
		}

		return this;
	}

	public AssertAlignment_ESList atLeastNHits(int expMin) {
		AssertNumber.isGreaterOrEqualTo(alignments().size(), expMin);
		return this;
	}
}
