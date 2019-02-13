package ca.nrc.datastructure.trie;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.inuktitutcomputing.data.LinguisticDataSingleton;
import ca.inuktitutcomputing.morph.MorphInuk;
import ca.inuktitutcomputing.script.Syllabics;
import ca.inuktitutcomputing.morph.Decomposition;


public class StringSegmenter_IUMorpheme extends StringSegmenter {
	
	public String[] segment(String string) throws Exception {
		return segment(string,false);
	}

	public String[] segment(String string, boolean fullAnalysis) throws Exception {
		LinguisticDataSingleton.getInstance("csv");
		Decomposition [] decs = null;
		String word = string;
		if (Syllabics.allInuktitut(string))
			word = Syllabics.transcodeToRoman(string); 
		try {
			decs = MorphInuk.decomposeWord(word);
		} catch (Exception e) {
			throw e;
		}
        if (decs != null && decs.length>0) {
        	Decomposition dec = decs[0];
        	Pattern p = Pattern.compile("(\\{[^:]+\\:(.+?)\\})") ;      
        	Matcher m = p.matcher(dec.toStr2()) ;
        	Vector<String> v = new Vector<String>();
        	while (m.find()) {
        		if (fullAnalysis) {
        			v.add(m.group(1));
        		} else {
        			v.add("{"+m.group(2)+"}");
        		}
        	}
        	return (String[]) v.toArray(new String[v.size()]);
        }
        else {
        	return new String[]{};
        }
	}

}
