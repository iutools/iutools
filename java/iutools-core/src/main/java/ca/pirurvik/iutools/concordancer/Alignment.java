package ca.pirurvik.iutools.concordancer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.nrc.datastructure.Pair;

public class Alignment {

	public boolean misaligned = false;
	Map<String,String> langSentences = new HashMap<String,String>();
	
	public Alignment() {
		init_Alignment(null, null, null, null, null);
	}

	public Alignment(String lang1, String text1, 
			String lang2, String text2) {
		init_Alignment(lang1, text1, lang2, text2, null);
	}
	
	public Alignment(String lang1, String text1, 
			String lang2, String text2, boolean _misaligned) {
		init_Alignment(lang1, text1, lang2, text2, _misaligned);
	}

	protected void init_Alignment(String lang1, String text1, 
			String lang2, String text2, Boolean _misaligned) {
		if (_misaligned == null) {
			_misaligned = false;
		}
		langSentences.put(lang1, text1);
		langSentences.put(lang2, text2);
		misaligned = _misaligned;
	}

	public String getText(String lang) {
		return langSentences.get(lang);
	}
	
	public Pair<String,String> langs() {
		String[] langsArr = langSentences.keySet().toArray(new String[langSentences.keySet().size()]);
	    
		Pair<String,String> langPair = null;
		if (langsArr[0].compareTo(langsArr[1]) < 0) { 
			langPair = Pair.of(langsArr[0], langsArr[1]);
		} else {
			langPair = Pair.of(langsArr[1], langsArr[0]);			
		}
		
		return langPair;
	}
	
	public String toString() {
		Pair<String,String> langPair = langs();
		
		String toStr = 
				"(" + 
				langPair.getFirst()+":" + getText(langPair.getFirst()) +
				" <--> " +
				langPair.getSecond()+":" + getText(langPair.getSecond()) +
				")"
				;

		return toStr;
	}

}
