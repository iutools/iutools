package ca.pirurvik.iutools.spellchecker;

public class ScoredSpelling {
	public String spelling = null;
	public Double score = null;
	
	public ScoredSpelling() {
		
	}
	
	public ScoredSpelling(String _spelling, Double _score) {
		this.spelling = _spelling;
		this.score = _score;
	}
	
	public String toString() {
		String str = spelling+" (dist="+score+")";
		return str;
	}
}
