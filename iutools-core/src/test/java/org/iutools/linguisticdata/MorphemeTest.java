package org.iutools.linguisticdata;

import ca.nrc.testing.AssertString;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class MorphemeTest {

	//////////////////////////////////
	// DOCUMENTATION TESTS
	//////////////////////////////////

	@Test
	public void test__Morpheme__Synopsis() throws Exception {
		// Typically, you create a Morpheme by retrieving it from the LinguisticData
		// singleton
		String morphID = "inuk/1n";
		Morpheme morph = LinguisticData.getInstance().getMorpheme(morphID);

		// The Morpheme class also has some static methods that allow you to
		// get information about the morpheme without retrieving it from the
		// LinguisticData singleton. This only applies to information that can
		// be deduced directly from the morpheme's ID
		//

		// This returns a human-readable description of what the morpheme
		// is about
		String descr = Morpheme.humanReadableDescription(morphID);

		// This returns the canonical form of the morpheme ('inuk' in this case)
		String canonicalForm = Morpheme.canonicalForm(morphID);

		Pair<String, String> typeConstraints = Morpheme.typeConstraints(morphID);

		// Type of word that this morpheme can attach to
		// If the type is "%", it means the morpheme is a root which can only
		// be found at the start of a word.
		String attachesToType = typeConstraints.getLeft();

		// When a word is extended with the morpheme, it becomes a word of this
		// type.
		// If the type is '%', it means the morpheme is a terminal that can only be
		// found at the end of a word.
		String resultsIn = typeConstraints.getRight();
	}

	//////////////////////////////////
	// VERIFICATION TESTS
	//////////////////////////////////

	@Test
	public void test__hasCanonicalForm__HappyPath() throws Exception {
		String morpheme = "{inuk/1v}";
		Assert.assertTrue(
			"Morpheme "+morpheme+" should have had canonical form inuk",
			Morpheme.hasCanonicalForm(morpheme, "inuk"));
		Assert.assertFalse(
			"Morpheme "+morpheme+" should NOT have had canonical form it",
			Morpheme.hasCanonicalForm(morpheme, "it"));
	}

	@Test
	public void test__humanReadableDescription__HappyPath() throws Exception {
		String morphID = "umiaq/1n";
		String expDescr = "umiaq (noun root)";
		String gotDescr = Morpheme.humanReadableDescription(morphID);
		AssertString.assertStringEquals(
			"Bad description for morpheme id "+morphID, expDescr, gotDescr);
	}

	@Test
	public void test__isComposite() {
		LinguisticData lingData = LinguisticData.getInstance();
		Morpheme morph = lingData.getMorpheme("tit/1vv");
		Assert.assertFalse(morph.isComposite());
		morph = lingData.getMorpheme("ilinniaqtit/1v");
		Assert.assertTrue(morph.isComposite());
	}

	@Test
	public void test__canonicalForm__HappyPath() throws Exception {
		String morphID = "inuk/1n";
		String gotCanonical = Morpheme.canonicalForm(morphID);
		AssertString.assertStringEquals(
			"Wrong canonical form for morpheme "+morphID,
			"inuk", gotCanonical
		);
	}

	@Test
	public void test__typeConstraints__VariousCases() throws Exception {
		String[][] cases = new String[][] {
			new String[] {"inuk/1n", "%", "n"},
			new String[] {"it/tn-gen-p", "X", "%"},
			new String[] {"t/1vv", "v", "v"},
			new String[] {"aq/2nv", "n", "v"}
		};

		String focusOnCase = null;
//		focusOnCase = "t/1vv";

		for (String[] aCase: cases) {
			String morphID = aCase[0];
			String expAttachesTo = aCase[1];
			String expResultsIn = aCase[2];
			if (focusOnCase != null && !focusOnCase.equals(morphID)) {
				continue;
			}
			Pair<String,String> constraints = Morpheme.typeConstraints(morphID);
			AssertString.assertStringEquals(
				"Wrong 'attachesTo' for morpheme "+morphID,
				expAttachesTo, constraints.getLeft()
			);
			AssertString.assertStringEquals(
				"Wrong 'resultsIn' for morpheme "+morphID,
				expResultsIn, constraints.getRight()
			);
		}

		if (focusOnCase != null) {
			Assertions.fail("Test run only on one case. Don't forget to set focusOnCase=null");
		}
	}
}
