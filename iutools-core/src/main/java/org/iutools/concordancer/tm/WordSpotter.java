package org.iutools.concordancer.tm;

import org.apache.commons.lang3.tuple.Pair;
import org.iutools.concordancer.SentencePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Given a SentencePair that has word-level alignments, this class can spot
 * a word or expression in one language as well as its equivalent in the other
 * language.
 *
 * For more details on how to use this class, see the DOCUMENTATION TESTS section
 * of WordSpotterTest.
 *
 */
public class WordSpotter {

	public WordSpotter() {

	}

	public Map<String,String> spot(String sourceLang, String sourceWord,
		SentencePair pair) {
		Map<String,String> spottings = new HashMap<String,String>();
		spottings.put(sourceLang, "BLAH");
		spottings.put(pair.otherLangThan(sourceLang), "BLOB");

		return spottings;
	}

	public Map<String, String> higlight(String sourceLang, String word,
		SentencePair pair) {
		Map<String,String> highglighted = new HashMap<String,String>();
		highglighted.put(sourceLang, pair.getText(sourceLang));
		String otherLang = pair.otherLangThan(sourceLang);
		highglighted.put(otherLang, pair.getText(otherLang));

		return highglighted;
	}
}
