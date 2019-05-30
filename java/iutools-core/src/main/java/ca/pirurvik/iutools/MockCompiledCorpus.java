package ca.pirurvik.iutools;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import ca.nrc.datastructure.trie.StringSegmenter;


public class MockCompiledCorpus extends CompiledCorpus {
	
	
	public MockCompiledCorpus() throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {	
		segmenterClassName = MockStringSegmenter_IUMorpheme.class.getName();
		getSegmenter();
	}
	
	public void setDictionary(HashMap<String,String> _dictionary) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		((MockStringSegmenter_IUMorpheme)getSegmenter()).setDictionary(_dictionary);
	}
	

}


class MockStringSegmenter_IUMorpheme extends StringSegmenter {

	private HashMap<String,String> dictionary;

	public MockStringSegmenter_IUMorpheme() {
	}
	
	public String[] segment(String word) throws Exception {
		return segment(word,true);
	}

	@Override
	public String[] segment(String word, boolean fullAnalysis) throws Exception {
		return dictionary.get(word).split(" ");
	}
	
	public void setDictionary(HashMap<String,String> _dictionary) {
		dictionary = _dictionary;
	}
}
