package org.iutools.linguisticdata;

import ca.nrc.testing.AssertSet;
import ca.nrc.testing.AssertString;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Ignore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

public class MorphemeHumanReadableDescrTest {

	@Test
	public void test__description4id__SeveralCases() throws Exception {
		String focusOnCase = null;
//		focusOnCase = "vugut/tv-dec-1p";

		Pair<String,String>[] cases = new Pair[] {
			Pair.of("umiaq/1n", "umiaq (noun root)"),
			Pair.of("pisuk/1v", "pisuk (verb root)"),
			Pair.of("amma/1c", "amma (conjunction)"),
			Pair.of("aakka/1a", "aakka (adverb)"),
			Pair.of("aamai/1e", "aamai (expression/disclaimer)"),
			Pair.of("uvanga/1p", "uvanga (pronoun)"),
			Pair.of("quti/1nn", "quti (noun-to-noun suffix)"),
			Pair.of("liuq/1nv", "liuq (noun-to-verb suffix)"),
			Pair.of("ji/1vn", "ji (verb-to-noun suffix)"),
			Pair.of("nasuk/1vv", "nasuk (verb-to-verb suffix)"),

			Pair.of("mi/tn-loc-s", "mi (noun ending; locative singular)"),
			Pair.of("mni/tn-loc-s-1s", "mni (posessive noun ending; locative singular; 1st person singular posessor)"),
			Pair.of("vugut/tv-dec-1p", "vugut (intransitive verb ending; declarative 1st person plural)"),
			Pair.of("gakku/tv-caus-1s-3s", "gakku (transitive verb ending; causative 1st person singular; 3rd person singular object)"),

			Pair.of("lu/1q", "lu (tail element)")
		};

		for (Pair<String,String> aCase: cases) {
			String id = aCase.getLeft();
			String expDescr = aCase.getRight();
			if (focusOnCase != null && !focusOnCase.equals(id)) {
				continue;
			}
			String gotDescr = MorphemeHumanReadableDescr.humanReadableDescription(id);
			AssertString.assertStringEquals(
				"Bad description for morpheme id "+id, expDescr, gotDescr);
		}

		if (focusOnCase != null) {
			Assertions.fail("Test run only on one case. Make sure you test again with focusOnCase=null before you commit");
		}
	}

	@Test @Ignore
	public void test__humanReadableDescr__RunOnAllMorphemesInDB() throws Exception {
		Set<String> allDescriptions = new HashSet<String>();
		for (String morphID: LinguisticData.getInstance().allMorphemeIDs()) {
			try {
				String descr = MorphemeHumanReadableDescr.humanReadableDescription(morphID);
				String[] parts = descr.split("\\(");
				String grammDescr = parts[1];
				allDescriptions.add(grammDescr);
			} catch (Exception e) {
				throw new Exception("Exception raising while processing id: "+morphID, e);
			}
		}

		String[] expDescriptions = new String[] {
			"BLAH"
		};
		AssertSet.assertEquals(
			"Bag of descriptions was not as expected", expDescriptions, allDescriptions);
	}

}
