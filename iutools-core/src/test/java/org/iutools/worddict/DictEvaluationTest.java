package org.iutools.worddict;

import ca.nrc.testing.AssertRuntime;
import org.iutools.config.IUConfig;
import org.iutools.sql.SQLLeakMonitor;
import org.iutools.utilities.StopWatch;
import org.iutools.worddict.MachineGeneratedDict.*;
import org.iutools.concordancer.tm.TMEvaluator.MatchType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.nio.file.Paths;

public class DictEvaluationTest {

	SQLLeakMonitor sqlLeakMonitor = null;

	@BeforeEach
	public void setUp() throws Exception {
		sqlLeakMonitor = new SQLLeakMonitor();
	}

	@AfterEach
	public void tearDown() {
		sqlLeakMonitor.assertNoLeaks();
	}

	@Test
	public void test_evaluateWordDict_OnWikipediaGlossary_first20(
		TestInfo testInfo) throws Exception {
		String glossaryPath = IUConfig.getIUDataPath("data/glossaries/wpGlossary.json");
		int stopAfterN = 20;
		MDictEvaluator evaluator = new MDictEvaluator()
//				.setMinMaxPairs(null, 20).setMaxTranslations(5)
//				.setMinMaxPairs(100, 100).setMaxTranslations(5)
//				.setMinMaxPairs(50, 50).setMaxTranslations(5)
//				.setMinMaxPairs(null, 100).setMaxTranslations(10)
//				.setMinMaxPairs(100, 100).setMaxTranslations(10)
				;

		long start = StopWatch.nowMSecs();
		DictEvaluationResults results =
			evaluator.evaluate(Paths.get(glossaryPath), stopAfterN, (Integer)null);

		EvaluationExpectations exp = new EvaluationExpectations()
			.setTotalGlossaryEntries(stopAfterN)
			.setTotalSingleWordIUEntries(13)
			.setTotalIUPresent(WhatTerm.ORIGINAL, 6)
			.setTotalIUPresent(WhatTerm.RELATED, 2)
			.setTotalENSpotted_Strict(2)
			.setTotalENSpotted_Lenient(0)
			.setTotalENSpotted_LenientOverlap(1)
			;
		assertExpectationsMet(exp, results);

		AssertRuntime.runtimeHasNotChanged(
			results.avgSecsPerEntryPresent, 1.545,
			"avg secs for retrieving a dict entry", testInfo);
	}

	@Test
	public void test_evaluateWordDict_OnWikipediaGlossary_AllEntries(
		TestInfo testInfo) throws Exception {
		String glossaryPath = IUConfig.getIUDataPath("data/glossaries/wpGlossary.json");
		MDictEvaluator evaluator = new MDictEvaluator()
			.setMinMaxPairs(null, 100)
			.setMaxTranslations(10);

		// Change those if you want to debug specific glossary entries
		Integer stopAfterN = null;
		Integer startingAtN = null;

		DictEvaluationResults results =
			evaluator.evaluate(Paths.get(glossaryPath), stopAfterN, startingAtN);

		EvaluationExpectations exp = new EvaluationExpectations()
			.setTotalGlossaryEntries(556)
			.setTotalSingleWordIUEntries(465)
			.setTotalIUPresent(WhatTerm.ORIGINAL, 183)
			.setTotalIUPresent(WhatTerm.RELATED, 75)
			.setTotalENSpotted_Strict(93)
			.setTotalENSpotted_Lenient(4)
			.setTotalENSpotted_LenientOverlap(11)
			;
		assertExpectationsMet(exp, results);

		AssertRuntime.runtimeHasNotChanged(
			results.avgSecsPerEntryPresent, 0.20,
			"avg secs for retrieving a dict entry", testInfo);


	}

	////////////////////////////////////////////
	// TEST HELPERS
	////////////////////////////////////////////

	public static class EvaluationExpectations {
		Integer totalGlossaryEntries = null;
		Integer totalSingleWordIUEntries = null;

