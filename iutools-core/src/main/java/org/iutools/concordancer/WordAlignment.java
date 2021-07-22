package org.iutools.concordancer;

import ca.nrc.datastructure.Cloner;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a word-level alignment between two sentences.
 *
 * For more details on how to use this class see DOCUMENTATION TESTS section in
 * WordAlignmentTest.
 */
public class WordAlignment {
	public String langPair = null;
	public Map<String,String[]> tokens4lang = new HashMap<String,String[]>();
	public String[] tokensPairing = null;

	private Pair<Integer,Integer>[] _matchedTokenPairs = null;

	public WordAlignment(String l1, String[] l1Tokens,
		String l2, String[] l2Tokens, String[] l1_l2_wordpairs) {
		this.langPair = l1+"-"+l2;
		tokens4lang.put(l1, l1Tokens);
		tokens4lang.put(l2, l2Tokens);
		setTokensPairing(l1_l2_wordpairs);
	}

	private void setTokensPairing(String[] l1_l2_wordpairs) {
		this.tokensPairing = l1_l2_wordpairs;
		this._matchedTokenPairs = null;
	}


	public WordAlignment reverseDirection() throws WordAlignmentException {
		WordAlignment reversed = null;
		try {
			reversed = Cloner.clone(this);
			Pair<Integer,Integer>[] origPairs = matchedTokenPairs();
			String[] reversedPairings = new String[origPairs.length];
			int ii = 0;
			for (Pair<Integer,Integer> anOrigPair: origPairs) {
				reversedPairings[ii] = anOrigPair.getRight()+"-"+anOrigPair.getLeft();
				ii++;
			}
		} catch (Cloner.ClonerException e) {
			throw new WordAlignmentException(e);
		}
		return reversed;
	}

	private Pair<Integer,Integer>[] matchedTokenPairs() throws WordAlignmentException {
		if (_matchedTokenPairs == null) {
			_matchedTokenPairs = new Pair[tokensPairing.length];
			int ii = 0;
			for (String aMatching: tokensPairing) {
				String[] tokPair = aMatching.split("-");
				if (tokPair.length != 2) {
					throw new WordAlignmentException("Invalid token pairing "+aMatching);
				}
				try {
					Integer l1tok = Integer.parseInt(tokPair[0]);
					Integer l2tok = Integer.parseInt(tokPair[1]);
					_matchedTokenPairs[ii] = Pair.of(l1tok, l2tok);
				} catch (Exception e) {
					throw new WordAlignmentException("Invalid token pairing "+aMatching, e);
				}
				ii++;
			}
		}
		return _matchedTokenPairs;
	}
}