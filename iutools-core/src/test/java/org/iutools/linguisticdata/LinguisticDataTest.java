package org.iutools.linguisticdata;

import ca.nrc.testing.RunOnCases;
import ca.nrc.testing.RunOnCases.*;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

public class LinguisticDataTest {

	//////////////////////////////////////
	// DOCUMENTATION TESTS
	//////////////////////////////////////

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

	//////////////////////////////////////
	// VERIFICATION TESTS
	//////////////////////////////////////

	@Test
	public void test__getMorpheme__VariousCases() throws Exception {
		Case[] cases = new Case[]{
			new Case("iqqanaijaq/1v", "iqqanaijaq/1v"),
		};

		Consumer<Case> runner = (aCase) -> {
			String morphID = (String) aCase.data[0];
			Morpheme gotMorpheme = LinguisticData.getInstance().getMorpheme(morphID);
			if (aCase.expectsNull()) {
				Assert.assertEquals(
					aCase.descr + "\nMorpheme SHOULD have been null",
					null, gotMorpheme);
			} else {
				Assert.assertFalse(
					aCase.descr + "\nMorpheme should NOT have been null",
					null == gotMorpheme);

				// TODO: Add some assertions here
			}
		};

		new RunOnCases(cases, runner)
			.run();
	}
}
