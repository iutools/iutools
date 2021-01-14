package org.iutools.concordancer;

import org.apache.commons.lang3.tuple.Pair;
import ca.nrc.testing.AssertObject;
import ca.nrc.testing.Asserter;

public class AssertAlignment extends Asserter<Alignment> {
	public AssertAlignment(Alignment _gotObject) {
		super(_gotObject);
	}

	public AssertAlignment(Alignment _gotObject, String mess) {
		super(_gotObject, mess);
	}

	public AssertAlignment langsAre(String lang1, String lang2) throws Exception {
		Pair<String,String> gotLangs = alignment().langs();
		Pair<String,String> expLangs = Pair.of(lang1,lang2);
		AssertObject.assertDeepEquals(
			baseMessage+"\nlanguage pair was not as expected.",
			expLangs, gotLangs
		);
		return this;
	}

	public AssertAlignment textForLangIs(String lang, String expText) throws Exception {
		String gotText = alignment().getText(lang);
		AssertObject.assertDeepEquals(
			baseMessage+"\nText was wrong for language: "+lang,
			expText, gotText
		);
		return this;
	}

	public AssertAlignment tokensForLangAre(String lang, String... expTokens)
		throws Exception {
		String[] gotTokens = alignment().getTokens(lang);
		AssertObject.assertDeepEquals(
			baseMessage+"\nTokens were wrong for language: "+lang,
			expTokens, gotTokens
		);

		return this;
	}

	public Alignment alignment() {
		return this.gotObject;
	}

	public AssertAlignment correspondingTokensAre(String lang,
		int start, int end, int expRightStart, int expRightEnd) throws Exception {
		return correspondingTokensAre(lang, start, end,
			expRightStart, expRightEnd, (Integer)null, (Integer)null);
	}

	public AssertAlignment correspondingTokensAre(String lang,
		int start, int end, int expRightStart, int expRightEnd,
		Integer expLefStart, Integer expLeftEnd)
		throws Exception {

		Pair<Integer,Integer> expLeft = Pair.of(start,end);
		if (expLefStart != null && expLefStart != null) {
			expLeft = Pair.of(expLefStart, expLeftEnd);
		}
		Pair<Integer,Integer> expRight = Pair.of(expRightStart, expRightEnd);
		Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> expCorr =
			Pair.of(expLeft, expRight);

		Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> gotCorr =
			alignment().getCorrespondingTokenOffsets(lang, start, end);

		AssertObject.assertDeepEquals(
			baseMessage+"Corresponding tokens were not as expected",
			expCorr, gotCorr
		);
		return this;
	}
}
