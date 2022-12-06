package org.iutools.webservice.morphexamples;

import ca.nrc.testing.AssertObject;
import ca.nrc.testing.AssertSet;
import org.apache.commons.lang3.tuple.Pair;
import org.iutools.linguisticdata.MorphemeHumanReadableDescr;
import org.iutools.webservice.AssertEndpointResult;
import org.iutools.webservice.EndpointResult;

import java.util.*;

public class AssertMorphemeExamplesResult extends AssertEndpointResult {

	@Override
	protected MorphemeExamplesResult result() {
		return (MorphemeExamplesResult)gotObject;
	}

	public AssertMorphemeExamplesResult(EndpointResult _gotObject) {
		super(_gotObject);
	}

	public AssertMorphemeExamplesResult(EndpointResult _gotObject, String mess) {
		super(_gotObject, mess);
	}

	public void examplesForMorphemeAre(String morphID, String... expExamples)
		throws Exception {
		String[] gotExamples = result().examplesForMorpheme.get(morphID);
		AssertObject.assertDeepEquals(
			baseMessage+"\nWord examples not as expected for morpheme "+morphID,
			expExamples, gotExamples);
	}

	public AssertMorphemeExamplesResult matchingMorphemesAre(
		Pair<String,String>... expIDsAndDefs) throws Exception {

		// Then check the human-readable descriptions of the matching morphemes
		Set<MorphemeHumanReadableDescr> gotMorphemes = result().matchingMorphemesDescr();
		Set<MorphemeHumanReadableDescr> expMorphemes = new HashSet<MorphemeHumanReadableDescr>();
		for (Pair<String,String> idAndDef: expIDsAndDefs) {
			expMorphemes.add(
				new MorphemeHumanReadableDescr(
					idAndDef.getLeft(), idAndDef.getRight()));
		}
		AssertSet.assertEquals(
			"Descriptions of matching morphemes were wrong.",
			expMorphemes, gotMorphemes);

		return this;
	}
}