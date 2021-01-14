package org.iutools.concordancer;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

public class AlignmentTest {

	//////////////////////////////////////
	// DOCUMENTATION TEST
	//////////////////////////////////////

	@Test
	public void test__Alignment__Synopsis() {
		// Use Alignment objects to represent a pair of aligned sentences
		// in different languages
		//
		// For example:
		//
		Alignment alignment = new Alignment(
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
				new String[] {"Hello", "world"},
				new String[] {"Bonjour", "le", "monde"},
				new int[][] {
					// 1st en token aligns with 1st fr token
					new int[]{0,0},
					// 2nd en token aligns with 2nd and 3rd fr tokens
					new int[]{1,1}, new int[]{1,2}});

		// If token alignment has been provided, then given a sequence of tokens
		// in one language, you can ask for the corresponding sequence of tokens
		// in the other language
		//
		// For example, this asks for aligning the 2nd en token ("world").
		//
		Pair<String[], String[]> alignedTokens =
			alignment.getCorrespondingTokens("en", 1);
		String[] enTokens = alignedTokens.getLeft();
		String[] frTokens = alignedTokens.getRight();

		// This asks for aligning the 2nd and 3rd fr tokens
		alignedTokens = alignment.getCorrespondingTokens("fr", 1, 2);

		// Instead of asking for aligned tokens, you can ask for aligned
		// token offsets
		//
		Pair<Pair<Integer,Integer>,Pair<Integer,Integer>> alignedOffsets =
			alignment.getCorrespondingTokenOffsets("en", 1);
		Pair<Integer,Integer> enStartEnd = alignedOffsets.getLeft();
		Pair<Integer,Integer> frStartEnd = alignedOffsets.getRight();
	}

	/////////////////////////////
	// VERIFICATION TESTS
	/////////////////////////////

	@Test
	public void test__Alignment__HappyPath() throws Exception {
		Alignment alignment = new Alignment(
			"en", "Hello world",
			"fr", "Bonjour le monde"
		);


		// You can also provide token-level alignment information
		//
		alignment
			.setTokens("en", "Hello", "world")
			.setTokens("fr", "Bonjour", "le", "monde")
			.setTokenAlignments(
				new String[] {"Hello", "world"},
				new String[] {"Bonjour", "le", "monde"},
				new int[][] {
					new int[]{0,0},
					new int[]{1,1}, new int[]{1,2}});
			;

		new AssertAlignment(alignment)
			.langsAre("en", "fr")
			.textForLangIs("en", "Hello world")
			.textForLangIs("fr", "Bonjour le monde")
			.tokensForLangAre("en", "Hello", "world")
			.tokensForLangAre("fr", "Bonjour", "le", "monde")
//			.correspondingTokensAre("en", 0, 0, 0, 0)
//			.correspondingTokensAre("en", 1, 1, 1, 2)
//			.correspondingTokensAre("fr", 0, 0, 0, 0)
//			.correspondingTokensAre("fr", 1, 2, 1, 1)
			;

	}
}
