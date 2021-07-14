package org.iutools.linguisticdata;

import ca.nrc.testing.AssertString;
import org.junit.Assert;
import org.junit.Test;

public class MorphemeTest {

	@Test
	public void test__hasCanonicalForm__HappyPath() {
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
}
