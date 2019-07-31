package ca.inuktitutcomputing.morph;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.inuktitutcomputing.applications.Decompose;
import ca.inuktitutcomputing.data.LinguisticDataSingleton;
import ca.inuktitutcomputing.script.Syllabics;
import ca.nrc.datastructure.Pair;

public class Gist {
	
	public String word = null;
	public Pair<String,String>[] wordComponents = null;

	public Gist(String word) {
		this.word = word;
		boolean syllabic = false;
		String latin;
		Decomposition[] decs;
		
		MorphologicalAnalyzer morphAnalyzer = new MorphologicalAnalyzer();
		
		Pattern pattern = Pattern.compile("^(.+?)---(.+)$");

		try {
			if (Syllabics.allInuktitut(word)) {
				latin = Syllabics.transcodeToRoman(word);
				syllabic = true;
			}
			else
				latin = word;
			decs = morphAnalyzer.decomposeWord(latin);
			if (decs != null && decs.length > 0) {
				Decomposition dec = decs[0];
				String[] meaningsOfParts = Decompose.getMeaningsInArrayOfStrings(dec.toStr2(),"en",true,false);
				wordComponents = new Pair[meaningsOfParts.length];
				for (int i=0; i<meaningsOfParts.length; i++) {
					Matcher matcher = pattern.matcher(meaningsOfParts[i]);
					matcher.matches();
					wordComponents[i] = new Pair<String,String>(matcher.group(1),matcher.group(2));
				}
			}
		} catch (Exception e) {
			wordComponents = new Pair[0];
		}
	}
}
