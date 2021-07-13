package org.iutools.linguisticdata;

import ca.nrc.testing.AssertString;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

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
	public void test__description4id__SeveralCases() {
		String focusOnCase = null;
//		focusOnCase = "tit/tn-nom-p-2s";

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

//			Pair.of("mi/tn-loc-s", "???"),
//			Pair.of("mni/tn-loc-s-1s", "???"),
//			Pair.of("vugut/tv-dec-1p", "???"),
//			Pair.of("vugut/tv-dec-1p", "???"),


//			Pair.of("tn-nom-s-ps", "noun ending s-ps"),
//			Pair.of("tv-nom-s-ps", "verb ending s-ps"),

			Pair.of("lu/1q", "lu (tail element)")

		};

		for (Pair<String,String> aCase: cases) {
			String id = aCase.getLeft();
			String expDescr = aCase.getRight();
			if (focusOnCase != null && !focusOnCase.equals(id)) {
				continue;
			}
			String gotDescr = Morpheme.description4id(id);
			AssertString.assertStringEquals(
				"Bad description for morpheme id "+id, expDescr, gotDescr);
		}

		if (focusOnCase != null) {
			Assertions.fail("Test run only on one case. Make sure you test again with focusOnCase=null before you commit");
		}
	}
}
