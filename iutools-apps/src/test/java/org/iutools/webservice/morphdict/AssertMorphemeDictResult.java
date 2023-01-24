package org.iutools.webservice.morphdict;

import ca.nrc.testing.AssertObject;
import ca.nrc.testing.AssertSet;
import org.apache.commons.lang3.tuple.Pair;
import org.iutools.linguisticdata.MorphemeHumanReadableDescr;
import static org.iutools.script.TransCoder.Script;

import org.iutools.script.TransCoder;
import org.iutools.webservice.AssertEndpointResult;
import org.iutools.webservice.EndpointResult;
import org.junit.jupiter.api.Assertions;

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

	public AssertMorphemeDictResult matchingMorphCanonicalsAre(String[] expCanonicalsArr) throws Exception {
		Set<MorphemeHumanReadableDescr> gotMorphemes = result().matchingMorphemesDescr();
		Set<String> gotCanonicals = new HashSet<>();
		for (MorphemeHumanReadableDescr descr: gotMorphemes) {
			gotCanonicals.add(descr.canonicalForm);
		}
		Set<String> expCanonicals = new HashSet<>();
		Collections.addAll(expCanonicals, expCanonicalsArr);
		AssertSet.assertEquals(
			"Morpheme canonical forms were wrong.",
			expCanonicals, gotCanonicals);

		return this;

	}

	public AssertMorphemeDictResult examplesAreInScript(Script expScript) {
		for (String morphID: result().examplesForMorpheme.keySet()) {
			String[] examples = result().examplesForMorpheme.get(morphID);
			for (String anExample: examples) {
				Script gotScript = TransCoder.textScript(anExample);
				Assertions.assertEquals(expScript, gotScript,
						baseMessage+"\nExample "+anExample+" for morpheme "+morphID+" was not in the expected IU script.");
			}
		}
		return this;
	}
}