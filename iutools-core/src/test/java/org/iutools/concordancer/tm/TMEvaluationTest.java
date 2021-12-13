package org.iutools.concordancer.tm;

import ca.nrc.ui.commandline.UserIO;
import org.iutools.config.IUConfig;
import org.junit.Ignore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import org.iutools.concordancer.tm.TMEvaluator.MatchType;

public class TMEvaluationTest {

	@Test
	public void test_evaluateOnWikipediaGlossary_first20() throws Exception {
		String glossaryPath = IUConfig.getIUDataPath("data/glossaries/wpGlossary.json");
		int firstN = 20;
		EvaluationResults results =
			new TMEvaluator()
				.evaluate(Paths.get(glossaryPath), firstN);
		AssertEvaluationResults asserter = new AssertEvaluationResults(results);
		asserter
			.totalGlossaryEntries(firstN)
			.totalSingleIUTermEntries(13)

			.totaIUPresent(6)

			.totalENPresent_inSense(MatchType.STRICT, 4)
			.totalENPresent_inSense(MatchType.LENIENT, 0)
			.totalENPresent_inSense(MatchType.LENIENT_OVERLAP, 0)

			.totalENSpotted_inSense(MatchType.STRICT, 3)
			.totalENSpotted_inSense(MatchType.LENIENT, 0)
			.totalENSpotted_inSense(MatchType.LENIENT_OVERLAP, 1)

			.totalENPresent_atLeastInSense(MatchType.STRICT, 4)
			.totalENPresent_atLeastInSense(MatchType.LENIENT, 4)
			.totalENPresent_atLeastInSense(MatchType.LENIENT_OVERLAP, 4)

			.totalENSpotted_atLeastInSense(MatchType.STRICT, 3)
			.totalENSpotted_atLeastInSense(MatchType.LENIENT, 3)
			.totalENSpotted_atLeastInSense(MatchType.LENIENT_OVERLAP, 4)

			.rateENSpotted_inSense(MatchType.STRICT, 0.75)
			.rateENSpotted_inSense(MatchType.LENIENT, 0.75)
			.rateENSpotted_inSense(MatchType.LENIENT_OVERLAP, 1.0)
		;
	}

	@Test
	public void test_evaluateOnWikipediaGlossary_ALL() throws Exception {
		String glossaryPath = IUConfig.getIUDataPath("data/glossaries/wpGlossary.json");
		Integer firstN = null;
		EvaluationResults results =
			new TMEvaluator()
//				.focusOnWord("inuit (nunaqaqqaaqsimajut)")
				.evaluate(Paths.get(glossaryPath), firstN);
		new AssertEvaluationResults(results)
			.totalGlossaryEntries(556)
			.totalSingleIUTermEntries(464)

			.totaIUPresent(182)

			.totalENPresent_inSense(MatchType.STRICT, 93)
			.totalENPresent_inSense(MatchType.LENIENT, 3)
			.totalENPresent_inSense(MatchType.LENIENT_OVERLAP, 3)

			.totalENSpotted_inSense(MatchType.STRICT, 67)
			.totalENSpotted_inSense(MatchType.LENIENT, 4)
			.totalENSpotted_inSense(MatchType.LENIENT_OVERLAP, 2)

			.totalENPresent_atLeastInSense(MatchType.STRICT, 93)
			.totalENPresent_atLeastInSense(MatchType.LENIENT, 96)
			.totalENPresent_atLeastInSense(MatchType.LENIENT_OVERLAP, 99)

			.totalENSpotted_atLeastInSense(MatchType.STRICT, 67)
			.totalENSpotted_atLeastInSense(MatchType.LENIENT, 71)
			.totalENSpotted_atLeastInSense(MatchType.LENIENT_OVERLAP, 73)

			.rateENSpotted_inSense(MatchType.STRICT, 0.720)
			.rateENSpotted_inSense(MatchType.LENIENT, 0.739)
			.rateENSpotted_inSense(MatchType.LENIENT_OVERLAP, 0.737)
		;

	}

}
