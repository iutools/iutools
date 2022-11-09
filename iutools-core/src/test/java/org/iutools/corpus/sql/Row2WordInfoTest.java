package org.iutools.corpus.sql;

import ca.nrc.testing.AssertString;
import ca.nrc.testing.RunOnCases;
import org.iutools.corpus.CompiledCorpusException;
import org.iutools.corpus.WordInfo;
import org.iutools.sql.Row2Pojo;
import org.iutools.sql.Row2PojoTest;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class Row2WordInfoTest extends Row2PojoTest<WordInfo> {

	/////////////////////////////
	// DOCUMENTATION TESTS
	/////////////////////////////

	@Test
	public void test__Row2WordInfo__Synopsis() throws Exception {
		// Use Row2WordInfo to convert a WordInfo object to an SQL row
		//
		Row2WordInfo converter = new Row2WordInfo();

		// Say you have a WordInfo object...
		WordInfo winfo =
			new WordInfo("inuktitut");
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

		// You can convert it to a JSON object that contains the data that must
		// be used to put that WordInfo in the SQL db.
		JSONObject rowData = converter.toRowJson(winfo);

		// Note that the rowData object contains some columns that are not part
		// of the original WordInfo object.
		//
		// For example, the wordNgrams column is a string that allows you to search
		// for words that contain a particular ngram.
		//
		String wordNgrams = rowData.getString("wordNgrams");

		// Similarly, the morphemeNgrams and morphemeNgramsWrittenForms columns
		// are strings that allow you to search for words that contain a particular
		// ngram of morphemes (in either written or canonical form).
		//
		String morphemeNgrams = rowData.getString("morphemeNgrams");
		String morphemeNgramsWrittenForms = rowData.getString("morphemeNgramsWrittenForms");

		// Row2WordInfo also has some static methods that you can use compose part
		// of an SQL query that match agains wordNgrams, morphemeNgrams or
		// morphemeNgramsWrittenForms
		//
		String ngram = "^inuk";
		String ngramQuery = Row2WordInfo.formatNgramAsSearchableString(ngram);
	}

	//////////////////////////////////////
	// VERIFICATION TESTS
	//////////////////////////////////////

	@Test
	public void test__wordNgrams__HappyPath() {
		String gotNgramsStr = Row2WordInfo.wordNgrams("nunavut");
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
				super(_descr, new Object[] {expNgrams, false, morphemes});
			}
			public MorphNgramCase(String _descr, String expNgrams, Boolean writtenForms, String... morphemes) {
				super(_descr, new Object[] {expNgrams, writtenForms, morphemes});
			}

		}

		MorphNgramCase[] cases = new MorphNgramCase[] {

			// Cases where we want Ngrams in the form of full IDs
			new MorphNgramCase(
				"Multiple morphemes - NO braces",
				"BEG__inuk_1n BEG__inuk_1n__titut_tn_sim_p BEG__inuk_1n__titut_tn_sim_p__END inuk_1n inuk_1n__titut_tn_sim_p inuk_1n__titut_tn_sim_p__END titut_tn_sim_p titut_tn_sim_p__END",
				"inuk/1n", "titut/tn-sim-p"
			),
			new MorphNgramCase(
				"Multiple morphemes - WITH braces",
				"BEG__inuk_1n BEG__inuk_1n__titut_tn_sim_p BEG__inuk_1n__titut_tn_sim_p__END inuk_1n inuk_1n__titut_tn_sim_p inuk_1n__titut_tn_sim_p__END titut_tn_sim_p titut_tn_sim_p__END",
				"{inuk/1n}", "{titut/tn-sim-p}"
			),
			new MorphNgramCase(
				"Single morpheme - NO braces",
				"BEG__inuk_1n BEG__inuk_1n__END inuk_1n inuk_1n__END",
				"inuk/1n"
			),
			new MorphNgramCase(
				"Single morpheme - WITH braces",
				"BEG__inuk_1n BEG__inuk_1n__END inuk_1n inuk_1n__END",
				"{inuk/1n}"
			),

			new MorphNgramCase(
				"Ngram with tailing \\",
				"BEG__inuk_1n BEG__inuk_1n__END inuk_1n inuk_1n__END",
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

			// Cases where we want Ngrams in the form of written forms
			//
			new MorphNgramCase(
				"Multiple morphemes - NO braces",
				"BEG__inuk BEG__inuk__titut BEG__inuk__titut__END inukZZZZZ inuk__titut inuk__titut__END titut titut__END",
				true, "inuk/1n", "titut/tn-sim-p"
			),
			new MorphNgramCase(
				"Multiple morphemes - WITH braces",
				"BEG__inuk BEG__inuk__titut BEG__inuk__titut__END inukZZZZZ inuk__titut inuk__titut__END titut titut__END",
				true, "{inuk/1n}", "{titut/tn-sim-p}"
			),
			new MorphNgramCase(
				"Single morpheme - NO braces",
				"BEG__inuk BEG__inuk__END inukZZZZZ inuk__END",
				true, "inuk/1n"
			),
			new MorphNgramCase(
				"Single morpheme - WITH braces",
				"BEG__inuk BEG__inuk__END inukZZZZZ inuk__END",
				true, "{inuk/1n}"
			),

			new MorphNgramCase(
				"Ngram with tailing \\",
				"BEG__inuk BEG__inuk__END inukZZZZZ inuk__END",
				true, "inuk/1n", "\\"
			),

			new MorphNgramCase(
				"null decomp",
				"",
				true, (String[])null
			),
			new MorphNgramCase(
				"empty decomp",
				""
			),

		};

		Consumer<RunOnCases.Case> runner = (aCase) -> {
			String expNgrams = (String) aCase.data[0];
			Boolean writtenForms = (Boolean) aCase.data[1];
			String[] decomp = (String[]) aCase.data[2];
			try {
				String gotNgrams =
					Row2WordInfo.computeMorphNgrams(
						writtenForms, decomp);
				String mess = aCase.descr;
				if (writtenForms) {
					mess += " (as written forms)";
				}
				mess += "\nMorpheme Ngrams not as expected.";
				AssertString.assertStringEquals(
					mess,
					expNgrams, gotNgrams);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};

		new RunOnCases((RunOnCases.Case[])cases, runner)
//			.onlyCaseNums(8)
			.run();
	}

	@Test
	public void test__formatNgramAsSearchableString__VariousCases() throws Exception {
		class FormatNgramCase extends RunOnCases.Case {
			public FormatNgramCase(String _descr, String expFormatted, String... morphemes) {
				super(_descr, new Object[] {expFormatted, morphemes});
			}
		}

		FormatNgramCase[] cases = new FormatNgramCase[] {

			new FormatNgramCase("Single morpheme - NO braces - MIDDLE of word - leading and tailing spaces",
				"tut_1n", " tut/1n "),
			new FormatNgramCase("Single morpheme - NO braces - START of word",
				"BEG__inuk_1n", "^", "inuk/1n"),
			new FormatNgramCase("Single morpheme - NO braces - END of word",
				"tut_1n__END", "tut/1n", "$"),
			new FormatNgramCase("Single morpheme - NO braces - START and END of word",
				"BEG__inuk_1n__END", "^", "inuk/1n", "$"),

			new FormatNgramCase("Single morpheme - WITH braces - MIDDLE of word",
				"tut_1n", "{tut/1n}"),
			new FormatNgramCase("Single morpheme - WITH braces - MIDDLE of word - leading and tailing spaces",
				"tut_1n", " {tut/1n} "),
			new FormatNgramCase("Single morpheme - WITH braces - START of word",
				"BEG__inuk_1n", "^", "{inuk/1n}"),
			new FormatNgramCase("Single morpheme - WITH braces - END of word",
				"tut_1n__END", "{tut/1n}", "$"),
			new FormatNgramCase("Single morpheme - WITH braces - START and END of word",
				"BEG__inuk_1n__END", "^", "{inuk/1n}", "$"),

			new FormatNgramCase("Multiple morphemes - NO braces - MIDDLE of word",
				"inuk_1n__titut_tn_sim_p", "inuk/1n", "titut/tn-sim-p"),
			new FormatNgramCase("Multiple morphemes - NO braces - MIDDLE of word - leading and tailing spaces",
				"inuk_1n__titut_tn_sim_p", " inuk/1n ", " titut/tn-sim-p "),
			new FormatNgramCase("Multiple morphemes - NO braces - START of word",
				"BEG__inuk_1n__titut_tn_sim_p", "^", "inuk/1n", "titut/tn-sim-p"),
			new FormatNgramCase("Multiple morphemes - NO braces - END of word",
				"inuk_1n__titut_tn_sim_p__END", "inuk/1n", "titut/tn-sim-p", "$"),
			new FormatNgramCase("Multiple morphemes - NO braces - START and END of word",
				"BEG__inuk_1n__titut_tn_sim_p__END", "^", "inuk/1n", "titut/tn-sim-p", "$"),

			new FormatNgramCase("Multiple morphemes - WITH braces - MIDDLE of word",
				"inuk_1n__titut_tn_sim_p", "{inuk/1n}", "{titut/tn-sim-p}"),
			new FormatNgramCase("Multiple morphemes - WITH braces - MIDDLE of word - leading and tailing spaces",
				"inuk_1n__titut_tn_sim_p", " {inuk/1n} ", " {titut/tn-sim-p} "),
			new FormatNgramCase("Multiple morphemes - WITH braces - START of word",
				"BEG__inuk_1n__titut_tn_sim_p", "^", "{inuk/1n}", "{titut/tn-sim-p}"),
			new FormatNgramCase("Multiple morphemes - WITH braces - END of word",
				"inuk_1n__titut_tn_sim_p__END", "{inuk/1n}", "{titut/tn-sim-p}", "$"),
			new FormatNgramCase("Multiple morphemes - WITH braces - START and END of word",
				"BEG__inuk_1n__titut_tn_sim_p__END", "^", "{inuk/1n}", "{titut/tn-sim-p}", "$"),

			new FormatNgramCase("Single morpheme - starts with '^'",
				"BEG__inuk_1n", "^{inuk/1n}"),
			new FormatNgramCase("Single morpheme - ends with '$'",
				"inuk_1n__END", "{inuk/1n}$"),
			new FormatNgramCase("Single morpheme - starts with '^' and ends with '$'",
				"BEG__inuk_1n__END",
				"^{inuk/1n}$"),

			new FormatNgramCase("Ngram with tailing \\",
				"inuk_1n",
				"inuk/1n", "\\" ),
			new FormatNgramCase("null decomp",
				"",
				(String[])null),
			new FormatNgramCase("Empty decomp",
				""),
			new FormatNgramCase("Just ^",
				"BEGZZZZZ",
				"^"),
			new FormatNgramCase("Just $",
				"ENDZZZZZ",
				"$"),
		};

		Consumer<RunOnCases.Case> runner = (aCase) -> {
			String expFormatted = (String) aCase.data[0];
			String[] decompArr = (String[]) aCase.data[1];
			List<String> decomp = null;
			if (decompArr != null) {
				decomp = new ArrayList<String>();
				Collections.addAll(decomp, decompArr);
			}
			try {
				String gotFormated = Row2WordInfo.formatNgramAsSearchableString((Boolean)null, decomp);
				AssertString.assertStringEquals(
					aCase.descr+"\nFormatted Morpheme Ngram not as expected.",
					expFormatted, gotFormated);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};

		new RunOnCases((RunOnCases.Case[])cases, runner)
//			.onlyCaseNums(10)
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

	@Override
	protected Row2Pojo<WordInfo> makeRow2Pojo() {
		return new Row2WordInfo();
	}

	@Override
	protected WordInfo makeTestPojo() throws CompiledCorpusException {
		WordInfo winfo =
			new WordInfo("inuktitut");
		winfo
			.setFrequency(237)
			.setDecompositions(
				new String[][] {
					new String[] {"inuk/1n", "titut/tn-sim-p"},
					new String[] {"inuk/1n", "ktut/tn-sim-p-2s"},
					new String[] {"inuk/1n", "iq/1nn", "tut/tn-sim-s"}
				}, 13);
		return winfo;
	}
}
