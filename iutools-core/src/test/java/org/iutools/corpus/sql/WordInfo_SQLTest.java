package org.iutools.corpus.sql;

import ca.nrc.testing.AssertString;
import ca.nrc.testing.RunOnCases;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

public class WordInfo_SQLTest {

	/////////////////////////////
	// DOCUMENTATION TESTS
	/////////////////////////////

	@Test
	public void test__WordInfo_SQL__Synopsis() throws Exception {
		// WordInfo represents information about an Inuktitut, in a way that can
		// be stored, retrieved and searched in SQL database.
		//
		WordInfo_SQL winfo =
			new WordInfo_SQL("inuktitut");
		winfo
			.setFrequency(237)
			.setDecompositions(
				// Sample of all possible decompositions
				new String[][] {
					new String[] {"inuk/1n", "titut/tn-sim-p"},
					new String[] {"inuk/1n", "ktut/tn-sim-p-2s"},
					new String[] {"inuk/1n", "iq/1nn", "tut/tn-sim-s"}
				},
				// Total number of possible decompositions (may be greater than the
				// number of sample decomps.
				13
			);

		// In addition to the inherited methods, WordInfo_SQL add some methods
		// and fields that are useful for SQL

		/**
		 * This field lists all the character ngrams for the word. It makes it possible for SQL
		 * to rapidly find words that contain a particular ngram.
		 */
		winfo.getWordNgrams();

		/**
		 * This field lists all the morpheme ngrams for the word. It makes it possible for SQL
		 * to rapidly find words that contain a particular ngram.
		 */
		String morphNgrams = winfo.getMorphemeNgrams();
	}

	//////////////////////////////////////
	// VERIFICATION TESTS
	//////////////////////////////////////

	@Test
	public void test__getWordNgrams__HappyPath() {
		WordInfo_SQL winfo = new WordInfo_SQL("nunavut");
		String gotNgramsStr = winfo.getWordNgrams();
		String[] expNgrams = new String[] {
			"BEGnuna", "unavu", "BEGnunavut", "BEGnunavutEND", "vutEND"
		};
		assertContainsNgrams(
			"Ngram string for word nunavut did not contain the expected ngrams",
			expNgrams, gotNgramsStr);
	}


	@Test
	public void test__computeMorphNgrams__VariousCases() throws Exception {
		class MorphNgramCase extends RunOnCases.Case {
			public MorphNgramCase(String _descr, String expNgrams, String... morphemes) {
				super(_descr, new Object[] {expNgrams, morphemes});
			}
		}

		MorphNgramCase[] cases = new MorphNgramCase[] {
			new MorphNgramCase(
				"Multiple morphemes - NO braces",
				"inuk_1n inuk_1n-titut_tnsimp titut_tnsimp ",
				"inuk/1n", "titut/tn-sim-p"
			),
			new MorphNgramCase(
				"Multiple morphemes - WITH braces",
				"inuk_1n inuk_1n-titut_tnsimp titut_tnsimp ",
				"{inuk/1n}", "{titut/tn-sim-p}"
			),
			new MorphNgramCase(
				"Single morpheme - NO braces",
				"inuk_1n ",
				"inuk/1n"
			),
			new MorphNgramCase(
				"Single morpheme - WITH braces",
				"inuk_1n ",
				"{inuk/1n}"
			),

			new MorphNgramCase(
				"Ngram with tailing \\",
				"inuk_1n ",
				"inuk/1n",
				"\\"
			),

			new MorphNgramCase(
				"null decomp",
				"",
				(String[])null
			),
			new MorphNgramCase(
				"empty decomp",
				""
			),
		};

		Consumer<RunOnCases.Case> runner = (aCase) -> {
			String expNgrams = (String) aCase.data[0];
			String[] decomp = (String[]) aCase.data[1];
			try {
				String gotNgrams = WordInfo_SQL.computeMorphNgrams(decomp);
				AssertString.assertStringEquals(
					aCase.descr+"\nMorpheme Ngrams not as expected.",
					expNgrams, gotNgrams);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};

		new RunOnCases((RunOnCases.Case[])cases, runner)
//			.onlyCaseNums(5)
			.run();
	}

	//////////////////////////////////////
	// TEST HELPERS
	//////////////////////////////////////

	private void assertContainsNgrams(String mess, String[] expNgrams,
		String gotNgramsStr) {
		for (String expNgram: expNgrams) {
			AssertString.assertStringContains(
				mess, gotNgramsStr, expNgram
			);
		}
	}
}
