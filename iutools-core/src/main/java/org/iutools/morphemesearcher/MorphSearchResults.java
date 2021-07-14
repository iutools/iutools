package org.iutools.morphemesearcher;

import org.iutools.linguisticdata.Morpheme;
import org.iutools.linguisticdata.MorphemeException;

import java.util.List;

public class MorphSearchResults {
	
	public String morphemeWithId;
	public String morphemeDescr;
	public List<ScoredExample> words;
	
	public MorphSearchResults(String _morphemeWithId, List<ScoredExample> _words) throws MorphemeException {
		this.morphemeWithId = _morphemeWithId;
		this.morphemeDescr = Morpheme.humanReadableDescription(morphemeWithId);
		this.words = _words;
	}

}
