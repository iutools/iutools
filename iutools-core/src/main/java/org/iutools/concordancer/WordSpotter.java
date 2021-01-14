package org.iutools.concordancer;

import java.util.ArrayList;
import java.util.List;

public class WordSpotter {

	public WordSpotter() {

	}

	public WordSpotting[] spot(String sourceLang, String sourceWord,
										Alignment[] alignedSents) {
		List<WordSpotting> spottings = new ArrayList<WordSpotting>();
		for (Alignment sentPair: alignedSents) {
			WordSpotting aSpotting =
				new WordSpotting(sentPair);
			spottings.add(aSpotting);
		}

		return spottings.toArray(new WordSpotting[0]);
	}
}
