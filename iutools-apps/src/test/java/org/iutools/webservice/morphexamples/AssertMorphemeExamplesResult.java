package org.iutools.webservice.morphexamples;

import ca.nrc.testing.AssertObject;
import ca.nrc.testing.AssertSet;
import org.apache.commons.lang3.tuple.Pair;
import org.iutools.linguisticdata.MorphemeHumanReadableDescr;
import org.iutools.webservice.AssertEndpointResult;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.MorphemeSearchResult;

import java.io.IOException;
import java.util.*;

public class AssertMorphemeExamplesResult extends AssertEndpointResult {

	public AssertMorphemeExamplesResult(EndpointResult _gotObject) {
		super(_gotObject);
	}

	public AssertMorphemeExamplesResult(EndpointResult _gotObject, String mess) {
		super(_gotObject, mess);
	}

	public void exampleScoredExamplesAre(Pair[] expExamples) throws Exception {
		Map<String,Double> expScoresMap = new HashMap<String,Double>();
		for (Pair<String,Double> anExpExample: expExamples) {
			expScoresMap.put(anExpExample.getLeft(), anExpExample.getRight());
		}
		Map<String, MorphemeSearchResult> gotExampleObjects =
			result().matchingWords;
		HashMap<String,Double> gotScoresMap = new HashMap<String,Double>();
		for (String morpheme: gotExampleObjects.keySet()) {
			MorphemeSearchResult morphResult = gotExampleObjects.get(morpheme);
			for (int ii=0; ii < morphResult.words.size(); ii++) {
				gotScoresMap.put(morphResult.words.get(ii), morphResult.wordScores.get(ii));
			}
		}

		AssertObject.assertDeepEquals(
			baseMessage+"\nScored examples not as expected.",
			expScoresMap, gotScoresMap);
	}

	private MorphemeExamplesResult result() {
		return (MorphemeExamplesResult)gotObject;
	}

	public AssertMorphemeExamplesResult matchingMorphemesAre(
		String... expMorphIDs) throws Exception {

		// First check the IDs of the matching morphemes
		Set<String> gotMorphIDs = result().matchingMorphemeIDs();
		AssertSet.assertEquals("", expMorphIDs, gotMorphIDs);

		// Then check the human-readable descriptions of the matching morphemes
		Set<MorphemeHumanReadableDescr> gotDescrSet = result().matchingMorphemesDescr();
		Set<MorphemeHumanReadableDescr> expDescrSet = new HashSet<MorphemeHumanReadableDescr>();
		for (String morphID: expMorphIDs) {
			expDescrSet.add(new MorphemeHumanReadableDescr(morphID));
		}
		AssertSet.assertEquals(
			"Descriptions of matching morphemes were wrong.",
			expDescrSet, gotDescrSet);



		return this;
	}
}