		Integer totalIUPresent_OriginalTerm = null;
		Integer totalIUPresent_RelatedTerms = null;

		Integer totalENSpotted_Strict = null;
		Integer totalENSpotted_Lenient = null;
		Integer totalENSpotted_LenientOverlap = 11;

		public int totalIUPresent_OriginalOrRelated() {
			return totalIUPresent_OriginalTerm + totalIUPresent_RelatedTerms;
		}

		public int totalENSpotted_atLeastStrict() {
			return totalENSpotted_Strict;
		}

		public int totalENSpotted_atLeastLenient() {
			return totalENSpotted_Strict + totalENSpotted_Lenient;
		}

		public int totalENSpotted_atLeastLenientOverlap() {
			return totalENSpotted_Strict + totalENSpotted_Lenient +
			totalENSpotted_LenientOverlap;
		}

		public double rateENSpotted_Strict() {
			return 1.0 * totalENSpotted_atLeastStrict() / totalIUPresent_OriginalOrRelated();
		}

		public double rateENSpotted_Lenient() {
			return 1.0 * totalENSpotted_atLeastLenient() / totalIUPresent_OriginalOrRelated();
		}

		public double rateENSpotted_LenientOverlap() {
			return 1.0 * totalENSpotted_atLeastLenientOverlap() / totalIUPresent_OriginalOrRelated();
		}

		public EvaluationExpectations setTotalGlossaryEntries(int total) {
			totalGlossaryEntries = total;
			return this;
		}

		public EvaluationExpectations setTotalSingleWordIUEntries(int total) {
			totalSingleWordIUEntries = total;
			return this;
		}

		public EvaluationExpectations setTotalIUPresent(WhatTerm whatTerm, int total) {
			if (whatTerm == WhatTerm.ORIGINAL) {
				totalIUPresent_OriginalTerm = total;
			} else {
				totalIUPresent_RelatedTerms = total;
			}
			return this;
		}

		public EvaluationExpectations setTotalENSpotted_Strict(int total) {
			totalENSpotted_Strict = total;
			return this;
		}

		public EvaluationExpectations setTotalENSpotted_Lenient(int total) {
			totalENSpotted_Lenient = total;
			return this;
		}

		public EvaluationExpectations setTotalENSpotted_LenientOverlap(int total) {
			totalENSpotted_LenientOverlap = total;
			return this;
		}
	}

	protected void assertExpectationsMet(
		EvaluationExpectations exp, DictEvaluationResults results) {
		new AssertDictEvaluationResults(results)
			.totalGlossaryEntries(exp.totalGlossaryEntries)
			.totalSingleWordIUEntries(exp.totalSingleWordIUEntries)

			.totalIUPresent(WhatTerm.ORIGINAL, exp.totalIUPresent_OriginalTerm)
			.totalIUPresent(WhatTerm.RELATED, exp.totalIUPresent_RelatedTerms)

			.totalENSpotted(MatchType.STRICT, exp.totalENSpotted_Strict)
			.totalENSpotted(MatchType.LENIENT, exp.totalENSpotted_Lenient)
			.totalENSpotted(MatchType.LENIENT_OVERLAP, exp.totalENSpotted_LenientOverlap)

			.totalENSpotted_atLeastInSense(MatchType.STRICT, exp.totalENSpotted_atLeastStrict())
			.totalENSpotted_atLeastInSense(MatchType.LENIENT, exp.totalENSpotted_atLeastLenient())
			.totalENSpotted_atLeastInSense(MatchType.LENIENT_OVERLAP, exp.totalENSpotted_atLeastLenientOverlap())

			.rateENSpotted(MatchType.STRICT, exp.rateENSpotted_Strict())
			.rateENSpotted(MatchType.LENIENT, exp.rateENSpotted_Lenient())
			.rateENSpotted(MatchType.LENIENT_OVERLAP, exp.rateENSpotted_LenientOverlap())
			;
	}
}
