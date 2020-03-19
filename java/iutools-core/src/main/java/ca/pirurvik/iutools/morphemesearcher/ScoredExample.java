package ca.pirurvik.iutools.morphemesearcher;

public class ScoredExample implements Comparable {
	public String word;
	public Double score;
	public Long frequency;
	
	public ScoredExample(String _word, Double _score, Long _freq) {
		this.word = _word;
		this.score = _score;
		this.frequency = _freq;
	}
	
	@Override
	public int compareTo(Object obj) {
		if (this.score < ((ScoredExample)obj).score)
			return 1;
		else if (this.score > ((ScoredExample)obj).score)
			return -1;
		else
			return 0;
	}
}
