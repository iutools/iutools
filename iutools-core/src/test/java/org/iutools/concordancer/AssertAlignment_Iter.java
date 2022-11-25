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
import java.util.regex.Pattern;


public class AssertAlignment_Iter extends Asserter<Iterator<Alignment>>{

	private int keepFirstN = 10;

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

	private void init__AssertAlignment_Iter(Integer _keepFirstN) {
		if (_keepFirstN != null) {
			keepFirstN = _keepFirstN;
		}

		while (iterator().hasNext() && firstNAlignments.size() < keepFirstN) {
			Alignment align = iterator().next();
			alignIDsFrequency.updateFreq(align.getId());
			firstNAlignments.add(align);
		}
		return;
	}


	public AssertAlignment_Iter topHitsMatch(
		String sourceLang, String sourceExpr, String targetLang, String[] possibleTranslations) {
		String sourceExprNormalized = normalizeText(sourceExpr, sourceLang);

		PrettyPrinter pprinter = new PrettyPrinter();
		int algnCount = 0;
		for (Alignment anAlignment: firstNAlignments) {
			algnCount++;
			if (algnCount > keepFirstN) {
				// We only check the first few N alignments
				break;
			}
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
			if (failureReason == null && possibleTranslations != null) {
				String targetTextNormalized =
					normalizeText(anAlignment.sentences.get(targetLang), targetLang);
				String regex = termsRegexp(possibleTranslations, targetLang);
				if (!targetTextNormalized.matches(regex)) {
					failureReason =
						"did not contain any of the possible translations in "+targetLang+".\n'"+
						"Target lang : "+targetLang+"\n" +
						"Possible translations regex: "+regex+"\n"+
						"Normalized target text : "+targetTextNormalized
						;
				}
			}
			if (failureReason != null) {
				String mess =
					"At least one alignment in the first "+ keepFirstN +" "+failureReason+"\n"+
					"Alignment:\n"+anAlignment;
				Assert.fail(mess);
			}
		}

		return this;
	}

	private String termsRegexp(String[] possibleTranslations, String targetLang) {
		String regex = null;
		for (String aTranslation: possibleTranslations) {
			if (regex != null) {
				regex += "|";
			} else {
				regex = "";
			}
			aTranslation = normalizeText(aTranslation, targetLang);
			String aTranslationRegex = Pattern.quote(aTranslation);
			aTranslationRegex.replaceAll("\\s+", "\\\\s+");
			regex += aTranslationRegex;
		}
		regex = "^.*("+regex+").*$";
		return regex;
	}

	private String normalizeText(String text, String lang) {
		String normalized = text;
		if (text != null) {
			if (lang.equals("en")) {
				normalized = text.toLowerCase();
			} else {
				normalized = TransCoder.ensureRoman(text, true);
				normalized = normalized.toLowerCase();
			}
		}
		return normalized;
	}

	public AssertAlignment_Iter atLeastNHits(int expMin) {
		if (expMin > keepFirstN) {
			// At creation time, we only retrieved the first few elements but
			// this is less than the expMin we need to veriy.
			// So retrieve more.
			while (iterator().hasNext() & firstNAlignments.size() < expMin) {
				firstNAlignments.add(iterator().next());
			}
		}
		AssertNumber.isGreaterOrEqualTo(
			"Total number of alignments was too low.",
			firstNAlignments.size(), expMin);
		return this;
	}

	public void atMostNHits(Integer maxHits) {
		int totalAlignments = firstNAlignments.size();
		if (iterator().hasNext() && firstNAlignments.size() < maxHits) {
			// So far, we nay have only retrieved a small sample of the hits a
			// and this sample is smaller than the maxHits we are trying to verify.
			// So, retrieve hits until we reach the end.
			while (iterator().hasNext()) {
				totalAlignments++;
				iterator().next();
			}
		}
		AssertNumber.isLessOrEqualTo(
			"The iterator contained too many alignments",
			maxHits, totalAlignments);
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

	protected Iterator<Alignment> iterator() {
		return (Iterator<Alignment>) gotObject;
	}
}
