package ca.pirurvik.iutools.text.ngrams;

import static org.junit.Assert.*;

import org.junit.Test;

import ca.nrc.testing.AssertObject;

public class NgramCompilerTest {

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


}
