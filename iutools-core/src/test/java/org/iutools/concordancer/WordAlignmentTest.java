package org.iutools.concordancer;

import org.junit.jupiter.api.Test;

public class WordAlignmentTest {

	///////////////////////////////////////
	// DOCUMENTATION TESTS
	///////////////////////////////////////

	@Test
	public void test__WordAlignment__Synopsis() {
		// Say you have aligned sentences in English and French
		//
		//   en sentence: "Hello world!";
		//   fr sentence: "Bonjour le monde!";
		//
		// Assume that the two sentences have been tokenized and the results of
		// the tokenization is a string with a single space separating the tokens
		//
		String[] enTokens = new String[] {"Hello", "world", "!"};
		String[] frTokens = new String[] {"Bonjour", "le", "monde", "!"};

		// Assume the tokens have been aligned
		String[] tokenMatches = new String[] {
			"0-0", "1-1", "1-2", "2-3"
		};

		// You can create a WordAlignment as follows
		WordAlignment alignment =
			new WordAlignment("en", enTokens, "fr", frTokens, tokenMatches);
	}
}