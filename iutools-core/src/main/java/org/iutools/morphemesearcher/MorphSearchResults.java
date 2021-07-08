package org.iutools.morphemesearcher;

import org.iutools.linguisticdata.Morpheme;

import java.util.List;

public class MorphSearchResults {
	
	public String morphemeWithId;
	public String morphemeDescr;
	public List<ScoredExample> words;
	
	public MorphSearchResults(String _morphemeWithId, List<ScoredExample> _words) {
		this.morphemeWithId = _morphemeWithId;
		this.morphemeDescr = Morpheme.description4id(morphemeWithId);
		this.words = _words;
	}

}
