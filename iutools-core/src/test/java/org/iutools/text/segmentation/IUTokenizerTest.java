package org.iutools.text.segmentation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
		List<Pair<String,Boolean>> tokens = tokenizer.getAllTokens();
		for (Pair<String,Boolean> aToken: tokens) {
			String tokString = aToken.getLeft();
			if (aToken.getRight()) {
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
		Assert.assertEquals("", "\"", tokenizer.allTokensPunctuation.get(0).getLeft());
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
		List<Pair<String,Boolean>> expectedTokens;
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
		
		expectedTokens = new ArrayList<Pair<String,Boolean>>();
		expectedTokens.add(Pair.of("009",true));
		expectedTokens.add(Pair.of(" ",false));
		expectedTokens.add(Pair.of("-",false));
		expectedTokens.add(Pair.of(" ",false));
		expectedTokens.add(Pair.of("4",true));
		expectedTokens.add(Pair.of("(",false));
		expectedTokens.add(Pair.of("3",true));
		expectedTokens.add(Pair.of("):",false));
		expectedTokens.add(Pair.of(" ",false));
		expectedTokens.add(Pair.of("ᐃᖏᕐᕋᖃᑦᑕᕐᓂᕐᒧᑦ",true));
		expectedTokens.add(Pair.of(" ",false));
		expectedTokens.add(Pair.of("ᐳᓚᕋᖅᑐᓕᕆᓂᕐᒧᓪᓗ",true));
		expectedTokens.add(Pair.of(" ",false));
		expectedTokens.add(Pair.of("ᒪᓕᒐᐅᑉ",true));
		expectedTokens.add(Pair.of(" ",false));
		expectedTokens.add(Pair.of("ᓄᑖᖑᕆᐊᖅᑕᐅᓂᖓ",true));
		expectedTokens.add(Pair.of(" ",false));
		expectedTokens.add(Pair.of("(",false));
		expectedTokens.add(Pair.of("ᐃᐊᓪ",true));
		expectedTokens.add(Pair.of("-",false));
		expectedTokens.add(Pair.of("ᑲᓇᔪᖅ",true));
		expectedTokens.add(Pair.of(")",false));
		expectedTokens.add(Pair.of(" ",false));
		expectedTokens.add(Pair.of("159",true));
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
		List<Pair<String,Boolean>> allTokens = tokenizer.getAllTokens();
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
		List<Pair<String,Boolean>>expectedTokens = new ArrayList<Pair<String,Boolean>>();
		expectedTokens.add(Pair.of("2015−mit",true));
		expectedTokens.add(Pair.of(".",false));
		AssertObject.assertDeepEquals("", expectedTokens, tokenizer.getTokens());
	}
	
	@Test
	public void test_run__Case_percent() throws IOException {
		IUTokenizer tokenizer = new IUTokenizer();
		String text;
		text = "a 0.08%−mit c";
		tokenizer.tokenize(text);
		List<Pair<String,Boolean>>expectedTokens = new ArrayList<Pair<String,Boolean>>();
		expectedTokens.add(Pair.of("a",true));
		expectedTokens.add(Pair.of(" ",false));
		expectedTokens.add(Pair.of("0.08%−mit",true));
		expectedTokens.add(Pair.of(" ",false));
		expectedTokens.add(Pair.of("c",true));
		AssertObject.assertDeepEquals("", expectedTokens, tokenizer.getTokens());
	}
	
	@Test
	public void test_run__Case_quotes() throws IOException {
		IUTokenizer tokenizer = new IUTokenizer();
		String text;
		text = "he said \"bla bla\".";
		tokenizer.tokenize(text);
		List<Pair<String,Boolean>>expectedTokens = new ArrayList<Pair<String,Boolean>>();
		expectedTokens.add(Pair.of("he",true));
		expectedTokens.add(Pair.of(" ",false));
		expectedTokens.add(Pair.of("said",true));
		expectedTokens.add(Pair.of(" ",false));
		expectedTokens.add(Pair.of("\"",false));
		expectedTokens.add(Pair.of("bla",true));
		expectedTokens.add(Pair.of(" ",false));
		expectedTokens.add(Pair.of("bla",true));
		expectedTokens.add(Pair.of("\".",false));
		AssertObject.assertDeepEquals("", expectedTokens, tokenizer.getTokens());
	}
	
	@Test
	public void test_run__Case_numbered_list() throws IOException {
		IUTokenizer tokenizer = new IUTokenizer();
		String text;
		text = "he said 1. ok 2. fine ... 10. no.";
		tokenizer.tokenize(text);
		List<Pair<String,Boolean>>expectedTokens = new ArrayList<Pair<String,Boolean>>();
		expectedTokens.add(Pair.of("he",true));
		expectedTokens.add(Pair.of(" ",false));
		expectedTokens.add(Pair.of("said",true));
		expectedTokens.add(Pair.of(" ",false));
		expectedTokens.add(Pair.of("1",true));
		expectedTokens.add(Pair.of(".",false));
		expectedTokens.add(Pair.of(" ",false));
		expectedTokens.add(Pair.of("ok",true));
		expectedTokens.add(Pair.of(" ",false));
		expectedTokens.add(Pair.of("2",true));
		expectedTokens.add(Pair.of(".",false));
		expectedTokens.add(Pair.of(" ",false));
		expectedTokens.add(Pair.of("fine",true));
		expectedTokens.add(Pair.of(" ",false));
		expectedTokens.add(Pair.of("...",false));
		expectedTokens.add(Pair.of(" ",false));
		expectedTokens.add(Pair.of("10",true));
		expectedTokens.add(Pair.of(".",false));
		expectedTokens.add(Pair.of(" ",false));
		expectedTokens.add(Pair.of("no",true));
		expectedTokens.add(Pair.of(".",false));
		AssertObject.assertDeepEquals("", expectedTokens, tokenizer.getTokens());
	}
	
	@Test
	public void test__tokenize__URL() throws IOException {
		IUTokenizer tokenizer = new IUTokenizer();
		String text = "http://www.somewhere.com/path-with-lots-of-hyphens/";
		tokenizer.tokenize(text);
		List<Pair<String, Boolean>> gotTokens = tokenizer.getAllTokens();
		Pair<String,Boolean>[] expTokens = new Pair[] {
			Pair.of("http", true), Pair.of("://", false), Pair.of("www", true), 
			Pair.of(".", false), Pair.of("somewhere", true), Pair.of(".", false),
			Pair.of("com", true), Pair.of("/", false),  
			Pair.of("path", true), Pair.of("-", false),
			Pair.of("with", true), Pair.of("-", false),
			Pair.of("lots", true), Pair.of("-", false),
			Pair.of("of", true), Pair.of("-", false),
			Pair.of("hyphens", true), Pair.of("/", false),
			
		};
		AssertObject.assertDeepEquals("", expTokens, gotTokens);
	}	
}
