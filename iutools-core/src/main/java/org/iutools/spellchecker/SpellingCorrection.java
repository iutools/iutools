package org.iutools.spellchecker;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.iutools.script.TransCoder;
import org.iutools.script.TransCoderException;

/**
 * Result of a spell check on a word done with the SpellChecker class.
 */
public class SpellingCorrection {
	public String orig;
		
	/** Whether or not the word was deemed mis-spelled */
	public Boolean wasMispelled = false;

	/**
	 * Fix done by the SHALLOW CHARACTER-level analysis.
	 */
	public String shallowFix = null;
	
	/**
	 * Possible fixes done by the DEEP MORPHOLOGICAL analysis
	 */
	public List<ScoredSpelling> deepFixes = new ArrayList<ScoredSpelling>();

	/**
	 * Longest correct portion at the START of the word
	 */
	private String correctLead;
		
	/**
	 * Longest correct portion at the END of the word
	 */
	private String correctTail;
	
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

	private void initialize(String _orig, List<String> _corrections, List<Double> _scores, Boolean _wasMispelled) {
		this.setOrig(_orig);
		this.shallowFix = null;
		if (_wasMispelled != null) this.wasMispelled = _wasMispelled;
		if (_corrections != null) {
			for (int ii=0; ii < _corrections.size(); ii++) {
				String spelling = _corrections.get(ii);
				Double score = 1.0;
				if (_scores != null) {
					score = _scores.get(ii);
				}
				deepFixes.add(new ScoredSpelling(spelling, score));
			}
		}
	}

	public String bestSuggestionSoFar() {
		return bestSuggestionSoFar((Boolean)null);
	}

	/**
	 * When applying different correction strategies in sequence, this will
	 * return the best "partial" correction we got from previous strategies.
	 * You can ask for brackets to be removed from the suggestions or not.
	 */
	public String bestSuggestionSoFar(Boolean allowBadCharsMarker) {
		if (allowBadCharsMarker == null) {
			allowBadCharsMarker = true;
		}
		String best = orig;
		if (shallowFix != null) {
			// Shallow rules did produce a partial fix.
			best = shallowFix;
//			best = best.replaceAll("(\\[|\\])", "");
		}
		if (!allowBadCharsMarker) {
			// Remove the bad word markers from the shallow fix
			best = best.replaceAll("(\\[|\\])", "");
		}
		return best;
	}

	public SpellingCorrection setOrig(String _orig) {
		this.orig = _orig;
		return this;
	}

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

	public String topSuggestion() {
		String top = orig;
		if (wasMispelled) {
			List<String> suggestions = getDeepSuggestions();
			if (!suggestions.isEmpty()) {
				top = suggestions.get(0);
			}
		}
		return top;
	}

	public List<String> getAllSuggestions() {
		Logger tLogger = LogManager.getLogger("org.iutools.spellchecker.SpellingCorrection.getAllSuggestions");
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("invoked for orig="+orig);
		}

		List<String> suggestions = new ArrayList<String>();

		// We may add 3 types of suggestions
		//
		// Shallow correction:
		//    This is a single suggestion that results from applying shallow rules
		//    that only look for sequences of characters that can NEVER appear in
		//    a valid Inuktitut word
		//
		// Deep corrections:
		//    These are suggestions that also ensure no morpheme composition rules
		//    are violated.
		//
		// Highlighted corrections
		//   This is a single suggestion that highlights the longest correct
		//   portions at start and end of the word (ex: nunav[vv]ut)
		//

		// We always start with the Highlighted correction
		String highlightedCorrection = highlightIncorrectMiddle();
		if (highlightedCorrection != null) {
			addNoDups(suggestions, highlightedCorrection);
		}

		// Next, we add the SHALLOW correction if one was found
		if (shallowFix != null) {
			addNoDups(suggestions, shallowFix);
		}

		// Next, add either Deep corrections (if we have some)
		//
		for (String deepSugg: getDeepSuggestions()) {
			addNoDups(suggestions, deepSugg);
		}

		if (tLogger.isTraceEnabled()) {
			tLogger.trace("returning suggestions="+String.join(",", suggestions));
		}

		return suggestions;
	}

	private void addNoDups(List<String> suggestions, String newSuggestion) {
		if (!suggestions.contains(newSuggestion)) {
			suggestions.add(newSuggestion);
		}
		return;
	}


	public List<ScoredSpelling> getScoredPossibleSpellings() {
		return deepFixes;
	}

	@Transient
	public List<String> getDeepSuggestions() {
		List<String> possibleSpellings = new ArrayList<String>();
		for (ScoredSpelling scoredSpelling: deepFixes) {
			possibleSpellings.add(scoredSpelling.spelling);
		}
		return possibleSpellings;
	}

	public SpellingCorrection setPossibleSpellings(List<ScoredSpelling> _scoredSpellings) {
		deepFixes = _scoredSpellings;
				
		if (deepFixes != null
				&& deepFixes.size() > 0
				&& deepFixes.get(0).spelling.equals(orig)) {
			this.deepFixes.remove(0);
		}
				
		return this;
	}
	
	/** Highlight middle portion of the word that seem incorrect. */
	protected String highlightIncorrectMiddle() {
		Logger tLogger = LogManager.getLogger("org.iutools.spellchecker.SpellingCorrection.highlightIncorrectMiddle");
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

	/**
	 * Checks if the shallowFix highlights faulty characters
	 * (ex: nunavu[mm]it)
	 */
	public boolean shallowFixHighlightsFaultyChars() {
		return shallowFix.matches("^.*\\[.*\\].*$");
	}
}
