package org.iutools.concordancer;

import ca.nrc.json.PrettyPrinter;
import ca.nrc.testing.AssertNumber;
import ca.nrc.testing.AssertString;
import ca.nrc.testing.Asserter;
import org.iutools.concordancer.tm.WordSpotter;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AssertAlignment_List extends Asserter<List<Alignment>> {
	public AssertAlignment_List(List<Alignment> _gotObject) {
		super(_gotObject);
	}

	public AssertAlignment_List(List<Alignment> _gotObject, String mess) {
		super(_gotObject, mess);
	}

	protected List<Alignment> alignments() {
		return this.gotObject;
	}

	public AssertAlignment_List allHitsMatchQuery(
		String sourceLang, String sourceExpr) {
		sourceExpr = sourceExpr.toLowerCase();
		String mess =
			"Expected:\n"+
			"  source lang : "+sourceLang+"\n"+
			"  source expr : "+sourceExpr+"\n"+
			"Got alignments:\n"+
			PrettyPrinter.print(alignments())+"\n"+
			"\n";

		for (Alignment anAlignment: alignments()) {
			String failureReason = null;
			if (!anAlignment.sentences.containsKey(sourceLang)) {
				failureReason = "did not have a sentence for the source language";
			}
			if (failureReason == null &&
				!anAlignment.sentences.get(sourceLang).toLowerCase().contains(sourceExpr)) {
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

	public AssertAlignment_List atLeastNHits(int expMin) {
		AssertNumber.isGreaterOrEqualTo(alignments().size(), expMin);
		return this;
	}

	public void includesTranslation(String l1, String l1Word, String l2,
	  	String expL2Word)
		throws Exception {
		List<String> gotTranslations = new ArrayList<String>();
		boolean found = false;
		for (Alignment alignment: alignments()) {
			SentencePair pair = alignment.sentencePair(l1, l2);
			WordSpotter spotter = new WordSpotter(pair);
			Map<String, String> spottings = spotter.spot(l1, l1Word);
			String gotL2Word = spottings.get(l2);
			AssertString.assertStringEquals(
				baseMessage+"\nTranslation of "+l1+" word '"+l1Word+"' was not as expected",
				expL2Word, gotL2Word
			);
		}
		Assertions.fail("FINISH IMPLEMENTING THIS ASSERTION");
	}
}
