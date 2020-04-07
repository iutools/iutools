package ca.pirurvik.iutools.concordancer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.nrc.datastructure.Pair;

public class Alignment {

	public boolean misaligned = false;
	Map<String,String> langText = new HashMap<String,String>();
	
	public Alignment() {
		init_Alignment(null, null, null, null, null);
	}

	public Alignment(String lang1, String textLang1, 
			String lang2, String textLang2) {
		init_Alignment(lang1, textLang1, lang2, textLang2, null);
	}
	
	public Alignment(String lang1,  String textLang1, 
			String lang2, String textLang2, boolean _misaligned) {
		init_Alignment(lang1, textLang1, lang2, textLang2, _misaligned);
	}

	protected void init_Alignment(String lang1, String textLang1, 
			String lang2, String textLang2, Boolean _misaligned) {
		if (_misaligned == null) {
			_misaligned = false;
		}
		langText.put(lang1, textLang1);
		langText.put(lang2, textLang2);
		misaligned = _misaligned;
	}

	public String getText(String lang) {
		return langText.get(lang);
	}
	
	public Pair<String,String> langs() {
		String[] langsArr = langText.keySet().toArray(new String[langText.keySet().size()]);
	    
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
				langPair.getFirst()+":\"" + getText(langPair.getFirst()) +
				"\" <--> " +
				langPair.getSecond()+":\"" + getText(langPair.getSecond()) +
				"\")"
				;

		return toStr;
	}
}
