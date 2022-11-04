package org.iutools.concordancer;

import ca.nrc.dtrc.stats.FrequencyHistogram;
import ca.nrc.json.PrettyPrinter;
import ca.nrc.testing.AssertNumber;
import ca.nrc.testing.AssertString;
import ca.nrc.testing.Asserter;
import org.iutools.concordancer.tm.WordSpotter;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;

import java.util.*;

public class AssertAlignment_List extends Asserter<List<Alignment>> {

	FrequencyHistogram<String> alignIDsFrequency = new FrequencyHistogram<String>();

	public AssertAlignment_List(List<Alignment> _gotObject) {
		super(_gotObject);
		init__AssertAlignment_List();
	}

	public AssertAlignment_List(List<Alignment> _gotObject, String mess) {
		super(_gotObject, mess);
		init__AssertAlignment_List();
	}

	private void init__AssertAlignment_List() {
		for (Alignment align: alignments()) {
			alignIDsFrequency.updateFreq(align.getId());
		}
		return;
	}

	protected List<Alignment> alignments() {
		return this.gotObject;
	}

	public AssertAlignment_List hitsMatchQuery(
		String sourceLang, String sourceExpr) {
		return hitsMatchQuery(sourceLang, sourceExpr, (Integer)null);
	}

	public AssertAlignment_List hitsMatchQuery(
		String sourceLang, String sourceExpr, Integer firstN) {
		if (firstN ==null) {
			firstN = 100;
		}
		sourceExpr = sourceExpr.toLowerCase();
		List<Alignment> firstNAlignments =
			alignments().subList(0, Math.min(firstN, alignments().size()));

		PrettyPrinter pprinter = new PrettyPrinter();
		int algnCount = 0;
		for (Alignment anAlignment: alignments()) {
			algnCount++;
			String failureReason = null;
			if (!anAlignment.sentences.containsKey(sourceLang)) {
				failureReason = "did not have a sentence for the source language";
			}
			if (failureReason == null &&
				!anAlignment.sentences.get(sourceLang).toLowerCase().contains(sourceExpr)) {
				failureReason = "did not contain the source expression";
			}
			if (failureReason != null) {
				String mess =
					"At least one alignment in the first "+firstN+" "+failureReason+"\n"+
					"Alignment:\n"+new PrettyPrinter().pprint(anAlignment);
				Assert.fail(mess);
			}
		}

		return this;
	}

	public AssertAlignment_List atLeastNHits(int expMin) {
		AssertNumber.isGreaterOrEqualTo(alignments().size(), expMin);
		return this;
	}

	public AssertAlignment_List atMostNHits(int expMax) {
		AssertNumber.isLessOrEqualTo(alignments().size(), expMax);
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

	public AssertAlignment_List containsNoDuplicates() {
		String errMess = "";
		for (String algnId: alignIDsFrequency.allValues()) {
			if (alignIDsFrequency.frequency(algnId) > 1) {
				errMess += "  "+algnId+" (freq: "+ alignIDsFrequency.frequency(algnId)+")";
			}
		}
		if (!errMess.isEmpty()) {
			errMess =
				"The folowing alignments appeared more than once!\n"+
				errMess;
			Assertions.fail(errMess);
		}
		return this;
	}
}
