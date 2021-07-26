package org.iutools.concordancer;

import ca.nrc.testing.AssertString;
import org.apache.commons.lang3.tuple.Pair;
import ca.nrc.testing.AssertObject;
import ca.nrc.testing.Asserter;
import org.junit.jupiter.api.Assertions;

public class AssertSentencePair extends Asserter<SentencePair> {
	public AssertSentencePair(SentencePair _gotObject) {
		super(_gotObject);
	}

	public AssertSentencePair(SentencePair _gotObject, String mess) {
		super(_gotObject, mess);
	}

	public AssertSentencePair langsAre(String lang1, String lang2) throws Exception {
		Pair<String,String> gotLangs = pair().langs();
		Pair<String,String> expLangs = Pair.of(lang1,lang2);
		AssertObject.assertDeepEquals(
			baseMessage+"\nlanguage pair was not as expected.",
			expLangs, gotLangs
		);
		return this;
	}

	public AssertSentencePair textForLangIs(String lang, String expText) throws Exception {
		String gotText = pair().getText(lang);
		AssertObject.assertDeepEquals(
			baseMessage+"\nText was wrong for language: "+lang,
			expText, gotText
		);
		return this;
	}

	public AssertSentencePair tokensForLangAre(String lang, String[] expTokens)
		throws Exception {
		String[] gotTokens = pair().getTokens(lang);
		AssertObject.assertDeepEquals(
			baseMessage+"\nTokens were wrong for language: "+lang,
			expTokens, gotTokens
		);

		return this;
	}

	public SentencePair pair() {
		return this.gotObject;
	}

	public AssertSentencePair correspondingTokensAre(String lang,
																	 int start, int end, int expRightStart, int expRightEnd) throws Exception {
		return correspondingTokensAre(lang, start, end,
			expRightStart, expRightEnd, (Integer)null, (Integer)null);
	}

	public AssertSentencePair correspondingTokensAre(String lang,
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
			pair().getCorrespondingTokenOffsets(lang, start, end);

		AssertObject.assertDeepEquals(
			baseMessage+"Corresponding tokens were not as expected",
			expCorr, gotCorr
		);
		return this;
	}

	public void otherLangTokensAre(
		String lang, int[] langTokens, int... expOtherLangTokens)
		throws Exception {
		int[] gotOtherLangTokens = pair().otherLangTokens(lang, langTokens);
		AssertObject.assertDeepEquals(
			baseMessage+"\nOther language tokens not as expected",
			expOtherLangTokens, gotOtherLangTokens);
	}
}
