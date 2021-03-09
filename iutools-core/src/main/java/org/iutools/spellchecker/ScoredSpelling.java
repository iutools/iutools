package org.iutools.spellchecker;

public class ScoredSpelling {
	public String spelling = null;
	public Double ngramSim = null;
	public Double editDist = null;
	public Long frequency = null;

	public ScoredSpelling() {
		init_ScoredSpelling((String)null, (Double)null, (Double)null);
	}

	public ScoredSpelling(String _spelling) {
		init_ScoredSpelling(_spelling, (Double)null, (Double)null);
	}

	public ScoredSpelling(String _spelling, Double _ngramSim) {
		init_ScoredSpelling(_spelling, _ngramSim, (Double)null);
	}

	static int compareSpellings(ScoredSpelling p1, ScoredSpelling p2) {
		// Favor spellings whose edit distance to the original input
		// is smaller
		//
		int score = p1.editDist.compareTo(p2.editDist);
		String how = "editDist (p1.(p2)";

		if (score == 0) {
			// If two spellings have the same edit distance w.r.t. original input,
			// frequency to break the tie.
			score = p2.frequency.compareTo(p1.frequency);
			how = "frequency (p2.(p1))";
		}
		SpellDebug.traceCandidateScoreComparison(
			"ScoredSpelling.compareSpellings", p1, p2, score,
			"Using "+how+", score="+score);
		return score;
	}

	private void init_ScoredSpelling(
		String _spelling, Double _ngramSim, Double _editDist) {
		this.spelling = _spelling;
		this.ngramSim = _ngramSim;
		this.editDist = _editDist;
	}

	public String toString() {
		String str = spelling+" (ngramSim="+ ngramSim +"; editDist="+editDist+
			"; frequency="+frequency+")";
		return str;
	}

	public ScoredSpelling setFrequency(Long _freq) {
		this.frequency = _freq;
		return this;
	}

	@Override
	public boolean equals(Object other) {
		boolean answer = false;
		if (other instanceof ScoredSpelling) {
			ScoredSpelling otherSpelling = (ScoredSpelling)other;
			answer = this.spelling.equals(otherSpelling.spelling);
		}
		return answer;
	}

	@Override
	public int hashCode() {
		return this.spelling.hashCode();
	}
}
