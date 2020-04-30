package ca.pirurvik.iutools.corpus;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

import ca.nrc.datastructure.trie.StringSegmenter;
import ca.nrc.datastructure.trie.StringSegmenterException;


public class MockCompiledCorpus extends CompiledCorpus {
	
	
	public MockCompiledCorpus() throws CompiledCorpusException {	
		segmenterClassName = MockStringSegmenter_IUMorpheme.class.getName();
		getSegmenter();
	}
	
	public void setDictionary(HashMap<String,String> _dictionary) throws CompiledCorpusException {
		((MockStringSegmenter_IUMorpheme)getSegmenter()).setDictionary(_dictionary);
	}
	

}


class MockStringSegmenter_IUMorpheme extends StringSegmenter {

	private HashMap<String,String> dictionary;

	public MockStringSegmenter_IUMorpheme() {
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
}
