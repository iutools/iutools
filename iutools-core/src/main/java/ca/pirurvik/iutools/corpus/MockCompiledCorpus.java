package ca.pirurvik.iutools.corpus;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import ca.nrc.datastructure.trie.StringSegmenter;
import ca.nrc.datastructure.trie.StringSegmenterException;


public class MockCompiledCorpus extends CompiledCorpus_InMemory {
	
	
	public MockCompiledCorpus() throws CompiledCorpusException {	
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
		return dictionary.get(word).split(" ");
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
		return new String[][] { segment(string, fullAnalysis) };
	}
}
