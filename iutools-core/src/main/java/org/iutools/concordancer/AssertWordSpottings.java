package org.iutools.concordancer;

import ca.nrc.string.StringUtils;
import ca.nrc.testing.Asserter;
import org.junit.jupiter.api.Assertions;

import java.util.*;

public class AssertWordSpottings extends Asserter<WordSpotting[]> {
	public AssertWordSpottings(WordSpotting[] _gotObject) {
		super(_gotObject);
	}

	public AssertWordSpottings(WordSpotting[] _gotObject, String mess) {
		super(_gotObject, mess);
	}

	public void wasSpottedInAllSentences() {
		Map<WordSpotting, List<String>> missing = new HashMap<WordSpotting,List<String>>();
		for (WordSpotting aSpotting: spottings()) {
			List<String> problematicLangs = new ArrayList<String>();
			for (String lang: new String[] {"en", "fr"})
			if (aSpotting.offsets(lang).isEmpty()) {
				problematicLangs.add(lang);
			}
			if (!problematicLangs.isEmpty()) {
				missing.put(aSpotting, problematicLangs);
			}
		}
		String mess =
			"Word or its equivalent was missing from some sentence pairs.\n"+
			"List of these pairs below:\n";
		for (WordSpotting aSpotting: missing.keySet()) {
			List<String> missingLangs = missing.get(aSpotting);
			mess +=
				aSpotting.sentencePair().toString()+"\n";
			mess +=
				"Missing word in langs: "+
				StringUtils.join(missingLangs.iterator(), ", ")+"\n"+
				"-----\n";
		}
		Assertions.assertTrue(missing.isEmpty(), mess);
	}

	public WordSpotting[] spottings() {
		return this.gotObject;
	}
}
