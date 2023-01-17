package org.iutools.linguisticdata.index;

import ca.nrc.testing.AssertObject;
import ca.nrc.testing.AssertSet;
import ca.nrc.testing.RunOnCases;
import static ca.nrc.testing.RunOnCases.Case;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.iutools.linguisticdata.Morpheme;
import org.iutools.linguisticdata.MorphemeHumanReadableDescr;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;


public class MorphemeIndexTest {

	///////////////////////////////////////////
	// DOCUMENTATION TESTS
	//////////////////////////////////////////
	@Test
	public void test__MorphemeIndex__Synopsis() throws Exception {
		// Use a MorphemeIndex to index and search morphemes.
		MorphemeIndex index = new MorphemeIndex("iutools_test");

		// You can search a morpheme by providing three pieces of information, some
		// of which may be null:
		// - written form
		// - grammatical role
		// - meaning
		//
		String writtenForm = "tut";
		String grammaticalRole = "verb";
		String meaning = "to step";
		List<Morpheme> morphemes = index.searchMorphemes(writtenForm, grammaticalRole, meaning);
	}

	///////////////////////////////////////////
	// VERIFICATION TESTS
	//////////////////////////////////////////

	@Test
	public void test__searchMorphemes__VariousCases() throws Exception {

		Case[] cases = new Case[] {

			new Case("Just canonical form; form has exact matches and more",
		"tut", null, null,
				"tut/1v", "tut/tn-sim-s", "tutaq/1n", "tutarut/1n", "tuti/1v", "tutik/1v",
				"tutiriaq/1n", "tutiriarmiutaq/1n"),

			new Case("Just canonical form; form only has matches that start with that form",
		"titi", null, null,
				"titiq/1v", "titiqqartalik/1n", "titiraq/1v", "titirarvigi/1v", "titirarvik/1n"),

			new Case("canonical form + grammar", "tut", "verb", null,
				"tut/1v", "tuti/1v", "tutik/1v"),

			new Case("canonical form + meaning", "tut", null, "step",
				"tuti/1v"),

			new Case("grammar + meaning", null, "verb", "step",
				"abluq/1v", "tuti/1v"),

			new Case("canonical + grammar + meaning", "tut", "verb", "step",
				"tuti/1v"),


		};

		Consumer<Case> runner = (caze) -> {
			if (caze.data.length < 3) {
				throw new RuntimeException("Data did not have the right length");
			}
			String canonical = (String)caze.data[0];
			String grammar = (String)caze.data[1];
			String meaning = (String)caze.data[2];
			List<String> expIDs = new ArrayList<>();
			for (int ii=3; ii < caze.data.length; ii++) {
				expIDs.add((String)caze.data[ii]);
			}

			MorphemeIndex index = new MorphemeIndex("iutools_test");
			List<Morpheme> gotMorphemes = null;
			try {
				gotMorphemes = index.searchMorphemes(canonical, grammar, meaning);
				assertMorphemeIDsAre("", gotMorphemes, expIDs);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};

		new RunOnCases(cases, runner)
//			.onlyCaseNums(1)
			.run();
	}

	//////////////////////////////////
	// TEST HELPERS
	//////////////////////////////////


	private void assertMorphemeIDsAre(String mess, List<Morpheme> gotMorphemes, List<String> expMorphIDs) throws Exception {
		String[] expIdsArr = expMorphIDs.toArray(new String[0]);
		assertMorphemeIDsAre(mess, gotMorphemes, expIdsArr);
	}

	private void assertMorphemeIDsAre(String mess, List<Morpheme> gotMorphemes,
		String... expMorphIDs) throws Exception {
		Set<String> gotMorphIDs = new HashSet<>();
		for (Morpheme morph: gotMorphemes) {
			gotMorphIDs.add(morph.id);
		}
		AssertSet.assertEquals(
			mess+"\nIDs of morphemes were not as expected",
			expMorphIDs, gotMorphIDs);
	}
}
