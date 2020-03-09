package ca.pirurvik.iutools.spellchecker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ca.nrc.datastructure.Pair;

public class SpellingCorrection {
	public String orig;
	public Boolean wasMispelled = false;
	private List<String> possibleSpellings = new ArrayList<String>();
	private List<Double> possibleSpellingScores = null;
	private List<ScoredSpelling> scoredPossibleSpellings = 
				new ArrayList<ScoredSpelling>();

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
		if (_corrections != null) this.possibleSpellings = _corrections;
		if (_scores != null) this.possibleSpellingScores = _scores;
		if (_wasMispelled != null) this.wasMispelled = _wasMispelled;
	}
	
//	public SpellingCorrection setPossibleSpellings(List<Pair<String,Double>> scoredCandidates) {
//		
//		possibleSpellings = new ArrayList<String>();
//		possibleSpellingScores = new ArrayList<Double>();
//		scoredPossibleSpellings = new ArrayList<ScoredSpelling>();
//		for (Pair<String,Double> aCand: scoredCandidates) {
//			possibleSpellings.add(aCand.getFirst());
//			possibleSpellingScores.add(aCand.getSecond());
//		}
//				
//		if (scoredCandidates != null && scoredCandidates.size() > 0 && scoredCandidates.get(0).equals(orig)) {
//			this.possibleSpellings.remove(0);
//			this.possibleSpellingScores.remove(0);
//		}
//		
//		for (int ii=0; ii<scoredCandidates.size(); ii++) {
//			String spelling = scoredCandidates.get(ii).getFirst();
//			Double score = possibleSpellingScores.get(ii);
//			this.scoredPossibleSpellings.add(new ScoredSpelling(spelling, score));
//		}
//		
//		return this;
//	}

	public List<String> getPossibleSpellings() {
		return this.possibleSpellings;
	}

	public SpellingCorrection setPossibleSpellings(List<ScoredSpelling> _scoredSpellings) {
		scoredPossibleSpellings = scoredPossibleSpellings;

		possibleSpellings = new ArrayList<String>();
		possibleSpellingScores = new ArrayList<Double>();
		for (ScoredSpelling aCand: scoredPossibleSpellings) {
			possibleSpellings.add(aCand.spelling);
			possibleSpellingScores.add(aCand.score);
		}
				
		if (scoredPossibleSpellings != null 
				&& scoredPossibleSpellings.size() > 0 
				&& scoredPossibleSpellings.get(0).spelling.equals(orig)) {
			this.possibleSpellings.remove(0);
			this.possibleSpellingScores.remove(0);
			this.scoredPossibleSpellings.remove(0);
		}
				
		return this;
	}


}
