package org.iutools.concordancer;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class WordSpotting {

	Alignment sentPair = null;

	public WordSpotting() {
		init_WordSpotting((Alignment)null, (String)null, (String)null);
	}
	public WordSpotting(Alignment _sentPair) {
		init_WordSpotting(_sentPair, (String)null, (String)null);
	}

	private void init_WordSpotting(Alignment _sentPair,
 		String _sourceLang, String _sourceWord) {
		this.sentPair = _sentPair;
	}

	public Alignment sentencePair() {
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
