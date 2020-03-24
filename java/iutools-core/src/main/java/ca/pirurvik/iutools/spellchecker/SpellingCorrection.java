package ca.pirurvik.iutools.spellchecker;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ca.nrc.datastructure.Pair;

public class SpellingCorrection {
	public String orig;
	public Boolean wasMispelled = false;
	public List<ScoredSpelling> scoredCandidates = 
				new ArrayList<ScoredSpelling>();
	public String correctLead;
	public String correctTail;
	
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
		this.orig = _orig;
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
		List<String> suggestions = new ArrayList<String>();
		
		suggestions.addAll(getPartiallyCorrect());
		suggestions.addAll(getPossibleSpellings());
		
		return suggestions;
	}

	@JsonIgnore
	private List<String> getPartiallyCorrect() {
		List<String> partiallyCorrect = new ArrayList<String>();
		if (partiallyCorrectExtremities() != null) {
			partiallyCorrect.add(partiallyCorrectExtremities());
		} else {
			partiallyCorrect.add(correctLead);
			partiallyCorrect.add(correctTail);
		}
		
		return partiallyCorrect;
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
	
	public String partiallyCorrectExtremities() {
		String correctPortions = null;
		if (correctLead != null && correctTail != null) {
			int middleStart = correctLead.length();
			int middleEnd = orig.length() - correctTail.length();
			if (middleStart < middleEnd) {
				correctPortions = 
					correctLead+
					"["+orig.substring(middleStart, middleEnd)+"]"+
					correctTail;
			}
		}
		
		return correctPortions;
	}
}
