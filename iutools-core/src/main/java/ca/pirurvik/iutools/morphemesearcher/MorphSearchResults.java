package ca.pirurvik.iutools.morphemesearcher;

import java.util.List;

public class MorphSearchResults {
	
	public String morphemeWithId;
	public List<ScoredExample> words;
	
	public MorphSearchResults(String _morphemeWithId, List<ScoredExample> _words) {
		this.morphemeWithId = _morphemeWithId;
		this.words = _words;
	}

}
