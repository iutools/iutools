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
			Pair.of("ilinniaqtit/1v", "ilinniaqtit (verb)"),
			Pair.of("tit/1vv", "tit (verb-to-verb)"),
			Pair.of("tit/tn-nom-p-2s", "tit (tn-noun-p-2s)"),
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
