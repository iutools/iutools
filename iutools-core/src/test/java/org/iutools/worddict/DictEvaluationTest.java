package org.iutools.worddict;

import org.iutools.config.IUConfig;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

public class DictEvaluationTest {

	@Test
	public void test_evaluateWordDict_OnWikipediaGlossary_first20() throws Exception {
		String glossaryPath = IUConfig.getIUDataPath("data/glossaries/wpGlossary.json");
		int firstN = 20;
		DictEvaluationResults results =
			new DictEvaluator()
				.evaluate(Paths.get(glossaryPath), firstN);
		new AssertDictEvaluationResults(results)
			.totalGlossaryEntries(firstN)
			.totalIUPresent(false, -999)
			.totalIUPresent(true, -999)
			;
//		.totaIUPresent(7)
//
//		.totalENPresent_inSense(TMEvaluator.MatchType.STRICT, 5)
//		.totalENPresent_inSense(TMEvaluator.MatchType.LENIENT, 0)
//		.totalENPresent_inSense(TMEvaluator.MatchType.LENIENT_OVERLAP, 0)
//
//		.totalENSpotted_inSense(TMEvaluator.MatchType.STRICT, 4)
//		.totalENSpotted_inSense(TMEvaluator.MatchType.LENIENT, 0)
//		.totalENSpotted_inSense(TMEvaluator.MatchType.LENIENT_OVERLAP, 1)
//
//		.totalENPresent_atLeastInSense(TMEvaluator.MatchType.STRICT, 5)
//		.totalENPresent_atLeastInSense(TMEvaluator.MatchType.LENIENT, 5)
//		.totalENPresent_atLeastInSense(TMEvaluator.MatchType.LENIENT_OVERLAP, 5)
//
//		.totalENSpotted_atLeastInSense(TMEvaluator.MatchType.STRICT, 4)
//		.totalENSpotted_atLeastInSense(TMEvaluator.MatchType.LENIENT, 4)
//		.totalENSpotted_atLeastInSense(TMEvaluator.MatchType.LENIENT_OVERLAP, 5)
//
//		.rateENSpotted_inSense(TMEvaluator.MatchType.STRICT, 0.8)
//		.rateENSpotted_inSense(TMEvaluator.MatchType.LENIENT, 0.8)
//		.rateENSpotted_inSense(TMEvaluator.MatchType.LENIENT_OVERLAP, 1.0)
//		;

	}

	@Test
	public void test_evaluateWordDict_OnWikipediaGlossary_AllEntries() throws Exception {
		String glossaryPath = IUConfig.getIUDataPath("data/glossaries/wpGlossary.json");
		int firstN = 20;
		DictEvaluationResults results =
		new DictEvaluator()
			.evaluate(Paths.get(glossaryPath));
	}

}
