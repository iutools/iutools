package org.iutools.spellchecker;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import ca.inuktitutcomputing.script.TransCoder;
import ca.inuktitutcomputing.script.TransCoderException;

public class SpellingCorrection {
	public String orig;
		public SpellingCorrection setOrig(String _orig) {
			this.orig = _orig;
			return this;
		}
		
	public Boolean wasMispelled = false;
	public List<ScoredSpelling> scoredCandidates = 
				new ArrayList<ScoredSpelling>();
	
	private String correctLead;
		public SpellingCorrection setCorrectLead(String _correctLead) throws SpellCheckerException {
			try {
				this.correctLead = 
					TransCoder.ensureSameScriptAsSecond(_correctLead, orig);
			} catch (TransCoderException e) {
				throw new SpellCheckerException(e);
			}
			return this;
		}
		public String getCorrectLead() {
			return this.correctLead;
		}
		
	private String correctTail;
		public SpellingCorrection setCorrectTail(String _correctTail) throws SpellCheckerException {
			try {
				this.correctTail =
						TransCoder.ensureSameScriptAsSecond(_correctTail, orig);
			} catch (TransCoderException e) {
				throw new SpellCheckerException(e);
			}
			return this;
		}
		public String getCorrectTail() {
			return correctTail;
		}
	
	public SpellingCorrection() {
		initialize(null, null, null, null);		
	}

	public SpellingCorrection(String _orig, String[] _corrections, Boolean _wasMispelled) {
		List<String> correctionsList = Arrays.asList(_corrections);
		initialize(_orig, correctionsList, null, _wasMispelled);
	}

	public SpellingCorrection(String _orig, String[] _corrections, Double[] _scores, Boolean _wasMispelled) {
		List<String> correctionsList = Arrays.asList(_corrections);
		List<Double> scoresList = Arrays.asList(_scores);
		initialize(_orig, correctionsList, scoresList, _wasMispelled);
	}
	
	public SpellingCorrection(String _orig, List<String> _corrections) {
		initialize(_orig, _corrections, null, true);
	}
	
	public SpellingCorrection(String _orig) {
		initialize(_orig, null, null, null);
	}

	public SpellingCorrection(String word, boolean _wasMispelled) {
		initialize(word, null, null, _wasMispelled);
	}

	private void initialize(String _orig, List<String> _corrections, 
			List<Double> _scores, Boolean _wasMispelled) {
		this.setOrig(_orig);
		if (_wasMispelled != null) this.wasMispelled = _wasMispelled;
		if (_corrections != null && _scores != null) {
			for (int ii=0; ii < _corrections.size(); ii++) {
				String spelling = _corrections.get(ii);
				Double score = _scores.get(ii);
				scoredCandidates.add(new ScoredSpelling(spelling, score));
			}
		}
	}
	
	public List<String> getAllSuggestions() {
		Logger tLogger = Logger.getLogger("org.iutools.spellchecker.SpellingCorrection.getAllSuggestions");
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("invoked for orig="+orig);
		}

		List<String> suggestions = new ArrayList<String>();
		
		String highlightedCorrection = highlightIncorrectMiddle();
		if (highlightedCorrection != null) {
			suggestions.add(highlightedCorrection);			
		}
		suggestions.addAll(getPossibleSpellings());

		if (tLogger.isTraceEnabled()) {
			tLogger.trace("returning suggestions="+String.join(",", suggestions));
		}

		return suggestions;
	}


	public List<ScoredSpelling> getScoredPossibleSpellings() {
		return scoredCandidates;
	}

	@Transient
	public List<String> getPossibleSpellings() {
		List<String> possibleSpellings = new ArrayList<String>();
		for (ScoredSpelling scoredSpelling: scoredCandidates) {
			possibleSpellings.add(scoredSpelling.spelling);
		}
		return possibleSpellings;
	}

	public SpellingCorrection setPossibleSpellings(List<ScoredSpelling> _scoredSpellings) {
		scoredCandidates = _scoredSpellings;
				
		if (scoredCandidates != null 
				&& scoredCandidates.size() > 0 
				&& scoredCandidates.get(0).spelling.equals(orig)) {
			this.scoredCandidates.remove(0);
		}
				
		return this;
	}
	
	/** Highlight middle portion of the word that seem incorrect. */
	protected String highlightIncorrectMiddle() {
		Logger tLogger = Logger.getLogger("org.iutools.spellchecker.SpellingCorrection.highlightIncorrectMiddle");
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("invoked for orig="+orig+
				", correctLead="+correctLead+", correctTail="+correctTail);
		}
		
		String wordWithBadPortionsHighlighted = null;	
		if (correctLead != null && correctTail != null) {
			int highlightStart = -1;
			int highlightEnd = -1;
			int extremetiesLength = correctLead.length() + correctTail.length();
						
			if (extremetiesLength < orig.length()) {
				// There is a gap between the correct leads and tail.
				// So the problem must be in the characters in between.	
				//
				highlightStart = correctLead.length();
				highlightEnd = orig.length() - correctTail.length();
			} else if (extremetiesLength > orig.length()) {
				// The correct lead and tail overlap.
				// So the problem must be somewhere in the overlapping chars.
				//
				highlightStart = orig.length() - correctTail.length();
				highlightEnd = correctLead.length();
			} else {
				// Correct lead and tail exactly cover the word (no gap nor 
				// overlap). No highglighting to be done so leave the 
				// highlight start and end at -1
			}
			
			if (tLogger.isTraceEnabled()) {
				tLogger.trace("highlightStart="+highlightStart+", "+
					"highlightEnd="+highlightEnd);
			}
			
			if (highlightStart > 0 && highlightEnd > 0) {
				wordWithBadPortionsHighlighted = 
					orig.substring(0, highlightStart) + "[" +
					orig.substring(highlightStart, highlightEnd) + "]" +
					orig.substring(highlightEnd);
			}
		}
				
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("returning wordWithBadPortionsHighlighted="+
				wordWithBadPortionsHighlighted);
		}
				
		return wordWithBadPortionsHighlighted;
	}
	
	
	/** Highlight the portion of the word that precedes the apparently correct 
	 * leading text */
	private String highlightIncorrectLead() {
		String tail = 
				"[" + orig.substring(0, orig.length() - correctTail.length()) + 
				"]" + correctTail;
				
		return tail;
	}

	/** Highlight the portion of the word that follows the apparently correct 
	 * tailing text */
	private String highlightIncorrectTail() {
		String lead = 
			correctLead + "[" + orig.substring(correctLead.length()) + "]";
		
		return lead;
	}

}
