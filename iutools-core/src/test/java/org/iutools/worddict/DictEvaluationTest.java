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
		AssertRuntime.runtimeHasNotChanged(
			results.avgSecsPerEntryPresent, 1.545,
			"avg secs for retrieving a dict entry", testInfo);

		new AssertDictEvaluationResults(results)
			.totalGlossaryEntries(stopAfterN)
			.totalSingleWordIUEntries(13)

			.totalIUPresent(WhatTerm.ORIGINAL, 6)
			.totalIUPresent(WhatTerm.RELATED, 2)

			.totalENSpotted(MatchType.STRICT, 2)
			.totalENSpotted(MatchType.LENIENT, 0)
			.totalENSpotted(MatchType.LENIENT_OVERLAP, 1)

			.totalENSpotted_atLeastInSense(MatchType.STRICT, 2)
			.totalENSpotted_atLeastInSense(MatchType.LENIENT, 2)
			.totalENSpotted_atLeastInSense(MatchType.LENIENT_OVERLAP,3)

			.rateENSpotted(MatchType.STRICT, 2.0/8)
			.rateENSpotted(MatchType.LENIENT, 2.0/8)
			.rateENSpotted(MatchType.LENIENT_OVERLAP, 3.0/8)
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

			.totalIUPresent(WhatTerm.ORIGINAL, 183)
			.totalIUPresent(WhatTerm.RELATED, 75)

			.totalENSpotted(MatchType.STRICT, 92)
			.totalENSpotted(MatchType.LENIENT, 5)
			.totalENSpotted(MatchType.LENIENT_OVERLAP, 11)

			.totalENSpotted_atLeastInSense(MatchType.STRICT, 92)
			.totalENSpotted_atLeastInSense(MatchType.LENIENT, 97)
			.totalENSpotted_atLeastInSense(MatchType.LENIENT_OVERLAP, 108)

			.rateENSpotted(MatchType.STRICT, 0.357)
			.rateENSpotted(MatchType.LENIENT, 0.376)
			.rateENSpotted(MatchType.LENIENT_OVERLAP, 0.419)
			;
	}
}
