package org.iutools.text.segmentation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import ca.nrc.testing.RunOnCases;
import static ca.nrc.testing.RunOnCases.Case;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ca.nrc.testing.AssertObject;

public class IUTokenizerTest {
	
	@Before
	public void setUp() {
	}
	
	/////////////////////////////
	// DOCUMENTATION TESTS
	/////////////////////////////

	@Test
	public void test__IUTokenizer__Synopsis() {
		// Use this class to tokenize text written in Inuktut
		IUTokenizer tokenizer = new IUTokenizer();
		String text = "(ᑲᖏᖅᖠᓂᐅᑉ ᐅᐊᓐᓇᖓ-ᐃᒡᓗᓕᒑᕐᔪᒃ)";
		
		// This only returns tokens that are actual words
		List<String> words = tokenizer.tokenize(text);
		
		// If you want to have all tokens, including spaces, punctuation 
		// etc.., you then have to invoke this method
		//
		List<String> everything = tokenizer.wordsAndAll();
		
		// You can also get a list of tokens, with an indicator that 
		// says if it was a word or not
		//
		List<Token> tokens = tokenizer.getAllTokens();
		for (Token aToken: tokens) {
			String tokString = aToken.text;
			if (aToken.isWord) {
				// Token is an actual word
			} else {
				// Token is NOT a word
			}
		}
	}
	
	/////////////////////////////
	// VERIFICATION TESTS
	/////////////////////////////
	
