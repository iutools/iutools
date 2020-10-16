package ca.pirurvik.iutools.morphrelatives;

import java.util.ArrayList;
import java.util.List;

import ca.nrc.testing.AssertObject;
import ca.nrc.testing.Asserter;

public class AssertMorphologicalRelativeArray extends Asserter<MorphologicalRelative[]> {

	public AssertMorphologicalRelativeArray(MorphologicalRelative[] gotExpansions, String mess) {
		super(gotExpansions, mess);
	}
	
	public MorphologicalRelative[] expansions() {
		return gotObject;
	}
	
	public AssertMorphologicalRelativeArray wordsAre(String[] expWords) 
		throws Exception {
		List<String> gotWords = new ArrayList<String>();
		for (MorphologicalRelative anExpansion: expansions()) {
			gotWords.add(anExpansion.getWord());
		}
		AssertObject.assertDeepEquals(
			baseMessage+"\nExpansion words were not as expected", 
			expWords, gotWords);
		
		return this;
	}

}
