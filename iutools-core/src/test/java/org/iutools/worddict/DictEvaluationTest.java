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
		AssertRuntime.runtimeHasNotChanged(
			results.avgSecsPerEntryPresent, 1.545,
			"avg secs for retrieving a dict entry", testInfo);

		int totalENSpotted_Strict = 2;
		int totalENSpotted_Lenient = 0;
		int totalENSpotted_LenientOverlap = 1;
		if (new IUConfig().tmDataStore().equals("sql")) {
			totalENSpotted_Lenient = 1;
		}
		int totalEnSpotted_AtLeastStrict = totalENSpotted_Strict;
		int totalEnSpotted_AtLeastLenient =
			totalENSpotted_Strict + totalENSpotted_Lenient;
		int totalEnSpotted_AtLeastLenientOverlap =
			totalENSpotted_Strict + totalENSpotted_Lenient +
			totalENSpotted_LenientOverlap;

		new AssertDictEvaluationResults(results)
			.totalGlossaryEntries(stopAfterN)
			.totalSingleWordIUEntries(13)

			.totalIUPresent(WhatTerm.ORIGINAL, 6)
			.totalIUPresent(WhatTerm.RELATED, 2)

			.totalENSpotted(MatchType.STRICT, totalENSpotted_Strict)
			.totalENSpotted(MatchType.LENIENT, totalENSpotted_Lenient)
			.totalENSpotted(MatchType.LENIENT_OVERLAP, totalENSpotted_LenientOverlap)

			.totalENSpotted_atLeastInSense(MatchType.STRICT, totalEnSpotted_AtLeastStrict)
			.totalENSpotted_atLeastInSense(MatchType.LENIENT, totalEnSpotted_AtLeastLenient)
			.totalENSpotted_atLeastInSense(MatchType.LENIENT_OVERLAP,totalEnSpotted_AtLeastLenientOverlap)

			.rateENSpotted(MatchType.STRICT, 1.0*totalEnSpotted_AtLeastStrict/8)
			.rateENSpotted(MatchType.LENIENT, 1.0*totalEnSpotted_AtLeastLenient/8)
			.rateENSpotted(MatchType.LENIENT_OVERLAP, 1.0*totalEnSpotted_AtLeastLenientOverlap/8)
			;
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
		AssertRuntime.runtimeHasNotChanged(
			results.avgSecsPerEntryPresent, 0.20,
			"avg secs for retrieving a dict entry", testInfo);

		int expTotalIUPresent = 183;
		int expTotalEnSpotted_Strict = 93;
		int expTotalEnSpotted_Lenient = 4;
		int expTotalEnSpotted_LenientOverlap = 11;
		if (new IUConfig().tmDataStore().equals("sql")) {
			// Some of the expectations are different for SQL
			expTotalIUPresent = 185;
			expTotalEnSpotted_Strict = 93;
			expTotalEnSpotted_Lenient = 3;
			expTotalEnSpotted_LenientOverlap = 13;
		}
		int expTotalEnSpotted_atLeastStrict = expTotalEnSpotted_Strict;
		int expTotalEnSpotted_atLeastLenient =
			expTotalEnSpotted_Strict + expTotalEnSpotted_Lenient;
		int expTotalEnSpotted_atLeastLenientOverlap =
			expTotalEnSpotted_Strict + expTotalEnSpotted_Lenient +
			expTotalEnSpotted_LenientOverlap;

		new AssertDictEvaluationResults(results)
			.totalGlossaryEntries(556)
			.totalSingleWordIUEntries(465)

			// For ES TM
			.totalIUPresent(WhatTerm.ORIGINAL, expTotalIUPresent)
			.totalIUPresent(WhatTerm.RELATED, 75)

			.totalENSpotted(MatchType.STRICT, expTotalEnSpotted_Strict)
			.totalENSpotted(MatchType.LENIENT, expTotalEnSpotted_Lenient)
			.totalENSpotted(MatchType.LENIENT_OVERLAP, expTotalEnSpotted_LenientOverlap)

			.totalENSpotted_atLeastInSense(MatchType.STRICT, expTotalEnSpotted_atLeastStrict)
			.totalENSpotted_atLeastInSense(MatchType.LENIENT, expTotalEnSpotted_atLeastLenient)
			.totalENSpotted_atLeastInSense(MatchType.LENIENT_OVERLAP, expTotalEnSpotted_atLeastLenientOverlap)

			.rateENSpotted(MatchType.STRICT, 0.360)
			.rateENSpotted(MatchType.LENIENT, 0.376)
			.rateENSpotted(MatchType.LENIENT_OVERLAP, 0.419)
			;
	}
}
