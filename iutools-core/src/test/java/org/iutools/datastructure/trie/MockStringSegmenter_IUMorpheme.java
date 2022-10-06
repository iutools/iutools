package org.iutools.datastructure.trie;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.iutools.linguisticdata.LinguisticDataException;

public class MockStringSegmenter_IUMorpheme extends StringSegmenter {

	private Map<String,String> dictionary = new HashMap<String,String>();

	public MockStringSegmenter_IUMorpheme() {
		init_MockStringSegmenter_IUMorpheme();
	}
	
	private void init_MockStringSegmenter_IUMorpheme() {
		dictionary.put("inuit", "{inuk/1n} {it/tn-nom-p}");
		dictionary.put("nunami", "{nuna/1n} {mi/tn-loc-s}");
		dictionary.put("iglumik", "{iglu/1n} {mik/tn-acc-s}");
		dictionary.put("inuglu", "{inuk/1n} {lu/1q}");
	}

	@Override
	public String[] segment(String word, Boolean fullAnalysis) throws TimeoutException, StringSegmenterException {
		String[] decompositions = null;
		
		String decompsStr = dictionary.get(word);
		if (decompsStr != null) {
			decompositions = decompsStr.split((" "));
		}
		
		if (decompositions == null) {
			decompositions =
				new StringSegmenter_IUMorpheme().segment(word, fullAnalysis);
		}
		
		return decompositions;
	}
	
	public void setDictionary(HashMap<String,String> _dictionary) {
		dictionary = _dictionary;
	}
	
	@Override
	public void disactivateTimeout() {
	}

	@Override
	public String[][] possibleSegmentations(String string, Boolean fullAnalysis)
	throws TimeoutException, StringSegmenterException {
		return new String[][] { segment(string, fullAnalysis) };
	}
}
