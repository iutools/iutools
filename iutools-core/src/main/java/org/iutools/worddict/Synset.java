package org.iutools.worddict;

public class Synset {
	public String[] synonyms = new String[0];

	/** Empty constructor for Jackson de-serialization */
	public Synset() {
		init_Synset((String[])null);
	}

	private void init_Synset(String[] _synonyms) {
		if (_synonyms != null) {
			this.synonyms = _synonyms;
		}
	}
}
