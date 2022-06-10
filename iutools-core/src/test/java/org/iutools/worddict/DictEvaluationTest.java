package org.iutools.worddict;

import ca.nrc.testing.AssertRuntime;
import org.iutools.config.IUConfig;
import org.iutools.utilities.StopWatch;
import org.iutools.worddict.MultilingualDict.*;
import org.iutools.concordancer.tm.TMEvaluator.MatchType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.nio.file.Paths;

public class DictEvaluationTest {

	@Test
	public void test_evaluateWordDict_OnWikipediaGlossary_first20(
		TestInfo testInfo) throws Exception {
		String glossaryPath = IUConfig.getIUDataPath("data/glossaries/wpGlossary.json");
		int stopAfterN = 20;
		DictEvaluator evaluator = new DictEvaluator()
//				.setMinMaxPairs(null, 20).setMaxTranslations(5)
//				.setMinMaxPairs(100, 100).setMaxTranslations(5)
//				.setMinMaxPairs(50, 50).setMaxTranslations(5)
//				.setMinMaxPairs(null, 100).setMaxTranslations(10)
//				.setMinMaxPairs(100, 100).setMaxTranslations(10)
				;

		long start = StopWatch.nowMSecs();
		DictEvaluationResults results =
			evaluator.evaluate(Paths.get(glossaryPath), stopAfterN, (Integer)null);
//		long elapsed = StopWatch.elapsedMsecsSince(start);
//		double gotAvgSecs = elapsed / (1000.0 * results.totalIUPresent());
		AssertRuntime.runtimeHasNotChanged(
			results.avgSecsPerEntryPresent, 0.20,
			"avg secs for retrieving a dict entry", testInfo);

		new AssertDictEvaluationResults(results)
			.totalGlossaryEntries(stopAfterN)
			.totalSingleWordIUEntries(13)

			.totalIUPresent(WhatTerm.ORIGINAL, 6)
			.totalIUPresent(WhatTerm.RELATED, 2)

			.totalENSpotted(MatchType.STRICT, 3)
			.totalENSpotted(MatchType.LENIENT, 0)
			.totalENSpotted(MatchType.LENIENT_OVERLAP, 1)

			.totalENSpotted_atLeastInSense(MatchType.STRICT, 3)
			.totalENSpotted_atLeastInSense(MatchType.LENIENT, 3)
			.totalENSpotted_atLeastInSense(MatchType.LENIENT_OVERLAP,4)

			.rateENSpotted(MatchType.STRICT, 3.0/8)
			.rateENSpotted(MatchType.LENIENT, 3.0/8)
			.rateENSpotted(MatchType.LENIENT_OVERLAP, 4.0/8)
			;
	}

	@Test
	public void test_evaluateWordDict_OnWikipediaGlossary_AllEntries(
		TestInfo testInfo) throws Exception {
		String glossaryPath = IUConfig.getIUDataPath("data/glossaries/wpGlossary.json");
		DictEvaluator evaluator = new DictEvaluator()
			.setMinMaxPairs(null, 100)
			.setMaxTranslations(10);

		Integer stopAfterN = null;
		Integer startingAtN = null;
		DictEvaluationResults results =
			evaluator.evaluate(Paths.get(glossaryPath), stopAfterN, startingAtN);
		AssertRuntime.runtimeHasNotChanged(
			results.avgSecsPerEntryPresent, 0.20,
			"avg secs for retrieving a dict entry", testInfo);

		new AssertDictEvaluationResults(results)
			.totalGlossaryEntries(556)
			.totalSingleWordIUEntries(465)

			.totalIUPresent(WhatTerm.ORIGINAL, 185)
			.totalIUPresent(WhatTerm.RELATED, 76)

			.totalENSpotted(MatchType.STRICT, 87)
			.totalENSpotted(MatchType.LENIENT, 7)
			.totalENSpotted(MatchType.LENIENT_OVERLAP, 10)

			.totalENSpotted_atLeastInSense(MatchType.STRICT, 87)
			.totalENSpotted_atLeastInSense(MatchType.LENIENT, 94)
			.totalENSpotted_atLeastInSense(MatchType.LENIENT_OVERLAP, 104)

			.rateENSpotted(MatchType.STRICT, 0.333)
			.rateENSpotted(MatchType.LENIENT, 0.360)
			.rateENSpotted(MatchType.LENIENT_OVERLAP, 0.398)
			;
	}
}
