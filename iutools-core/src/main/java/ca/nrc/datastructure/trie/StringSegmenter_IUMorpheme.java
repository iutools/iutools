package ca.nrc.datastructure.trie;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.inuktitutcomputing.data.LinguisticDataException;
import ca.inuktitutcomputing.morph.MorphInukException;
import ca.inuktitutcomputing.morph.MorphologicalAnalyzer;
import ca.inuktitutcomputing.morph.MorphologicalAnalyzerException;
import ca.inuktitutcomputing.script.Syllabics;
import ca.inuktitutcomputing.morph.Decomposition;


public class StringSegmenter_IUMorpheme extends StringSegmenter {
	
	private MorphologicalAnalyzer morphAnalyzer;
	
	public StringSegmenter_IUMorpheme() throws StringSegmenterException {
		try {
			morphAnalyzer = new MorphologicalAnalyzer();
		} catch (LinguisticDataException e) {
			throw new StringSegmenterException(e);
		}
	}
	
	public String[] segment(String string) throws TimeoutException, StringSegmenterException, LinguisticDataException {
		return segment(string,false);
	}

	public String[] segment(String string, boolean fullAnalysis) throws TimeoutException, StringSegmenterException, LinguisticDataException {
		String[] bestSegmentation = null;
		String[][] allSegmentations = possibleSegmentations(string, fullAnalysis);
		if (allSegmentations != null && allSegmentations.length > 0) {
			bestSegmentation = allSegmentations[0];
		}
		
		return bestSegmentation;
	}
	
	public MorphologicalAnalyzer getAnalyzer() {
		return morphAnalyzer;
	}

	@Override
	public void disactivateTimeout() {
		getAnalyzer().disactivateTimeout();
	}

	@Override
	public String[][] possibleSegmentations(String string, boolean fullAnalysis)
			throws TimeoutException, StringSegmenterException {
		List<String[]> allSegmentations = new ArrayList<String[]>();
		String word = string;
		if (Syllabics.allInuktitut(string)) {
			word = Syllabics.transcodeToRoman(string); 
		}
		
		Decomposition[] decs = null;
		try {
			decs = morphAnalyzer.decomposeWord(word);
		} catch (MorphologicalAnalyzerException e) {
			throw new StringSegmenterException(e);
		}
		
        if (decs != null && decs.length > 0) {
        	for (Decomposition dec: decs) {
	        	Pattern p = Pattern.compile("(\\{[^:]+\\:(.+?)\\})") ;      
	        	Matcher m;
				try {
					m = p.matcher(dec.toStr2());
				} catch (LinguisticDataException e) {
					throw new StringSegmenterException(e);
				}
	        	Vector<String> v = new Vector<String>();
	        	while (m.find()) {
	        		if (fullAnalysis) {
	        			v.add(m.group(1));
	        		} else {
	        			v.add("{"+m.group(2)+"}");
	        		}
	        	}
	        	allSegmentations.add((String[]) v.toArray(new String[v.size()]));
        	}
        }
        
        String[][] allSegmentationsArr = new String[allSegmentations.size()][];
        int pos = 0;
        for (String[] aSegmentation: allSegmentations) {
        	allSegmentationsArr[pos] = aSegmentation;
        	pos++;
        }

        return allSegmentationsArr;
	}
	
	

}
