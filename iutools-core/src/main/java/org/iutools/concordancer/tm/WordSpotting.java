package org.iutools.concordancer.tm;

import org.apache.commons.lang3.tuple.Pair;
import org.iutools.concordancer.SentencePair;

import java.util.ArrayList;
import java.util.List;

public class WordSpotting {

	SentencePair sentPair = null;

	public WordSpotting() {
		init_WordSpotting((SentencePair)null, (String)null, (String)null);
	}
	public WordSpotting(SentencePair _sentPair) {
		init_WordSpotting(_sentPair, (String)null, (String)null);
	}

	private void init_WordSpotting(SentencePair _sentPair,
											 String _sourceLang, String _sourceWord) {
		this.sentPair = _sentPair;
	}

	public SentencePair sentencePair() {
		return sentPair;
	}

	public List<Pair<Integer,Integer>> offsets(String lang) {
		List<Pair<Integer,Integer>> results = new ArrayList<Pair<Integer,Integer>>();
		return results;
	}

	public List<String> occurences(String lang) {
		List<String> occurs = new ArrayList<String>();
		return occurs;
	}
}
