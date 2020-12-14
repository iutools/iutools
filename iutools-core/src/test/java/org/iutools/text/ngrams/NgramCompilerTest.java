package org.iutools.text.ngrams;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import ca.nrc.testing.AssertObject;

public class NgramCompilerTest {
	
	@Test
	public void test__compile__StringInput__NoExtremities() throws Exception {
		Set<String> gotNgrams = new NgramCompiler(4).compile("hello");
		String[] expNgrams = new String[] {"hell", "ello", "hello"};
		assertNgramsEqual("", expNgrams, gotNgrams);
	}

	@Test
	public void test__compile__ArrayInput__NoExtremities() throws Exception {
		String[] chars = "hello".split("");
		Set<String[]> gotNgrams = new NgramCompiler(4).compile(chars);
		String[][] expNgrams = new String[][] {
			"hell".split(""), "ello".split(""), "hello".split("")
		};
		assertNgramsEqual("", expNgrams, gotNgrams);
	}

	@Test
	public void test__compile__StringInput__WithExtremities() throws Exception {
		Set<String> gotNgrams = new NgramCompiler(4, true).compile("hello");
		String[] expNgrams = new String[] {
			"^hell", "hell", "ello", "ello$", "hello", "^hello", "hello$", 
			"hello", "^hello$"};
		assertNgramsEqual("", expNgrams, gotNgrams);
	}

	@Test
	public void test__compile__ArrayInput__WithExtremities() throws Exception {
		String[] chars = "hello".split("");
		Set<String[]> gotNgrams = new NgramCompiler(4, true).compile(chars);
		String[][] expNgrams = new String[][] {
			"^hell".split(""), "hell".split(""), "ello".split(""), 
			"ello$".split(""), "hello".split(""), "^hello".split(""), 
			"hello$".split(""), "hello".split(""), "^hello$".split("")
		};
		assertNgramsEqual("", expNgrams, gotNgrams);
	}

	@Test
	public void test__atBeginningOfString__HappyPath() throws Exception {
		String[] ngram = new String[] {"h", "e", "l", "l", "o"};
		String[] gotNgram = NgramCompiler.atBeginningOfString(ngram);
		String[] expNgram = new String[] {"^", "h", "e", "l", "l", "o"};
		AssertObject.assertDeepEquals("", expNgram, gotNgram);
	}

	@Test
	public void test__atBeginningOfString__NgramAlreadyForBeginning__LeavesItAlone() throws Exception {
		String[] ngram = new String[] {"^", "h", "e", "l", "l", "o"};
		String[] gotNgram = NgramCompiler.atBeginningOfString(ngram);
		String[] expNgram = new String[] {"^", "h", "e", "l", "l", "o"};
		AssertObject.assertDeepEquals("", expNgram, gotNgram);
	}

	@Test
	public void test__atBeginningOfString__NullInput__ReturnsNull() throws Exception {
		String[] ngram = null;
		String[] gotNgram = NgramCompiler.atBeginningOfString(ngram);
		String[] expNgram = null;
		AssertObject.assertDeepEquals("", expNgram, gotNgram);
	}

	@Test
	public void test__atBeginningOfString__EmptyInput__PrependsBeginningChar() throws Exception {
		String[] ngram = new String[0];
		String[] gotNgram = NgramCompiler.atBeginningOfString(ngram);
		String[] expNgram = new String[] {"^"};
		AssertObject.assertDeepEquals("", expNgram, gotNgram);
	}

	@Test
	public void test__atEndOfString__HappyPath() throws Exception {
		String[] ngram = new String[] {"h", "e", "l", "l", "o"};
		String[] gotNgram = NgramCompiler.atEndOfString(ngram);
		String[] expNgram = new String[] {"h", "e", "l", "l", "o", "$"};
		AssertObject.assertDeepEquals("", expNgram, gotNgram);
	}
	
	@Test
	public void test__atEndOfString__NgramAlreadyForEnd__LeavesItAlone() throws Exception {
		String[] ngram = new String[] {"h", "e", "l", "l", "o", "$"};
		String[] gotNgram = NgramCompiler.atEndOfString(ngram);
		String[] expNgram = new String[] {"h", "e", "l", "l", "o", "$"};
		AssertObject.assertDeepEquals("", expNgram, gotNgram);
	}
	
	@Test
	public void test__atEndOfString__NullInput__ReturnsNull() throws Exception {
		String[] ngram = null;
		String[] gotNgram = NgramCompiler.atEndOfString(ngram);
		String[] expNgram = null;
		AssertObject.assertDeepEquals("", expNgram, gotNgram);
	}
	
	@Test
	public void test__atEndOfString__EmptyInput__AppendEndChar() throws Exception {
		String[] ngram = new String[0];
		String[] gotNgram = NgramCompiler.atEndOfString(ngram);
		String[] expNgram = new String[] {"$"};
		AssertObject.assertDeepEquals("", expNgram, gotNgram);
	}

	///////////////////////////////
	// HELPER METHODS
	//////////////////////////////
	
	private void assertNgramsEqual(
		String mess, String[] expNgramsArr, Set<String> gotNgrams) 
				throws Exception {
		Set<String> expNgrams = new HashSet<String>();
		for (String aNgram: expNgramsArr) {
			expNgrams.add(aNgram);
		}
		AssertObject.assertDeepEquals(
			mess+"\nNgrams were not as expected",
			expNgrams, gotNgrams);		
	}
	
	private void assertNgramsEqual(
		String mess, String[][] expNgramsArr, Set<String[]> gotNgramsArrs) 
		throws Exception {

		Set<String> expNgrams = new HashSet<String>();
		for (String[] aNgram: expNgramsArr) {
			expNgrams.add(String.join("|", aNgram));
		}
		
		Set<String> gotNgrams = new HashSet<String>();
		for (String[] aNgram: gotNgramsArrs) {
			gotNgrams.add(String.join("|", aNgram));
		}
		AssertObject.assertDeepEquals(
			mess+"\nNgrams were not as expected",
			expNgrams, gotNgrams);
		
		}
}
