package ca.pirurvik.iutools.morphemesearcher;

public class ScoredExample {
	public String word;
	public Double score;
	public Long frequency;
	
	public ScoredExample(String _word, Double _score, Long _freq) {
		this.word = _word;
		this.score = _score;
		this.frequency = _freq;
	}
}
