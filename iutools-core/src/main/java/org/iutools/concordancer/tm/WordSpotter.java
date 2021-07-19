package org.iutools.concordancer.tm;

import org.iutools.concordancer.SentencePair;

import java.util.ArrayList;
import java.util.List;

public class WordSpotter {

	public WordSpotter() {

	}

	public WordSpotting[] spot(String sourceLang, String sourceWord,
										SentencePair[] alignedSents) {
		List<WordSpotting> spottings = new ArrayList<WordSpotting>();
		for (SentencePair sentPair: alignedSents) {
			WordSpotting aSpotting =
				new WordSpotting(sentPair);
			spottings.add(aSpotting);
		}

		return spottings.toArray(new WordSpotting[0]);
	}
}
