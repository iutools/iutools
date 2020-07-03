package ca.pirurvik.iutools;

import java.util.ArrayList;
import java.util.List;

import ca.nrc.testing.AssertObject;
import ca.nrc.testing.Asserter;

public class AssertQueryExpansionArray extends Asserter<QueryExpansion[]> {

	public AssertQueryExpansionArray(QueryExpansion[] gotExpansions, String mess) {
		super(gotExpansions, mess);
	}
	
	public QueryExpansion[] expansions() {
		return gotObject;
	}
	
	public AssertQueryExpansionArray wordsAre(String[] expWords) 
		throws Exception {
		List<String> gotWords = new ArrayList<String>();
		for (QueryExpansion anExpansion: expansions()) {
			gotWords.add(anExpansion.getWord());
		}
		AssertObject.assertDeepEquals(
			baseMessage+"\nExpansion words were not as expected", 
			expWords, gotWords);
		
		return this;
	}

}
