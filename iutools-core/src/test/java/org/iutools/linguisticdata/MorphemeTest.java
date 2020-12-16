package org.iutools.linguisticdata;

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

}
