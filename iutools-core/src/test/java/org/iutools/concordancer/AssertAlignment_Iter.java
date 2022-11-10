package org.iutools.concordancer;

import ca.nrc.dtrc.stats.FrequencyHistogram;
import ca.nrc.json.PrettyPrinter;
import ca.nrc.testing.AssertNumber;
import ca.nrc.testing.AssertString;
import ca.nrc.testing.Asserter;
import org.iutools.concordancer.tm.WordSpotter;
import org.iutools.script.TransCoder;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;

import java.util.*;


public class AssertAlignment_Iter extends Asserter<Iterator<Alignment>>{

	private Integer maxHits = null;

	FrequencyHistogram<String> alignIDsFrequency = new FrequencyHistogram<String>();
	List<Alignment> firstNAlignments = new ArrayList<Alignment>();

	public AssertAlignment_Iter(Iterator<Alignment> _gotObject) {
		super(_gotObject);
		init__AssertAlignment_Iter((Integer)null);
	}

	public AssertAlignment_Iter(Iterator<Alignment> _gotObject, String mess) {
		super(_gotObject, mess);
		init__AssertAlignment_Iter((Integer)null);
	}

	private void init__AssertAlignment_Iter(Integer _maxHits) {
		if (_maxHits == null) {
			maxHits = 100;
		}
		Iterator<Alignment> iter = (Iterator<Alignment>) gotObject;
		while (iter.hasNext() && firstNAlignments.size() < maxHits) {
			Alignment align = iter.next();
			alignIDsFrequency.updateFreq(align.getId());
			firstNAlignments.add(align);
		}
		return;
	}


	public AssertAlignment_Iter hitsMatchQuery(
		String sourceLang, String sourceExpr) {
		String sourceExprNormalized = normalizeText(sourceExpr, sourceLang);

		PrettyPrinter pprinter = new PrettyPrinter();
		int algnCount = 0;
		for (Alignment anAlignment: firstNAlignments) {
			algnCount++;
			String failureReason = null;
			if (!anAlignment.sentences.containsKey(sourceLang)) {
				failureReason = "did not have a sentence for the source language";
			}
			String sourceTextNormalized =
				normalizeText(anAlignment.sentences.get(sourceLang), sourceLang);

			if (failureReason == null &&
				!sourceTextNormalized.contains(sourceExprNormalized)) {
				failureReason =
					"did not contain the source ("+sourceLang+") expression '"+sourceExpr+"'\n"+
					"Source lang : "+sourceLang+"\n" +
					"Normalized Source expr : "+sourceExprNormalized+"\n" +
					"Normalized Source text : "+sourceTextNormalized
					;
			}
			if (failureReason != null) {
				String mess =
					"At least one alignment in the first "+maxHits+" "+failureReason+"\n"+
					"Alignment:\n"+new PrettyPrinter().pprint(anAlignment);
				Assert.fail(mess);
			}
		}

		return this;
	}

	private String normalizeText(String text, String lang) {
		String normalized = null;
		if (lang.equals("en")) {
			normalized = text.toLowerCase();
		} else {
			normalized = TransCoder.ensureRoman(text, true);
			normalized = normalized.toLowerCase();
		}
		return normalized;
	}

	public AssertAlignment_Iter atLeastNHits(int expMin) {
		AssertNumber.isGreaterOrEqualTo(
			"Total number of alignments was too low.",
			firstNAlignments.size(), expMin);
		return this;
	}


	public AssertAlignment_Iter atMostNHits(int expMax) {
		AssertNumber.isLessOrEqualTo(
			"Total number of alignments was too high.",
			firstNAlignments.size(), expMax);
		return this;
	}

	public void includesTranslation(String l1, String l1Word, String l2,
	  	String expL2Word)
		throws Exception {
		List<String> gotTranslations = new ArrayList<String>();
		boolean found = false;
		for (Alignment alignment: firstNAlignments) {
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

	public AssertAlignment_Iter containsNoDuplicates() {
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
