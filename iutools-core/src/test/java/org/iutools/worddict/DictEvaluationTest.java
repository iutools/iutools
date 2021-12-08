package org.iutools.worddict;

import org.iutools.concordancer.tm.TMEvaluator;
import org.iutools.config.IUConfig;
import org.iutools.worddict.MultilingualDict.*;
import org.iutools.concordancer.tm.TMEvaluator.MatchType;
import org.junit.jupiter.api.Disabled;
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

			.totalIUPresent(WhatTerm.ORIGINAL, 7)
			.totalIUPresent(WhatTerm.RELATED, 2)

			.totalENSpotted(MatchType.STRICT, 3)
			.totalENSpotted(MatchType.LENIENT, 0)
			.totalENSpotted(MatchType.LENIENT_OVERLAP, 0)

			.totalENSpotted_atLeastInSense(MatchType.STRICT, 3)
			.totalENSpotted_atLeastInSense(MatchType.LENIENT, 3)
			.totalENSpotted_atLeastInSense(MatchType.LENIENT_OVERLAP,3)

			.rateENSpotted(MatchType.STRICT, 3.0/9)
			.rateENSpotted(MatchType.LENIENT, 3.0/9)
			.rateENSpotted(MatchType.LENIENT_OVERLAP, 3.0/9)
			;
	}

	@Test
	public void test_evaluateWordDict_OnWikipediaGlossary_AllEntries() throws Exception {
		String glossaryPath = IUConfig.getIUDataPath("data/glossaries/wpGlossary.json");
		DictEvaluationResults results =
		new DictEvaluator()
			.evaluate(Paths.get(glossaryPath));

		new AssertDictEvaluationResults(results)
			.totalGlossaryEntries(556)

			.totalIUPresent(WhatTerm.ORIGINAL, 192)
			.totalIUPresent(WhatTerm.RELATED, 77)

			.totalENSpotted(MatchType.STRICT, 78)
			.totalENSpotted(MatchType.LENIENT, 4)
			.totalENSpotted(MatchType.LENIENT_OVERLAP, 3)

			.totalENSpotted_atLeastInSense(MatchType.STRICT, 78)
			.totalENSpotted_atLeastInSense(MatchType.LENIENT, 82)
			.totalENSpotted_atLeastInSense(MatchType.LENIENT_OVERLAP, 85)

			.rateENSpotted(MatchType.STRICT, 0.290)
			.rateENSpotted(MatchType.LENIENT, 0.304)
			.rateENSpotted(MatchType.LENIENT_OVERLAP, 0.316)
			;

	}

}
