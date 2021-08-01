package org.iutools.concordancer.tm;

import ca.nrc.testing.AssertObject;
import ca.nrc.testing.AssertString;
import org.apache.commons.lang3.tuple.Pair;
import org.iutools.concordancer.SentencePair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordSpotterTest {

	SentencePair pair = null;
	SentencePair pair_stemmedTokens = null;
	SentencePair pair_stemmedTokensIU = null;

	Object[][] cases = null;


	@BeforeEach
	public void setUp() {
		pair =
			new SentencePair(
				"en", "Hello, beautiful world!",
				"fr", "Bonjour à ce monde magnifique!")
			.setTokenAlignments(
				"en", new String[] {"Hello", ",", "beautiful", "world", "!"},
				"fr", new String[] {"Bonjour", "à", "ce", "monde", "magnifique", "!"},
				new Integer[][] {
					new Integer[]{0,0}, new Integer[]{2,4}, new Integer[]{3,3},
					new Integer[]{4,5}});

		pair_stemmedTokens =
			new SentencePair(
				"en", "Hello, beautiful world!",
				"fr", "Bonjour à ce monde magnifique!")
			.setTokenAlignments(
				"en", new String[] {"Hello", ",", "beautiful", "world", "!"},
				"fr", new String[] {"Bonj@@", "à", "ce", "mond@@", "magn@@", "!"},
				new Integer[][] {
					new Integer[]{0,0}, new Integer[]{2,4}, new Integer[]{3,3},
					new Integer[]{4,5}});

		pair_stemmedTokensIU =
			new SentencePair(
				"en", "Stephen Inukshuk",
				"iu", "ᓯᑏᕙᓐ ᐃᓄᒃᓱᒃ")
			.setTokenAlignments(
				"en", new String[] {"Hello", ",", "beautiful", "world", "!"},
				"iu", new String[] {"ᓯᑏ@@", "ᕙᓐ", "ᐃᓄ@@", "ᒃᓱᒃ"},
				new Integer[][] {
					new Integer[]{0,0}, new Integer[]{1,0}, new Integer[]{1,1},
					new Integer[]{2,1}, new Integer[] {3,3}, new Integer[] {4,3},
					new Integer[] {5,2}, new Integer[] {5,3}});


		cases = new Object[][] {
			new Object[] {pair, "en", "hello", "Hello", "Bonjour"},
			new Object[] {pair, "fr", "bonjour", "Bonjour", "Hello"},
			new Object[] {pair, "fr", "monde magnifique", "monde magnifique", "beautiful world"},
			new Object[] {pair_stemmedTokens, "en", "hello", "Hello", "Bonjour"},
			new Object[] {pair_stemmedTokens, "fr", "bonjour", "Bonjour", "Hello"},
			};
	}


	//////////////////////////////////
	// DOCUMENTATION TESTS
	//////////////////////////////////

	@Test
	public void test__WordSpotter__Synopsis() throws Exception {
		// Use WordSpotter to spot a word/expression and its equivalent in a
		// pair of sentences
		//
		SentencePair pair =
			new SentencePair(
				"en", "Hello, beautiful world!",
				"fr", "Bonjour à ce monde magnifique!"
			);

		// Note: Word spotting is only possible if the sentence pair specifies
		// word-level alignments.
		pair.setTokenAlignments(
			"en", new String[] {"Hello", ",", "beautiful", "world", "!"},
			"fr", new String[] {"Bonjour", "à", "ce", "monde", "magnifique", "!"},
			new Integer[][] {
				new Integer[]{0,0}, new Integer[]{2,4}, new Integer[]{3,3},
				new Integer[]{4,5}
			}
		);

		// You can spot the en word 'world' and it's fr equivalent as follows...
		String enWord = "world";
		WordSpotter spotter = new WordSpotter(pair);
		Map<String,String> spotting  = spotter.spot("en", enWord);
		for (String lang: new String[] {"en", "fr"}) {
			// This is the word in language lang.
			String langWord = spotting.get(lang);
		}

		// You can highlight the the word and its equivalent in the text
		// of the two sentences.
		//
		String tagName = "equiv";
		Map<String,String> highlighted  =
			spotter.highlight("en", enWord, tagName);
		for (String lang: new String[] {"en", "fr"}) {
			// This returns the text in one language, with the word or its
			// equivalent highlighted with html-ish tags <equiv></equiv>
			String langWord = highlighted.get(lang);
		}
	}

	//////////////////////////////////
	// VERIFICATION TESTS
	//////////////////////////////////

	@Test
	public void test__spot__SeveralCases() throws Exception {
		Integer focusOnCase = null;
//		focusOnCase = 2;

		int caseNum = -1;
		for (Object[] aCase: cases) {
			caseNum++;
			SentencePair pair = (SentencePair) aCase[0];
			String l1 = (String) aCase[1];
			String l1Expr = (String) aCase[2];
			String l2 = pair.otherLangThan(l1);
			String caseID = "#"+caseNum+": "+l1 + "-" + l1Expr;
			if (focusOnCase != null && !focusOnCase.equals(caseNum)) {
				continue;
			}
			String expL1Highlights = (String) aCase[3];
			String expL2Highlights = (String) aCase[4];
			WordSpotter spotter = new WordSpotter(pair);
			Map<String,String> gotSpottings  = spotter.spot(l1, l1Expr);
			Map<String,String> expSpottings = new HashMap<String,String>();
			{
				expSpottings.put(l1, expL1Highlights);
				expSpottings.put(l2, expL2Highlights);
			}
			AssertObject.assertDeepEquals(
			"Spottings not as expected for case: "+caseID,
				expSpottings, gotSpottings);

		}

		if (focusOnCase != null) {
			Assertions.fail("Test was only run on one of the cases. Don't to run all tests (i.e. set focuOnCase=null) .");
		}

	}

	@Test
	public void test__higlight__SeveralCases() throws Exception {
		String focusOnCase = null;
//		focusOnCase = "fr-bonjour";

		for (Object[] aCase: cases) {
			SentencePair pair = (SentencePair) aCase[0];
			String l1 = (String) aCase[1];
			String l1Expr = (String) aCase[2];
			String caseID = l1+"-"+l1Expr;
			if (focusOnCase != null && !focusOnCase.equals(caseID)) {
				continue;
			}
			String expL1Highlights = (String) aCase[3];
			String expL2Highlights = (String) aCase[4];
			String enWord = "hello";
			WordSpotter spotter = new WordSpotter(pair);
			new AssertWordSpotter(spotter)
				.producesHighlights(l1, l1Expr, expL1Highlights, expL2Highlights);
		}

		if (focusOnCase != null) {
			Assertions.fail("Test was only run on one of the cases. Don't to run all tests (i.e. set focuOnCase=null) .");
		}
	}

	@Test
	public void test__tokensMatching__VariousCases() throws Exception {
		String focusOnCase = null;
//		focusOnCase = "beautiful world";

		Object[][] cases = new Object[][] {
			new Object[] {
				"en", "beautiful world", new int[] {2, 3}
			},
			new Object[] {
				"en", "hello", new int[] {0}
			},
			new Object[] {
				"en", "should not be found", new int[] {}
			},
			new Object[] {
				"fr", "monde magnifique", new int[] {3, 4}
			},
			new Object[] {
				"en", null, new int[] {}
			},
			new Object[] {
				"en", "", new int[] {}
			}
		};
		for (Object[] aCase: cases) {
			String lang = (String)aCase[0];
			String text = (String)aCase[1];
			if (focusOnCase != null && !focusOnCase.equals(text)) {
				continue;
			}
			int[] expTokens = (int[])aCase[2];
			WordSpotter spotter = new WordSpotter(pair);
			int[] gotTokens = spotter.tokensMatchingText(lang, text);
			AssertObject.assertDeepEquals(
				"Tokens not as expected for lang="+lang+", text="+text,
				expTokens, gotTokens);
		}

		if (focusOnCase != null) {
			Assertions.fail("Test only run on one case. Make sure you set focusOnCase=null to run on all tests");
		}
	}

	@Test
	public void test__ensureTagsAreNotInMiddleOfWord__SeveralCases() {
		Integer focusOnCaseNum = 1;
		List<Pair<String,String>> cases = new ArrayList<Pair<String,String>>();
		{
			cases.add(
				Pair.of(
					"H<strong>el</strong>lo, beautiful  <strong>world</strong>",
					"<strong>Hello</strong> beautiful <strong>world</strong>"
				)
			);
			cases.add(
				Pair.of(
					"<strong>Hello</strong>, beautiful world!",
					"<strong>Hello</strong>, beautiful world!"
				)
			);
		}

		int caseNum = -1;
		for (Pair<String,String> aCase: cases) {
			caseNum++;
			if (focusOnCaseNum != null && focusOnCaseNum != caseNum) {
				continue;
			}
			String gotText =
				WordSpotter.ensureTagsAreNotInMiddleOfWord(aCase.getLeft(), "strong");
		AssertString.assertStringEquals(
			"Fixed tags not as expected for case #"+caseNum,
			aCase.getRight(), gotText);
		}
	}

	//////////////////////////////////
	// TEST HELPERS
	//////////////////////////////////


}
