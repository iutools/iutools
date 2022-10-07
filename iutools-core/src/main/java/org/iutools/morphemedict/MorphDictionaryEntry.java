package org.iutools.morphemedict;

import org.iutools.linguisticdata.Morpheme;
import org.iutools.linguisticdata.MorphemeException;

import java.util.ArrayList;
import java.util.List;

public class MorphDictionaryEntry {
	
	public String morphemeWithId;
	public String morphemeDescr;
	public List<MorphWordExample> words = new ArrayList<MorphWordExample>();

	public MorphDictionaryEntry(String _morphemeWithId) throws MorphemeException {
		this.init_MorphDictionaryEntry(_morphemeWithId, null);
	}

	public MorphDictionaryEntry(String _morphemeWithId, List<MorphWordExample> _words) throws MorphemeException {
		this.init_MorphDictionaryEntry(_morphemeWithId, _words);
	}

	public void init_MorphDictionaryEntry(String _morphemeWithId, List<MorphWordExample> _words) throws MorphemeException {
		this.morphemeWithId = _morphemeWithId;
		this.morphemeDescr = Morpheme.humanReadableDescription(morphemeWithId);
		if (_words != null) {
			this.words = _words;
		}
	}
}
