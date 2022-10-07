package org.iutools.morphemedict;

/**
 * Word that provides an example of use for a given morpheme
 */
public class MorphWordExample implements Comparable {
	public String word = null;
	public String isExampleOfMorpheme = null;
	public Double score = null;
	public Long frequency = null;
	public String decomp = null;
	
	public MorphWordExample(String _word, Double _score, Long _freq) {
		init__MorphWordExample(_word, (String)null, _score,
			_freq, (String)null);
	}

	private void init__MorphWordExample(String _word, String _isExampleOfMorpheme,
		Double _score, Long _freq, String _decomp) {
		this.word = _word;
		this.isExampleOfMorpheme = _isExampleOfMorpheme;
		this.score = _score;
		this.frequency = _freq;
		this.decomp = _decomp;
	}
	
	@Override
	public int compareTo(Object obj) {
		if (this.score < ((MorphWordExample)obj).score)
			return 1;
		else if (this.score > ((MorphWordExample)obj).score)
			return -1;
		else
			return 0;
	}
}
