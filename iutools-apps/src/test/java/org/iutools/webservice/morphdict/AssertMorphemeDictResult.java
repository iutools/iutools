package org.iutools.webservice.morphdict;

import ca.nrc.testing.AssertObject;
import ca.nrc.testing.AssertSet;
import org.apache.commons.lang3.tuple.Pair;
import org.iutools.linguisticdata.MorphemeHumanReadableDescr;
import org.iutools.webservice.AssertEndpointResult;
import org.iutools.webservice.EndpointResult;

import java.util.*;

public class AssertMorphemeDictResult extends AssertEndpointResult {

	@Override
	protected MorphemeDictResult result() {
		return (MorphemeDictResult)gotObject;
	}

	public AssertMorphemeDictResult(EndpointResult _gotObject) {
		super(_gotObject);
	}

	public AssertMorphemeDictResult(EndpointResult _gotObject, String mess) {
		super(_gotObject, mess);
	}

	public void examplesForMorphemeAre(String morphID, String... expExamples)
		throws Exception {
		String[] gotExamples = result().examplesForMorpheme.get(morphID);
		AssertObject.assertDeepEquals(
			baseMessage+"\nWord examples not as expected for morpheme "+morphID,
			expExamples, gotExamples);
	}

	public AssertMorphemeDictResult matchingMorphIDsAre(String... expIDsArr) throws Exception {
		Set<MorphemeHumanReadableDescr> gotMorphemes = result().matchingMorphemesDescr();
		Set<String> gotIDs = new HashSet<>();
		for (MorphemeHumanReadableDescr descr: gotMorphemes) {
			gotIDs.add(descr.id);
		}
		Set<String> expIDs = new HashSet<>();
		Collections.addAll(expIDs, expIDsArr);
		AssertSet.assertEquals(
			"Morpheme IDs were wrong.",
			expIDs, gotIDs);

		return this;

	}
	public AssertMorphemeDictResult matchingMorphemesAre(
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