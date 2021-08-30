package org.iutools.morph;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.iutools.bin.Decompose;
import org.iutools.linguisticdata.LinguisticDataException;
import org.iutools.linguisticdata.MorphemeHumanReadableDescr;
import org.iutools.morph.r2l.MorphologicalAnalyzer_R2L;
import org.iutools.script.Syllabics;
import ca.nrc.datastructure.Pair;

public class Gist {
	
	public String word = null;
	public Pair<String,String>[] wordComponents = null;
	public Map<String,MorphemeHumanReadableDescr>
		morphemesDefAndGrammar = new HashMap<String,MorphemeHumanReadableDescr>();

	public Gist() {}

	public Gist(String word) throws LinguisticDataException {
		this.word = word;
		boolean syllabic = false;
		String latin;
		Decomposition[] decs;
		MorphemeHumanReadableDescr[] morphDescriptions = null;

		MorphologicalAnalyzer_R2L morphAnalyzer = new MorphologicalAnalyzer_R2L();
		
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
				String[] meaningsOfParts = Decompose.getMeaningsInArrayOfStrings(dec.toString(),"en",true,false);
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
