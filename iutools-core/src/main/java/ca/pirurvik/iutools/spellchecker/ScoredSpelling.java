package ca.pirurvik.iutools.spellchecker;

public class ScoredSpelling {
	public String spelling = null;
	public Double score = null;
	
	public ScoredSpelling() {
		init_ScoredSpelling((String)null, (Double)null);
	}

	public ScoredSpelling(String _spelling) {
		init_ScoredSpelling(_spelling, (Double)null);
	}

	public ScoredSpelling(String _spelling, Double _score) {
		init_ScoredSpelling(_spelling, _score);
	}

	private void init_ScoredSpelling(String _spelling, Double _score) {
		this.spelling = _spelling;
		this.score = _score;
	}

	public String toString() {
		String str = spelling+" (dist="+score+")";
		return str;
	}
}
