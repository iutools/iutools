package org.iutools.webservice.morphexamples;

import ca.nrc.testing.AssertObject;
import ca.nrc.testing.AssertSet;
import org.apache.commons.lang3.tuple.Pair;
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
		String... expMorphIDs) throws IOException {
		Set<String> gotMorphIDs = result().matchingMorphemes();
		AssertSet.assertEquals("", expMorphIDs, gotMorphIDs);

		return this;
	}

	public AssertMorphemeExamplesResult matchingMorphemesDescriptionsAre(
		String... expDescriptions) throws IOException {
		Set<String> expDescrSet = new HashSet<String>();
		Collections.addAll(expDescrSet, expDescriptions);
		Set<String> gotDescrSet = result().matchingMorphemesDescr();
		AssertSet.assertEquals(
			"Descriptions of matching morphemes were wrong.", expDescrSet, gotDescrSet);

		return this;
	}
}