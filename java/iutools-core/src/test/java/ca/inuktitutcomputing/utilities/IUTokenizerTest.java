package ca.inuktitutcomputing.utilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ca.nrc.datastructure.Pair;
import ca.nrc.testing.AssertHelpers;

public class IUTokenizerTest {
	
	@Before
	public void setUp() {
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
		Assert.assertEquals("", "\"", tokenizer.allTokensPunctuation.get(0).getFirst());
	}

	@Test
	public void test_run() throws IOException {
		IUTokenizer tokenizer = new IUTokenizer();
		String text;
		text= "(ᑲᖏᖅᖠᓂᐅᑉ ᐅᐊᓐᓇᖓ-ᐃᒡᓗᓕᒑᕐᔪᒃ)";
		List<String> words = tokenizer.run(text);
		List<String> expectedWords = new ArrayList<String>();
		expectedWords.add("ᑲᖏᖅᖠᓂᐅᑉ");
		expectedWords.add("ᐅᐊᓐᓇᖓ");
		expectedWords.add("ᐃᒡᓗᓕᒑᕐᔪᒃ");
		AssertHelpers.assertDeepEquals("", expectedWords, words);
		
		text = "ᐅᖃᖅᑎ: ᒥᔅᑕ ᔫ ᐃᓄᒃ, ᒪᓕᒐᓕᐅᖅᑎ ᓄᓇᕗᑦ ᒪᓕᒐᓕᐅᕐᕕᖓ";
		words = tokenizer.run(text);
		expectedWords = new ArrayList<String>();
		expectedWords.add("ᐅᖃᖅᑎ");
		expectedWords.add("ᒥᔅᑕ");
		expectedWords.add("ᔫ");
		expectedWords.add("ᐃᓄᒃ");
		expectedWords.add("ᒪᓕᒐᓕᐅᖅᑎ");
		expectedWords.add("ᓄᓇᕗᑦ");
		expectedWords.add("ᒪᓕᒐᓕᐅᕐᕕᖓ");
		AssertHelpers.assertDeepEquals("", expectedWords, words);
	}
	
	@Test
	public void test_run__Cas_1() throws IOException {
		IUTokenizer tokenizer = new IUTokenizer();
		String text;
		List<String> words, expectedWords;
		List<Pair<String,Boolean>> expectedTokens;
		text = "009 - 4(3): ᐃᖏᕐᕋᖃᑦᑕᕐᓂᕐᒧᑦ ᐳᓚᕋᖅᑐᓕᕆᓂᕐᒧᓪᓗ ᒪᓕᒐᐅᑉ ᓄᑖᖑᕆᐊᖅᑕᐅᓂᖓ (ᐃᐊᓪ-ᑲᓇᔪᖅ) 159";
		words = tokenizer.run(text);
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
		AssertHelpers.assertDeepEquals("", expectedWords, words);
		
		expectedTokens = new ArrayList<Pair<String,Boolean>>();
		expectedTokens.add(new Pair<>("009",true));
		expectedTokens.add(new Pair<>(" ",false));
		expectedTokens.add(new Pair<>("-",false));
		expectedTokens.add(new Pair<>(" ",false));
		expectedTokens.add(new Pair<>("4",true));
		expectedTokens.add(new Pair<>("(",false));
		expectedTokens.add(new Pair<>("3",true));
		expectedTokens.add(new Pair<>("):",false));
		expectedTokens.add(new Pair<>(" ",false));
		expectedTokens.add(new Pair<>("ᐃᖏᕐᕋᖃᑦᑕᕐᓂᕐᒧᑦ",true));
		expectedTokens.add(new Pair<>(" ",false));
		expectedTokens.add(new Pair<>("ᐳᓚᕋᖅᑐᓕᕆᓂᕐᒧᓪᓗ",true));
		expectedTokens.add(new Pair<>(" ",false));
		expectedTokens.add(new Pair<>("ᒪᓕᒐᐅᑉ",true));
		expectedTokens.add(new Pair<>(" ",false));
		expectedTokens.add(new Pair<>("ᓄᑖᖑᕆᐊᖅᑕᐅᓂᖓ",true));
		expectedTokens.add(new Pair<>(" ",false));
		expectedTokens.add(new Pair<>("(",false));
		expectedTokens.add(new Pair<>("ᐃᐊᓪ",true));
		expectedTokens.add(new Pair<>("-",false));
		expectedTokens.add(new Pair<>("ᑲᓇᔪᖅ",true));
		expectedTokens.add(new Pair<>(")",false));
		expectedTokens.add(new Pair<>(" ",false));
		expectedTokens.add(new Pair<>("159",true));
		AssertHelpers.assertDeepEquals("", expectedTokens, tokenizer.getTokens());
		
	}

	
	@Test
	public void test_run__Cas_2() throws IOException {
		IUTokenizer tokenizer = new IUTokenizer();
		String text;
		List<String> words, expectedWords;
		text = "044 - 4(3): ᕿᑭᖅᑖᓗᒻᒥ ᑐᑦᑐᓕᕆᓂᖅ (Hᐃᒃᔅ) 179";
		words = tokenizer.run(text);
		expectedWords = new ArrayList<String>();
		expectedWords.add("044");
		expectedWords.add("4");
		expectedWords.add("3");
		expectedWords.add("ᕿᑭᖅᑖᓗᒻᒥ");
		expectedWords.add("ᑐᑦᑐᓕᕆᓂᖅ");
		expectedWords.add("Hᐃᒃᔅ");
		expectedWords.add("179");
		AssertHelpers.assertDeepEquals("", expectedWords, words);
	}


	
	@Test
	public void test_run__Cas_3() throws IOException {
		IUTokenizer tokenizer = new IUTokenizer();
		String text;
		List<String> words, expectedWords;
		text = "ᑖᓐᓇ G.R.E.A.T. ᐱᓕᕆᐊᖅ";
		words = tokenizer.run(text);
		expectedWords = new ArrayList<String>();
		expectedWords.add("ᑖᓐᓇ");
		expectedWords.add("G.R.E.A.T.");
		expectedWords.add("ᐱᓕᕆᐊᖅ");
		AssertHelpers.assertDeepEquals("", expectedWords, words);
		
		text = "ᐅᖃᖅᑎ: ᖁᔭᓐᓇᒦᒃ. ᒥᓂᔅᑕᐃ ᐅᖃᐅᓯᒃᓴᖏᑦ. ᒥᓂᔅᑐ ᒪᐃᒃ.";
		words = tokenizer.run(text);
		expectedWords = new ArrayList<String>();
		expectedWords.add("ᐅᖃᖅᑎ");
		expectedWords.add("ᖁᔭᓐᓇᒦᒃ");
		expectedWords.add("ᒥᓂᔅᑕᐃ");
		expectedWords.add("ᐅᖃᐅᓯᒃᓴᖏᑦ");
		expectedWords.add("ᒥᓂᔅᑐ");
		expectedWords.add("ᒪᐃᒃ");
		AssertHelpers.assertDeepEquals("", expectedWords, words);
	}

