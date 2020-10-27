package ca.pirurvik.iutools.corpus;

public class WordWithMorpheme {
	public String word;
	public String morphemeId;
	public String decomposition;
	public Long frequency;
	public String[][] decompsSample;

	public WordWithMorpheme(String _word, String _morphId, String _decomp,
		Long _freq) {
		init_WordWithMorpheme(_word, _morphId, _decomp, _freq,
			(String[][])null);
	}

	public WordWithMorpheme(String _word, String _morphId, String _decomp,
		long _freq, String[][] _decompsSample) {
		init_WordWithMorpheme(_word, _morphId, _decomp, new Long(_freq),
			_decompsSample);
	}
	
	private void init_WordWithMorpheme(String _word, String _morphId,
		String _topDecomp, Long _freq, String[][] _decompsSample) {
		this.word = _word;
		if (_morphId != null) {
			_morphId = _morphId.replaceAll("(^\\{|\\}$)", "");
		}
		this.morphemeId = _morphId;
		this.decomposition = _topDecomp;
		this.frequency = _freq;
		this.decompsSample = _decompsSample;
	}
}
