package org.iutools.morphrelatives;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;


/**
 * Represents a word that is morphologically close to another words
 * @author desilets
 *
 */
public class MorphologicalRelative {
	
	private String word;
		public String getWord() {return word;}
		public void setWord(String _word) {
			this.word = _word;
			this.toStringCached = null;
		}
	private long frequency; 
		public long getFrequency() {return frequency;}
 	private String[] morphemes = new String[0];
 		public String[] getMorphemes() {return morphemes;}
 		public void setMorphemes(String[] _morphemes) {
 			this.morphemes = _morphemes;
		}
	private String origWord = null;
 		public String getOrigWord() {return origWord;}
	private String[] origMorphemes = null;
		public String[] getOrigMorphemes() {return origMorphemes;}
	private List<String> commonMorphemes = null;
		public List<String> getCommonMorphemes() {return commonMorphemes;}
	
	private String toStringCached = null;

	public MorphologicalRelative() {};

	public MorphologicalRelative(String _word, String[] _morphemes,
			long _frequency, String[] _origMorphemes) {
		init_MorphologicalRelative(_word, _morphemes, _frequency, null, 
				origMorphemes);
	}
	
	public MorphologicalRelative(String _word, String[] _morphemes, 
			long _frequency) {
		init_MorphologicalRelative(_word, _morphemes, _frequency, null, 
				null);
	}
	
	public MorphologicalRelative(String _word, String[] _morphemes, 
			long _frequency, String _origWord) {
		init_MorphologicalRelative(_word, _morphemes, _frequency, _origWord, 
				null);
	}
	
	public MorphologicalRelative(String _word, String[] _morphemes, 
		long _frequency, String _origWord, String[] _origMorphemes) {
		init_MorphologicalRelative(_word, _morphemes, _frequency, _origWord, 
			_origMorphemes);
	}

	private void init_MorphologicalRelative(String _word, String[] _morphemes, long _frequency, String _origWord,
			String[] _origMorphemes) {
		this.word = _word;
		if (_morphemes != null) {
			this.morphemes = _morphemes;
		}
		this.frequency = _frequency;
		this.origWord = _origWord;
		this.origMorphemes  = _origMorphemes;
	}

	/**
	 * Computes the number of steps you have to take in the morpheme
	 * tree to get from original word to morphological relative
	 * @return
	 */
	public int morphologicalDistance() {
		int numCommon = morphemesInCommon().size();
		
		int distance =
				 // Need to backtrack by that many steps to get to
				 // from the original word to the common root of both words
				 origMorphemes.length - numCommon
				 
				 // Then need to move forward by that many steps to 
				 // get to the expansion word
				+ morphemes.length - numCommon;
		
		return distance;
	}
	
	public String toString() {
		if (toStringCached == null) {
			StringBuilder builder = new StringBuilder();
			builder.append(word);
			builder.append("::f=");
			builder.append(frequency);
			builder.append("::m=");
			for (String morph: morphemes) {
				builder.append(morph);
			}
			builder.append("::o=");
			if (origWord != null) {
				builder.append(origWord);
			} else {
				builder.append("null");
			}
			builder.append("::om=");
			if (origMorphemes != null) {
				for (String morph: origMorphemes) {
					builder.append(morph);
				}			
			} else {
				builder.append("null");
			}	
			
			toStringCached = builder.toString();
		}
		
		return toStringCached;
	}

	public double percentMorphsInCommon() throws MorphRelativesFinderException {
		Logger tLogger = Logger.getLogger("org.iutools.morphrelatives.MorphologicalRelative");
		double numInCommon = 1.0 * morphemesInCommon().size();
		double denominator = Math.max(origMorphemes.length, morphemes.length);
		double percentInCommon = numInCommon / denominator;

		MRelsTracer.traceRelative(tLogger, this, "returning percentInCommon="+percentInCommon);
		return percentInCommon;
	}

	public List<String> morphemesInCommon() {
		List<String> inCommon = null;
		
		// We can only compute the common morphemes if we were provided with 
		// the original word's morphemes at construction time
		//
		if (origMorphemes != null) {
			if (commonMorphemes == null) {
				inCommon = morphemesInCommonWith(origMorphemes);
			}
		}
		
		return inCommon;
	}

	
	public List<String> morphemesInCommonWith(String[] otherWordMorphemes) {
		List<String> common = new ArrayList<String>();
		for (int ii=0; 
			 ii < Math.min(morphemes.length, otherWordMorphemes.length);
			 ii++) {
			String morpheme = morphemes[ii];
			if (morpheme.equals(otherWordMorphemes[ii])) {
				common.add(morpheme);
			} else {
				break;
			}
		}
		
		return common;
	}
	
	@Override
    public int hashCode() {
        int hash = toString().hashCode();
        return hash;
    }	
	
	@Override
	public boolean equals(Object other) {		
		Boolean same = null;
		
		
		if (!this.getClass().isInstance(other) && 
			!other.getClass().isInstance(this)) {
			same = false;
		}
		
		if (same == null && !toString().equals(other.toString())) {
			same = false;
		}
		
		if (same == null) {
			same = true;
		}
		
		return same;
	}
}
