package org.iutools.corpus;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.iutools.linguisticdata.LinguisticDataException;
import org.iutools.datastructure.trie.StringSegmenter;
import org.iutools.datastructure.trie.StringSegmenterException;
import org.iutools.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.nrc.dtrc.elasticsearch.ElasticSearchException;


public class MockCompiledCorpus extends CompiledCorpus {
	
	public MockCompiledCorpus() throws CompiledCorpusException {
		super("mock_corpus");
		try {
			esFactory().indexAPI().delete();
		} catch (ElasticSearchException e) {
			throw new CompiledCorpusException(e);
		}
		segmenterClassName = MockStringSegmenter_IUMorpheme.class.getName();
		getSegmenter();
	}
	
	public void setDictionary(HashMap<String,String> _dictionary) throws CompiledCorpusException {
		((MockStringSegmenter_IUMorpheme)getSegmenter()).setDictionary(_dictionary);
	}
}


class MockStringSegmenter_IUMorpheme extends StringSegmenter {

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

	public String[] segment(String word) throws TimeoutException, StringSegmenterException{
		return segment(word,true);
	}

	@Override
	public String[] segment(String word, boolean fullAnalysis) throws TimeoutException, StringSegmenterException {
		String[] segments = null;
		String segmentsStr = dictionary.get(word);
		if (segmentsStr != null) {
			segments = segmentsStr.split(" ");
		} else {
			try {
				segments = new StringSegmenter_IUMorpheme().segment(word);
			} catch (LinguisticDataException e) {
				throw new StringSegmenterException(e);
			}
		}
		return segments;
	}
	
	public void setDictionary(HashMap<String,String> _dictionary) {
		dictionary = _dictionary;
	}

	@Override
	public void disactivateTimeout() {
	}

	@Override
	public String[][] possibleSegmentations(String string, boolean fullAnalysis)
			throws TimeoutException, StringSegmenterException {
		String[][] possSegs = null;
		String[] bestSegmentation = segment(string, fullAnalysis);
		if (bestSegmentation != null) {
			possSegs = new String[][] { segment(string, fullAnalysis) };
		}
		return possSegs;
	}
}
