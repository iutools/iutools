package org.iutools.corpus.sql;

import ca.nrc.testing.AssertString;
import ca.nrc.testing.RunOnCases;
import org.iutools.corpus.CompiledCorpusTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.function.Consumer;

public class CompiledCorpus_SQLTest extends CompiledCorpusTest {

	public final static String testCorpusName = "test-corpus";

	@Before
	public void setUp() throws Exception {
		CompiledCorpus_SQL corpus = new CompiledCorpus_SQL(testCorpusName);
		corpus.clearWords(false);
		corpus.changeLastUpdatedHistory(new Long(0));
		return;
	}

	@After
	public void tearDown() throws Exception {
		new CompiledCorpus_SQL(testCorpusName).clearWords(false);
	}

	public CompiledCorpus_SQL makeCorpusWithDefaultSegmenter() throws Exception {
		return new CompiledCorpus_SQL("test-corpus");
	}

	////////////////////////////////////////////////
	// VERIFICATION TESTS
	////////////////////////////////////////////////

	@Test
	public void test__formatMorphNgram4SqlSearching__VariousCases()
		throws Exception {
		class FormattingCase extends RunOnCases.Case {
			public FormattingCase(String _descr, String expFormatted, String... morphemes) {
				super(_descr, new Object[] {expFormatted, morphemes});
			}
		}

		FormattingCase[] cases = new FormattingCase[] {
			new FormattingCase("Single morpheme - NO braces - MIDDLE of word",
				"%{tut/1n}%", "tut/1n"),
			new FormattingCase("Single morpheme - NO braces - MIDDLE of word - leading and tailing spaces",
				"%{tut/1n}%", " tut/1n "),
			new FormattingCase("Single morpheme - NO braces - START of word",
				"{inuk/1n}%", "^", "inuk/1n"),
			new FormattingCase("Single morpheme - NO braces - END of word",
				"%{tut/1n}", "tut/1n", "$"),
			new FormattingCase("Single morpheme - NO braces - START and END of word",
				"{inuk/1n}", "^", "inuk/1n", "$"),

			new FormattingCase("Single morpheme - WITH braces - MIDDLE of word",
				"%{tut/1n}%", "{tut/1n}"),
			new FormattingCase("Single morpheme - WITH braces - MIDDLE of word - leading and tailing spaces",
				"%{tut/1n}%", " {tut/1n} "),
			new FormattingCase("Single morpheme - WITH braces - START of word",
				"{inuk/1n}%", "^", "{inuk/1n}"),
			new FormattingCase("Single morpheme - WITH braces - END of word",
				"%{tut/1n}", "{tut/1n}", "$"),
			new FormattingCase("Single morpheme - WITH braces - START and END of word",
				"{inuk/1n}", "^", "{inuk/1n}", "$"),

			new FormattingCase("Multiple morphemes - NO braces - MIDDLE of word",
				"%{inuk/1n} {titut/tn-sim-p}%", "inuk/1n", "titut/tn-sim-p"),
			new FormattingCase("Multiple morphemes - NO braces - MIDDLE of word - leading and tailing spaces",
				"%{inuk/1n} {titut/tn-sim-p}%", " inuk/1n ", " titut/tn-sim-p "),
			new FormattingCase("Multiple morphemes - NO braces - START of word",
				"{inuk/1n} {titut/tn-sim-p}%", "^", "inuk/1n", "titut/tn-sim-p"),
			new FormattingCase("Multiple morphemes - NO braces - END of word",
				"%{inuk/1n} {titut/tn-sim-p}", "inuk/1n", "titut/tn-sim-p", "$"),
			new FormattingCase("Multiple morphemes - NO braces - START and END of word",
				"{inuk/1n} {titut/tn-sim-p}", "^", "inuk/1n", "titut/tn-sim-p", "$"),

			new FormattingCase("Multiple morphemes - WITH braces - MIDDLE of word",
				"%{inuk/1n} {titut/tn-sim-p}%", "{inuk/1n}", "{titut/tn-sim-p}"),
			new FormattingCase("Multiple morphemes - WITH braces - MIDDLE of word - leading and tailing spaces",
				"%{inuk/1n} {titut/tn-sim-p}%", " {inuk/1n} ", " {titut/tn-sim-p} "),
			new FormattingCase("Multiple morphemes - WITH braces - START of word",
				"{inuk/1n} {titut/tn-sim-p}%", "^", "{inuk/1n}", "{titut/tn-sim-p}"),
			new FormattingCase("Multiple morphemes - WITH braces - END of word",
				"%{inuk/1n} {titut/tn-sim-p}", "{inuk/1n}", "{titut/tn-sim-p}", "$"),
			new FormattingCase("Multiple morphemes - WITH braces - START and END of word",
				"{inuk/1n} {titut/tn-sim-p}", "^", "{inuk/1n}", "{titut/tn-sim-p}", "$"),

			new FormattingCase("Single morpheme - starts with '^'",
				"{inuk/1n}%", "^{inuk/1n}"),
			new FormattingCase("Single morpheme - ends with '$'",
				"%{inuk/1n}", "{inuk/1n}$"),
			new FormattingCase("Single morpheme - starts with '^' and ends with '$'",
				"{inuk/1n}", "^{inuk/1n}$"),
		};

		Consumer<RunOnCases.Case> runner = (aCase) -> {
			try {
				String descr = aCase.descr;
				String expFormatted = (String) aCase.data[0];
				String[] morphemes = (String[]) aCase.data[1];
				String gotFormatted = CompiledCorpus_SQL.formatMorphNgram4SqlSearching(morphemes);
				AssertString.assertStringEquals(
					descr+"\nFormatted morphemes not as expected for morphemes='"+String.join(",", morphemes),
					expFormatted, gotFormatted
				);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};

		new RunOnCases(cases, runner)
//			.onlyCaseNums(2)
			.run();
	}
}
