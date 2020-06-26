package ca.pirurvik.iutools.corpus;

public class WordWithMorpheme {
	public String word;
	public String morphemeId;
	public String decomposition;
	public Long frequency;
	
	public WordWithMorpheme(String _word, String _morphId, String _decomp, Long _freq) {
		init_WordWithMorpheme(_word, _morphId, _decomp, _freq);
	}

	public WordWithMorpheme(String _word, String _morphId, String _decomp, long _freq) {
		init_WordWithMorpheme(_word, _morphId, _decomp, new Long(_freq));
	}
	
	private void init_WordWithMorpheme(String _word, String _morphId, String _decomp, Long _freq) {
		this.word = _word;
		if (_morphId != null) {
			_morphId = _morphId.replaceAll("(^\\{|\\}$)", "");
		}
		this.morphemeId = _morphId;
		this.decomposition = _decomp;
		this.frequency = _freq;
	}
}
