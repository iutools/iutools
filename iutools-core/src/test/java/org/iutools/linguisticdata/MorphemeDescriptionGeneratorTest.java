package org.iutools.linguisticdata;

import ca.nrc.testing.AssertString;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MorphemeDescriptionGeneratorTest {

	@Test
	public void test__description4id__SeveralCases() throws Exception {
		String focusOnCase = null;
//		focusOnCase = "mni/tn-loc-s-1s";

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

//			Pair.of("mi/tn-loc-s", "noun ending; singular locative case"),
//			Pair.of("mni/tn-loc-s-1s", "posessive noun ending; locative singular case; 1st person singular posessor"),
//			Pair.of("vugut/tv-dec-1p", "vugut (intransitive verb ending; declarative mood; 1st person plural subject)"),
//			Pair.of("gakku/tv-caus-1s-3s", "gakku (transitive verb ending; causative mood; 1st person singular subject;3rd person singular object)"),

			Pair.of("lu/1q", "lu (tail element)")
		};

		for (Pair<String,String> aCase: cases) {
			String id = aCase.getLeft();
			String expDescr = aCase.getRight();
			if (focusOnCase != null && !focusOnCase.equals(id)) {
				continue;
			}
			String gotDescr = MorphemeDescriptionGenerator.humanReadableDescription(id);
			AssertString.assertStringEquals(
				"Bad description for morpheme id "+id, expDescr, gotDescr);
		}

		if (focusOnCase != null) {
			Assertions.fail("Test run only on one case. Make sure you test again with focusOnCase=null before you commit");
		}
	}


}
