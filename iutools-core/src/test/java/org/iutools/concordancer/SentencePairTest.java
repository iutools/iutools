package org.iutools.concordancer;

import ca.nrc.testing.AssertString;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SentencePairTest {

	private SentencePair pair = null;

	@BeforeEach
	public void setUp() {
		pair = new SentencePair(
			"en", "Hello world",
			"fr", "Bonjour le monde"
		);


		// You can also provide token-level alignment information
		//
		pair
			.setTokenAlignments(
				"en", new String[] {"Hello", "world"},
				"fr", new String[] {"Bonjour", "le", "monde"},
				new Integer[][] {
					new Integer[]{0,0},
					new Integer[]{1,1}, new Integer[]{1,2}});
			;

	}

	//////////////////////////////////////
	// DOCUMENTATION TEST
	//////////////////////////////////////

	@Test
	public void test__SentencePair__Synopsis() {
		// Use SentencePair objects to represent a pair of aligned sentences
		// in different languages
		//
		// For example:
		//
		SentencePair alignment = new SentencePair(
			"en", "Hello world",
			"fr", "Bonjour le monde"
		);

		// If this is a bad sentence alignment (i.e. if the two sentences do not
		// actually correspond to each other, you can set
		//
		alignment.setMisaligned(true);

		// But in this case, let's assume that they are correctly aligned...
		//
		alignment.setMisaligned(false);

		// You can query an alignment for its list of languages
		Pair<String, String> langs = alignment.langPair();

		// You can get the text of the sentence in a given language
		String enSent = alignment.getText("en");
		String iuSent = alignment.getText("fr");

		// You can also provide token-level alignment information
		//
		alignment
			.setTokenAlignments(
				"en", new String[] {"Hello", "world"},
				"fr", new String[] {"Bonjour", "le", "monde"},
				new Integer[][] {
					// 1st en token aligns with 1st fr token
					new Integer[]{0,0},
					// 2nd en token aligns with 2nd and 3rd fr tokens
					new Integer[]{1,1}, new Integer[]{1,2}});

		// If token alignment has been provided, then given a list of tokens in
		// one language, you can get the corresponding tokens in the other language
		//
		int[] frTokens = new int[] {1, 2};
		int[] enTokens = pair.otherLangTokens("fr", frTokens);
	}

	/////////////////////////////
	// VERIFICATION TESTS
	/////////////////////////////

	@Test
	public void test__SentencePair__HappyPath() throws Exception {

		new AssertSentencePair(pair)
			.langsAre("en", "fr")
			.textForLangIs("en", "Hello world")
			.textForLangIs("fr", "Bonjour le monde")
			.tokensForLangAre("en", new String[] {"Hello", "world"})
			.tokensForLangAre("fr", new String[] {"Bonjour", "le", "monde"})
			.otherLangTokensAre("en", new int[] {1}, 1, 2)
			;
	}

	@Test
	public void test__stemmedTokensPattern__TextThatContainsParens() {
		String token = "hell (world";
		SentencePair.stemmedTokensPattern(token);
	}

	@Test
	public void test__escapeRegexpSpecialChars__SeveralCases() {
		Integer focusOnCase = null;

		String[][] escCases = new String[][] {
			new String[] {"hello", "hello"},
			new String[] {"hello+world", "hello\\+world"},
		};

		int caseNum = -1;
		for (String[] aCase: escCases) {
			String orig = aCase[0];
			String expEscaped = aCase[1];
			String gotEscaped = SentencePair.escapeRegexpSpecialChars(orig);
			AssertString.assertStringEquals(
				"Escaped string not as expected for case#"+caseNum,
				expEscaped, gotEscaped
			);
		}
	}
}
