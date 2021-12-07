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
//				.setVerbosity(UserIO.Verbosity.Level2)
				.evaluate(Paths.get(glossaryPath), firstN);
		AssertEvaluationResults asserter = new AssertEvaluationResults(results);
		asserter
			.totalGlossaryEntries(firstN)

			.totaIUPresent(7)

			.totalENPresent_inSense(MatchType.STRICT, 5)
			.totalENPresent_inSense(MatchType.LENIENT, 0)
			.totalENPresent_inSense(MatchType.LENIENT_OVERLAP, 0)

			.totalENSpotted_inSense(MatchType.STRICT, 4)
			.totalENSpotted_inSense(MatchType.LENIENT, 0)
			.totalENSpotted_inSense(MatchType.LENIENT_OVERLAP, 1)

			.totalENPresent_atLeastInSense(MatchType.STRICT, 5)
			.totalENPresent_atLeastInSense(MatchType.LENIENT, 5)
			.totalENPresent_atLeastInSense(MatchType.LENIENT_OVERLAP, 5)

			.totalENSpotted_atLeastInSense(MatchType.STRICT, 4)
			.totalENSpotted_atLeastInSense(MatchType.LENIENT, 4)
			.totalENSpotted_atLeastInSense(MatchType.LENIENT_OVERLAP, 5)

			.rateENSpotted_inSense(MatchType.STRICT, 0.8)
			.rateENSpotted_inSense(MatchType.LENIENT, 0.8)
			.rateENSpotted_inSense(MatchType.LENIENT_OVERLAP, 1.0)
		;
	}

	@Test
	public void test_evaluateOnWikipediaGlossary_ALL() throws Exception {
		String glossaryPath = IUConfig.getIUDataPath("data/glossaries/wpGlossary.json");
		Integer firstN = null;
		EvaluationResults results =
			new TMEvaluator()
				.evaluate(Paths.get(glossaryPath), firstN);
		new AssertEvaluationResults(results)
			.totalGlossaryEntries(556)

			.totaIUPresent(190)

			.totalENPresent_inSense(MatchType.STRICT, 99)
			.totalENPresent_inSense(MatchType.LENIENT, 3)
			.totalENPresent_inSense(MatchType.LENIENT_OVERLAP, 3)

			.totalENSpotted_inSense(MatchType.STRICT, 69)
			.totalENSpotted_inSense(MatchType.LENIENT, 4)
			.totalENSpotted_inSense(MatchType.LENIENT_OVERLAP, 2)

			.totalENPresent_atLeastInSense(MatchType.STRICT,99)
			.totalENPresent_atLeastInSense(MatchType.LENIENT, 102)
			.totalENPresent_atLeastInSense(MatchType.LENIENT_OVERLAP, 105)

			.totalENSpotted_atLeastInSense(MatchType.STRICT, 69)
			.totalENSpotted_atLeastInSense(MatchType.LENIENT, 73)
			.totalENSpotted_atLeastInSense(MatchType.LENIENT_OVERLAP, 75)

			.rateENSpotted_inSense(MatchType.STRICT, 0.697)
			.rateENSpotted_inSense(MatchType.LENIENT, 0.715)
			.rateENSpotted_inSense(MatchType.LENIENT_OVERLAP, 0.714)
		;

	}

}
