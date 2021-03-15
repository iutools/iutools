package org.iutools.webservice.morphexamples;

import ca.nrc.testing.AssertObject;
import org.apache.commons.lang3.tuple.Pair;
import org.iutools.webservice.AssertEndpointResult;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.MorphemeSearchResult;

import java.util.HashMap;
import java.util.Map;

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
			response().matchingWords;
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

	private MorphemeExamplesResult response() {
		return (MorphemeExamplesResult)gotObject;
	}
}