	@Test
	public void test_reconstruct() {
		IUTokenizer tokenizer = new IUTokenizer();
		String text;
		text = "044 - 4(3): ᕿᑭᖅᑖᓗᒻᒥ ᑐᑦᑐᓕᕆᓂᖅ (Hᐃᒃᔅ) 179";
		tokenizer.run(text);
		String reconstructedText = tokenizer.reconstruct();
		Assert.assertEquals("",text,reconstructedText);
	}
	
	@Test
	public void test_run__Case_ampersand() {
		IUTokenizer tokenizer = new IUTokenizer();
		String text;
		text = "sinik&uni";
		tokenizer.run(text);
		List<Pair<String,Boolean>> allTokens = tokenizer.getAllTokens();
		Assert.assertEquals("",1,allTokens.size());
	}
	@Test
	public void test_run__Case_tiret() throws IOException {
		IUTokenizer tokenizer = new IUTokenizer();
		String text;
		text = "2015−mit";
		List<String>words = tokenizer.run(text);
		List<String>expectedWords = new ArrayList<String>();
		expectedWords.add("2015−mit");
		AssertHelpers.assertDeepEquals("", expectedWords, words);
	}
	
	@Test
	public void test_run__Case_tiret_period() throws IOException {
		IUTokenizer tokenizer = new IUTokenizer();
		String text;
		text = "2015−mit.";
		tokenizer.run(text);
		List<Pair<String,Boolean>>expectedTokens = new ArrayList<Pair<String,Boolean>>();
		expectedTokens.add(new Pair<>("2015−mit",true));
		expectedTokens.add(new Pair<>(".",false));
		AssertHelpers.assertDeepEquals("", expectedTokens, tokenizer.getTokens());
	}
	
	@Test
	public void test_run__Case_quotes() throws IOException {
		IUTokenizer tokenizer = new IUTokenizer();
		String text;
		text = "he said \"bla bla\".";
		tokenizer.run(text);
		List<Pair<String,Boolean>>expectedTokens = new ArrayList<Pair<String,Boolean>>();
		expectedTokens.add(new Pair<>("he",true));
		expectedTokens.add(new Pair<>(" ",false));
		expectedTokens.add(new Pair<>("said",true));
		expectedTokens.add(new Pair<>(" ",false));
		expectedTokens.add(new Pair<>("\"",false));
		expectedTokens.add(new Pair<>("bla",true));
		expectedTokens.add(new Pair<>(" ",false));
		expectedTokens.add(new Pair<>("bla",true));
		expectedTokens.add(new Pair<>("\".",false));
		AssertHelpers.assertDeepEquals("", expectedTokens, tokenizer.getTokens());
	}
	
	@Test
	public void test_run__Case_numbered_list() throws IOException {
		IUTokenizer tokenizer = new IUTokenizer();
		String text;
		text = "he said 1. ok 2. fine ... 10. no.";
		tokenizer.run(text);
		List<Pair<String,Boolean>>expectedTokens = new ArrayList<Pair<String,Boolean>>();
		expectedTokens.add(new Pair<>("he",true));
		expectedTokens.add(new Pair<>(" ",false));
		expectedTokens.add(new Pair<>("said",true));
		expectedTokens.add(new Pair<>(" ",false));
		expectedTokens.add(new Pair<>("1",true));
		expectedTokens.add(new Pair<>(".",false));
		expectedTokens.add(new Pair<>(" ",false));
		expectedTokens.add(new Pair<>("ok",true));
		expectedTokens.add(new Pair<>(" ",false));
		expectedTokens.add(new Pair<>("2",true));
		expectedTokens.add(new Pair<>(".",false));
		expectedTokens.add(new Pair<>(" ",false));
		expectedTokens.add(new Pair<>("fine",true));
		expectedTokens.add(new Pair<>(" ",false));
		expectedTokens.add(new Pair<>("...",false));
		expectedTokens.add(new Pair<>(" ",false));
		expectedTokens.add(new Pair<>("10",true));
		expectedTokens.add(new Pair<>(".",false));
		expectedTokens.add(new Pair<>(" ",false));
		expectedTokens.add(new Pair<>("no",true));
		expectedTokens.add(new Pair<>(".",false));
		AssertHelpers.assertDeepEquals("", expectedTokens, tokenizer.getTokens());
	}
	
}
