package org.iutools.corpus.sql;

import org.iutools.corpus.CompiledCorpusTest;
import org.iutools.corpus.CorpusTestHelpers;
import org.iutools.sql.ResourcesTracker;
import org.iutools.sql.SQLTestHelpers;
import org.junit.After;
import org.junit.Before;

public class CompiledCorpus_SQLTest extends CompiledCorpusTest {

	public final static String testCorpusName = "test-corpus";

	private Integer initialOpenedStatements = null;
	private Integer initialOpenedResultSets = null;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		// Remember how many opened SQL resources there were when
		// we started this test
		initialOpenedResultSets = ResourcesTracker.totalResultSets(true);
		initialOpenedStatements = ResourcesTracker.totalStatements(true);

		CorpusTestHelpers.clearCorpus(testCorpusName);
		return;
	}

	@After
	public void tearDown() throws Exception {
		new CompiledCorpus_SQL(testCorpusName).clearWords(false);
		// Make sure the previous test did not increase the number of opened
		// SQL resources
		SQLTestHelpers.assertOpenedResourcesAre(
			"Tests have changed the number of opened SQL resources",
			initialOpenedStatements, initialOpenedResultSets);
	}

	public CompiledCorpus_SQL makeCorpusWithDefaultSegmenter() throws Exception {
		return new CompiledCorpus_SQL("test-corpus");
	}

	////////////////////////////////////////////////
	// VERIFICATION TESTS
	////////////////////////////////////////////////

//	@Test
//	public void test__formatMorphNgram4SqlSearching__VariousCases()
//		throws Exception {
//		class FormattingCase extends RunOnCases.Case {
//			public FormattingCase(String _descr, String expFormatted, String... morphemes) {
//				super(_descr, new Object[] {expFormatted, morphemes});
//			}
//		}
//
//		FormattingCase[] cases = new FormattingCase[] {
//			new FormattingCase("Single morpheme - NO braces - MIDDLE of word",
//				"tut_1n", "tut/1n"),
//			new FormattingCase("Single morpheme - NO braces - MIDDLE of word - leading and tailing spaces",
//				"tut_1n", " tut/1n "),
//			new FormattingCase("Single morpheme - NO braces - START of word",
//				"BEG__inuk_1n", "^", "inuk/1n"),
//			new FormattingCase("Single morpheme - NO braces - END of word",
//				"tut_1n__END", "tut/1n", "$"),
//			new FormattingCase("Single morpheme - NO braces - START and END of word",
//				"BEG__inuk_1n__END", "^", "inuk/1n", "$"),
//
//			new FormattingCase("Single morpheme - WITH braces - MIDDLE of word",
//				"tut_1n", "{tut/1n}"),
//			new FormattingCase("Single morpheme - WITH braces - MIDDLE of word - leading and tailing spaces",
//				"tut_1n", " {tut/1n} "),
//			new FormattingCase("Single morpheme - WITH braces - START of word",
//				"BEG__inuk_1n", "^", "{inuk/1n}"),
//			new FormattingCase("Single morpheme - WITH braces - END of word",
//				"tut_1n__END", "{tut/1n}", "$"),
//			new FormattingCase("Single morpheme - WITH braces - START and END of word",
//				"BEG__inuk_1n__END", "^", "{inuk/1n}", "$"),
//
//			new FormattingCase("Multiple morphemes - NO braces - MIDDLE of word",
//				"inuk_1n__titut_tn_sim_p", "inuk/1n", "titut/tn-sim-p"),
//			new FormattingCase("Multiple morphemes - NO braces - MIDDLE of word - leading and tailing spaces",
//				"inuk_1n__titut_tn_sim_p", " inuk/1n ", " titut/tn-sim-p "),
//			new FormattingCase("Multiple morphemes - NO braces - START of word",
//				"BEG__inuk_1n__titut_tn_sim_p", "^", "inuk/1n", "titut/tn-sim-p"),
//			new FormattingCase("Multiple morphemes - NO braces - END of word",
//				"inuk_1n__titut_tn_sim_p__END", "inuk/1n", "titut/tn-sim-p", "$"),
//			new FormattingCase("Multiple morphemes - NO braces - START and END of word",
//				"BEG__inuk_1n__titut_tn_sim_p__END", "^", "inuk/1n", "titut/tn-sim-p", "$"),
//
//			new FormattingCase("Multiple morphemes - WITH braces - MIDDLE of word",
//				"inuk_1n__titut_tn_sim_p", "{inuk/1n}", "{titut/tn-sim-p}"),
//			new FormattingCase("Multiple morphemes - WITH braces - MIDDLE of word - leading and tailing spaces",
//				"inuk_1n__titut_tn_sim_p", " {inuk/1n} ", " {titut/tn-sim-p} "),
//			new FormattingCase("Multiple morphemes - WITH braces - START of word",
//				"BEG__inuk_1n__titut_tn_sim_p", "^", "{inuk/1n}", "{titut/tn-sim-p}"),
//			new FormattingCase("Multiple morphemes - WITH braces - END of word",
//				"inuk_1n__titut_tn_sim_p__END", "{inuk/1n}", "{titut/tn-sim-p}", "$"),
//			new FormattingCase("Multiple morphemes - WITH braces - START and END of word",
//				"BEG__inuk_1n__titut_tn_sim_p__END", "^", "{inuk/1n}", "{titut/tn-sim-p}", "$"),
//
//			new FormattingCase("Single morpheme - starts with '^'",
//				"BEG__inuk_1n", "^{inuk/1n}"),
//			new FormattingCase("Single morpheme - ends with '$'",
//				"inuk_1n__END", "{inuk/1n}$"),
//			new FormattingCase("Single morpheme - starts with '^' and ends with '$'",
//				"BEG__inuk_1n__END", "^{inuk/1n}$"),
//		};
//
//		Consumer<RunOnCases.Case> runner = (aCase) -> {
//			try {
//				String descr = aCase.descr;
//				String expFormatted = (String) aCase.data[0];
//				String[] morphemes = (String[]) aCase.data[1];
//				String gotFormatted = WordInfo_SQL.formatNgramAsSearchableString(morphemes);
//				AssertString.assertStringEquals(
//					descr+"\nFormatted morphemes not as expected for morphemes='"+String.join(",", morphemes),
//					expFormatted, gotFormatted
//				);
//			} catch (Exception e) {
//				throw new RuntimeException(e);
//			}
//		};
//
//		new RunOnCases(cases, runner)
////			.onlyCaseNums(21)
//			.run();
//	}
}
