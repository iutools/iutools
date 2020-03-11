package ca.pirurvik.iutools.spellchecker;

public class ScoredSpelling {
	String spelling = null;
	Double score = null;
	
	public ScoredSpelling(String _spelling, Double _score) {
		this.spelling = _spelling;
		this.score = _score;
	}
	
	public String toString() {
		String str = spelling+" (score="+score+")";
		return str;
	}
}
