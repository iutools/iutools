package ca.nrc.datastructure.trie;

import java.util.ArrayList;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.inuktitutcomputing.data.LinguisticDataSingleton;
import ca.inuktitutcomputing.morph.MorphInuk;
import ca.inuktitutcomputing.morph.Decomposition;


public class StringSegmenter_IUMorpheme extends StringSegmenter {
	
	private static String name = "IUMorpheme";
	
	public String[] segment(String string) throws Exception {
		LinguisticDataSingleton.getInstance("csv");
		Decomposition [] decs = null;
		try {
			decs = MorphInuk.decomposeWord(string);
		} catch (Exception e) {
			throw e;
		}
        if (decs != null && decs.length>0) {
        	Decomposition dec = decs[0];
        	Pattern p = Pattern.compile("\\{[^:]+\\:(.+?)\\}") ;      
        	Matcher m = p.matcher(dec.toStr2()) ;
        	Vector v = new Vector();
        	while (m.find()) {
        		v.add("{"+m.group(1)+"}");
        	}
        	return (String[]) v.toArray(new String[v.size()]);
        }
        else {
        	return new String[]{};
        }
	}

}
