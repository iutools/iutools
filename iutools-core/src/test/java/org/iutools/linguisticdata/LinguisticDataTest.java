package org.iutools.linguisticdata;

import org.junit.jupiter.api.Test;

public class LinguisticDataTest {

	@Test
	public void test__LinguisticData__Synopsis() {
		// The LinguisticData class contains information about the various
		// morphemes used in the Inuktitut language
		//
		// You typically obtain it as a singleton

		LinguisticData data = LinguisticData.getInstance();

		// You can get an array with the IDs of all morphemes in the database
		String[] allMorphemeIDs = data.allMorphemeIDs();

		// Given a morpheme ID, you can get information about it
		for (String anID: allMorphemeIDs) {
			Morpheme morpheme = data.getMorpheme(anID);
		}
	}
}