	@Test
	public void test__tokenize__VariousCases() throws Exception {
		TokenizationCase[] cases = new TokenizationCase[] {
			new TokenizationCase("Roman - just spaces", "iglu inuksuk nunavut",
				"iglu", "inuksuk", "nunavut")
				.setAllTokens(
					Pair.of("iglu", true), Pair.of(" ", false),
					Pair.of("inuksuk", true), Pair.of(" ", false),
					Pair.of("nunavut", true)),

			new TokenizationCase("Roman - with punctuation", "iglu: inuksuk, nunavut",
				"iglu", "inuksuk", "nunavut")
				.setAllTokens(
					Pair.of("iglu", true), Pair.of(":", false), Pair.of(" ", false),
					Pair.of("inuksuk", true), Pair.of(",", false), Pair.of(" ", false),
					Pair.of("nunavut", true)),

			// Some words (in particular "sounds" like "ouch") contain apostrophes.
			// For example, "chicken" is written as "a'a'aak"
			new TokenizationCase("Roman - Word with apostrophes (=chicken)", "a'a'aak inuksuk",
				"a'a'aak", "inuksuk"),

			new TokenizationCase("Numbered list", "iglu 1. inuksuk 2. nunavut ... 10. inuktitut.",
			"iglu", "1", "inuksuk", "2", "nunavut", "10", "inuktitut"),

			new TokenizationCase("Syll - just spaces", "ᐃᒡᓗ ᐃᓄᒃᓱᒃ ᓄᓇᕗᑦ",
				"ᐃᒡᓗ", "ᐃᓄᒃᓱᒃ", "ᓄᓇᕗᑦ"),

			new TokenizationCase("Syll - with punctuation", "ᐃᒡᓗ: ᐃᓄᒃᓱᒃ, ᓄᓇᕗᑦ",
				"ᐃᒡᓗ", "ᐃᓄᒃᓱᒃ", "ᓄᓇᕗᑦ"),

			// Some words (in particular "sounds" like "ouch") contain apostrophes.
			// For example, "chicken" is written as "a'a'aak"
			new TokenizationCase("Syll - Word with apostrophes (=chicken)", "ᐊ'ᐊ'ᐋᒃ ᐃᓄᒃᓱᒃ",
				"ᐊ'ᐊ'ᐋᒃ", "ᐃᓄᒃᓱᒃ"),
		};

		Consumer<Case> runner =
			(uncastCase) ->
			{
				try {
					TokenizationCase aCase = (TokenizationCase)uncastCase;
					String text = aCase.text;
					List<String> expWords = aCase.expWords;
					List<Token> expTokens = aCase.expTokens;
					IUTokenizer tokenizer = new IUTokenizer();
					List<String> gotWords = tokenizer.tokenize(text);
					AssertObject.assertDeepEquals(
						"Words not as expected for text '"+text+"'",
						expWords, gotWords
					);
					if (expTokens != null) {
						List<Token> gotTokens = tokenizer.getAllTokens();
						AssertObject.assertDeepEquals(
							"Tokens not as expected for text '"+text+"'",
							expTokens, gotTokens
						);

					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			};

		new RunOnCases(cases, runner)
//			.onlyCaseNums(4)
//			.onlyCasesWithDescr("en-SEARCH-housing")
			.run();

		return;

	}
	
	@Test
	public void test____processInitialPunctuation() {
		IUTokenizer tokenizer = new IUTokenizer();
		String remainingToken = tokenizer.__processInitialPunctuation("all words");
		Assert.assertEquals("", "all words", remainingToken);
		Assert.assertTrue("",tokenizer.tokens.size()==0);
		Assert.assertTrue("",tokenizer.allTokensPunctuation.size()==0);
		
		tokenizer = new IUTokenizer();
		remainingToken = tokenizer.__processInitialPunctuation("\"all words");
		Assert.assertEquals("", "all words", remainingToken);
		Assert.assertTrue("",tokenizer.tokens.size()==1);
		Assert.assertEquals("", "\"", tokenizer.tokens.get(0));
		Assert.assertTrue("",tokenizer.allTokensPunctuation.size()==1);
		Assert.assertEquals("", "\"", tokenizer.allTokensPunctuation.get(0).text);
	}

	@Test
	public void test__tokenize__HappyPath() throws IOException {
		IUTokenizer tokenizer = new IUTokenizer();
		String text;
		text= "(ᑲᖏᖅᖠᓂᐅᑉ ᐅᐊᓐᓇᖓ-ᐃᒡᓗᓕᒑᕐᔪᒃ)";
		List<String> words = tokenizer.tokenize(text);
		List<String> expectedWords = new ArrayList<String>();
		expectedWords.add("ᑲᖏᖅᖠᓂᐅᑉ");
		expectedWords.add("ᐅᐊᓐᓇᖓ");
		expectedWords.add("ᐃᒡᓗᓕᒑᕐᔪᒃ");
		AssertObject.assertDeepEquals("", expectedWords, words);
		
		text = "ᐅᖃᖅᑎ: ᒥᔅᑕ ᔫ ᐃᓄᒃ, ᒪᓕᒐᓕᐅᖅᑎ ᓄᓇᕗᑦ ᒪᓕᒐᓕᐅᕐᕕᖓ";
		words = tokenizer.tokenize(text);
		expectedWords = new ArrayList<String>();
		expectedWords.add("ᐅᖃᖅᑎ");
		expectedWords.add("ᒥᔅᑕ");
		expectedWords.add("ᔫ");
		expectedWords.add("ᐃᓄᒃ");
		expectedWords.add("ᒪᓕᒐᓕᐅᖅᑎ");
		expectedWords.add("ᓄᓇᕗᑦ");
		expectedWords.add("ᒪᓕᒐᓕᐅᕐᕕᖓ");
		AssertObject.assertDeepEquals("", expectedWords, words);
	}

	@Test
	public void test__tokenize__WordWhoseTranslietrationContainsADoubleAmpersand() throws IOException {
		IUTokenizer tokenizer = new IUTokenizer();
		String text = "arviarmii&&utik";
		List<String> gotWords = tokenizer.tokenize(text);
		String[] expWords = new String[] {"arviarmii&&utik"};
		AssertObject.assertDeepEquals("",
			expWords, gotWords);
	}


	@Test
	public void test_run__Cas_1() throws IOException {
		IUTokenizer tokenizer = new IUTokenizer();
		String text;
		List<String> words, expectedWords;
		List<Token> expectedTokens;
		text = "009 - 4(3): ᐃᖏᕐᕋᖃᑦᑕᕐᓂᕐᒧᑦ ᐳᓚᕋᖅᑐᓕᕆᓂᕐᒧᓪᓗ ᒪᓕᒐᐅᑉ ᓄᑖᖑᕆᐊᖅᑕᐅᓂᖓ (ᐃᐊᓪ-ᑲᓇᔪᖅ) 159";
		words = tokenizer.tokenize(text);
		expectedWords = new ArrayList<String>();
		expectedWords.add("009");
		expectedWords.add("4");
		expectedWords.add("3");
		expectedWords.add("ᐃᖏᕐᕋᖃᑦᑕᕐᓂᕐᒧᑦ");
		expectedWords.add("ᐳᓚᕋᖅᑐᓕᕆᓂᕐᒧᓪᓗ");
		expectedWords.add("ᒪᓕᒐᐅᑉ");
		expectedWords.add("ᓄᑖᖑᕆᐊᖅᑕᐅᓂᖓ");
		expectedWords.add("ᐃᐊᓪ");
		expectedWords.add("ᑲᓇᔪᖅ");
		expectedWords.add("159");
		AssertObject.assertDeepEquals("", expectedWords, words);
		
		expectedTokens = new ArrayList<Token>();
		expectedTokens.add(new Token("009",true));
		expectedTokens.add(new Token(" ",false));
		expectedTokens.add(new Token("-",false));
		expectedTokens.add(new Token(" ",false));
		expectedTokens.add(new Token("4",true));
		expectedTokens.add(new Token("(",false));
		expectedTokens.add(new Token("3",true));
		expectedTokens.add(new Token("):",false));
		expectedTokens.add(new Token(" ",false));
		expectedTokens.add(new Token("ᐃᖏᕐᕋᖃᑦᑕᕐᓂᕐᒧᑦ",true));
		expectedTokens.add(new Token(" ",false));
		expectedTokens.add(new Token("ᐳᓚᕋᖅᑐᓕᕆᓂᕐᒧᓪᓗ",true));
		expectedTokens.add(new Token(" ",false));
		expectedTokens.add(new Token("ᒪᓕᒐᐅᑉ",true));
		expectedTokens.add(new Token(" ",false));
		expectedTokens.add(new Token("ᓄᑖᖑᕆᐊᖅᑕᐅᓂᖓ",true));
		expectedTokens.add(new Token(" ",false));
		expectedTokens.add(new Token("(",false));
		expectedTokens.add(new Token("ᐃᐊᓪ",true));
		expectedTokens.add(new Token("-",false));
		expectedTokens.add(new Token("ᑲᓇᔪᖅ",true));
		expectedTokens.add(new Token(")",false));
		expectedTokens.add(new Token(" ",false));
		expectedTokens.add(new Token("159",true));
		AssertObject.assertDeepEquals("", expectedTokens, tokenizer.getTokens());
	}

	
	@Test
	public void test_run__Cas_2() throws IOException {
		IUTokenizer tokenizer = new IUTokenizer();
		String text;
		List<String> words, expectedWords;
		text = "044 - 4(3): ᕿᑭᖅᑖᓗᒻᒥ ᑐᑦᑐᓕᕆᓂᖅ (Hᐃᒃᔅ) 179";
		words = tokenizer.tokenize(text);
		expectedWords = new ArrayList<String>();
		expectedWords.add("044");
		expectedWords.add("4");
		expectedWords.add("3");
		expectedWords.add("ᕿᑭᖅᑖᓗᒻᒥ");
		expectedWords.add("ᑐᑦᑐᓕᕆᓂᖅ");
		expectedWords.add("Hᐃᒃᔅ");
		expectedWords.add("179");
		AssertObject.assertDeepEquals("", expectedWords, words);
	}


	
	@Test
	public void test_run__Cas_3() throws IOException {
		IUTokenizer tokenizer = new IUTokenizer();
		String text;
		List<String> words, expectedWords;
		text = "ᑖᓐᓇ G.R.E.A.T. ᐱᓕᕆᐊᖅ";
		words = tokenizer.tokenize(text);
		expectedWords = new ArrayList<String>();
		expectedWords.add("ᑖᓐᓇ");
		expectedWords.add("G.R.E.A.T.");
		expectedWords.add("ᐱᓕᕆᐊᖅ");
		AssertObject.assertDeepEquals("", expectedWords, words);
		
		text = "ᐅᖃᖅᑎ: ᖁᔭᓐᓇᒦᒃ. ᒥᓂᔅᑕᐃ ᐅᖃᐅᓯᒃᓴᖏᑦ. ᒥᓂᔅᑐ ᒪᐃᒃ.";
		words = tokenizer.tokenize(text);
		expectedWords = new ArrayList<String>();
		expectedWords.add("ᐅᖃᖅᑎ");
		expectedWords.add("ᖁᔭᓐᓇᒦᒃ");
		expectedWords.add("ᒥᓂᔅᑕᐃ");
		expectedWords.add("ᐅᖃᐅᓯᒃᓴᖏᑦ");
		expectedWords.add("ᒥᓂᔅᑐ");
		expectedWords.add("ᒪᐃᒃ");
		AssertObject.assertDeepEquals("", expectedWords, words);
	}

	@Test
	public void test_reconstruct() {
		IUTokenizer tokenizer = new IUTokenizer();
		String text;
		text = "044 - 4(3): ᕿᑭᖅᑖᓗᒻᒥ ᑐᑦᑐᓕᕆᓂᖅ (Hᐃᒃᔅ) 179";
		tokenizer.tokenize(text);
		String reconstructedText = tokenizer.reconstruct();
		Assert.assertEquals("",text,reconstructedText);
	}
	
	@Test
	public void test_run__Case_ampersand() {
		IUTokenizer tokenizer = new IUTokenizer();
		String text;
		text = "sinik&uni";
		tokenizer.tokenize(text);
		List<Token> allTokens = tokenizer.getAllTokens();
		Assert.assertEquals("",1,allTokens.size());
	}
	@Test
	public void test_run__Case_tiret() throws IOException {
		IUTokenizer tokenizer = new IUTokenizer();
		String text;
		text = "2015−mit";
		List<String>words = tokenizer.tokenize(text);
		List<String>expectedWords = new ArrayList<String>();
		expectedWords.add("2015−mit");
		AssertObject.assertDeepEquals("", expectedWords, words);
	}
	
	@Test
	public void test_run__Case_tiret_period() throws IOException {
		IUTokenizer tokenizer = new IUTokenizer();
		String text;
		text = "2015−mit.";
		tokenizer.tokenize(text);
		Token[] expectedTokens = new Token[]{
			new Token("2015−mit", true),
			new Token(".", false)
		};
		AssertObject.assertDeepEquals("", expectedTokens, tokenizer.getTokens());
	}
	
	@Test
	public void test_run__Case_percent() throws IOException {
		IUTokenizer tokenizer = new IUTokenizer();
		String text;
		text = "a 0.08%−mit c";
		tokenizer.tokenize(text);
		Token[] expectedTokens = new Token[]{
			new Token("a", true),
			new Token(" ", false),
			new Token("0.08%−mit", true),
			new Token(" ", false),
			new Token("c", true),
		};
		AssertObject.assertDeepEquals("", expectedTokens, tokenizer.getTokens());
	}
	
	@Test
	public void test_run__Case_quotes() throws IOException {
		IUTokenizer tokenizer = new IUTokenizer();
		String text;
		text = "he said \"bla bla\".";
		tokenizer.tokenize(text);
		Token[] expectedTokens = new Token[]{
		new Token("he", true),
		new Token(" ", false),
		new Token("said", true),
		new Token(" ", false),
		new Token("\"", false),
		new Token("bla", true),
		new Token(" ", false),
		new Token("bla", true),
		new Token("\".", false),
		};
		AssertObject.assertDeepEquals("", expectedTokens, tokenizer.getTokens());
	}

	@Test
	public void test__tokenize__URL() throws IOException {
		IUTokenizer tokenizer = new IUTokenizer();
		String text = "http://www.somewhere.com/path-with-lots-of-hyphens/";
		tokenizer.tokenize(text);
		List<Token> gotTokens = tokenizer.getAllTokens();
		Token[] expTokens = new Token[] {
			new Token("http", true), new Token("://", false), new Token("www", true),
			new Token(".", false), new Token("somewhere", true), new Token(".", false),
			new Token("com", true), new Token("/", false),
			new Token("path", true), new Token("-", false),
			new Token("with", true), new Token("-", false),
			new Token("lots", true), new Token("-", false),
			new Token("of", true), new Token("-", false),
			new Token("hyphens", true), new Token("/", false),
			
		};
		AssertObject.assertDeepEquals("", expTokens, gotTokens);
	}

	//////////////////////////////////////
	// Test Helpers
	//////////////////////////////////////

	public static class TokenizationCase extends Case {

		public String text = null;
		public List<Token> expTokens = null;
		public List<String> expWords = null;

		public TokenizationCase(String _descr, String _text, String... _expWords) {
			super(_descr, null);
			text = _text;
			expWords = new ArrayList<String>();
			Collections.addAll(expWords, _expWords);
		}

		public TokenizationCase setAllTokens(Pair<String,Boolean>... tokens) {
			expTokens = new ArrayList<Token>();
			for (Pair<String,Boolean> token: tokens) {
				expTokens.add(new Token(token.getLeft(), token.getRight()));
			}
			return this;
		}
	}
}
