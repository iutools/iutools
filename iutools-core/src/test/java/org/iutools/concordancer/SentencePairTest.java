package org.iutools.concordancer;

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
		Pair<String, String> langs = alignment.langs();

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

		// If token alignment has been provided, then given expression in one
		// language, you can ask for the corresponding expression in the other language
		//
		//
		String enExpr = "world";
		String frExpr =
			alignment.otherLangText("en", enExpr);

		// You can also request that the expression and its equivalent in the
		// other language be marked up with some html-ish tags
		//
		// In the example below, the en expression and its iu equivalent will
		// be marked up with <equiv></equiv> tags.
		//
		String tagName = "equiv";
		Pair<String,String> markedUpPair = alignment.markupPair("en", enExpr, tagName);
		String enMarkedUp = markedUpPair.getLeft();
		String iuMarkedUp = markedUpPair.getRight();
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
			.otherLangTextIs("en", "hello", "Bonjour")
			.otherLangTextIs("en", "world", "le monde")
			;
	}

	@Test
	public void test__tokens4text__HappyPath() throws Exception {

		new AssertSentencePair(pair)
			.tokensForTextAre("en", "hello", 0)
			.tokensForTextAre("en", "world", 1)
			.tokensForTextAre("en", "hello world", 0, 1)
			.tokensForTextAre("fr", "bonjour", 0)
			.tokensForTextAre("fr", "le", 1)
			.tokensForTextAre("fr", "monde", 2)
			.tokensForTextAre("fr", "le monde", 1, 2)
			;
	}
}
