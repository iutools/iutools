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

			.totalENPresent_inSense(null, 15)
			.totalENPresent_inSense(MatchType.STRICT, 4)
			.totalENPresent_inSense(MatchType.LENIENT, 0)
			.totalENPresent_inSense(MatchType.LENIENT_OVERLAP, 1)

			.totalENSpotted_inSense(null, 15)
			.totalENSpotted_inSense(MatchType.STRICT, 4)
			.totalENSpotted_inSense(MatchType.LENIENT, 1)
			.totalENSpotted_inSense(MatchType.LENIENT_OVERLAP, 0)

			.totalENPresent_atLeastInSense(MatchType.STRICT, 4)
			.totalENPresent_atLeastInSense(MatchType.LENIENT, 4)
			.totalENPresent_atLeastInSense(MatchType.LENIENT_OVERLAP, 5)

			.totalENSpotted_atLeastInSense(MatchType.STRICT, 4)
			.totalENSpotted_atLeastInSense(MatchType.LENIENT, 5)
			.fail("Why is the total of lenient SPOTTING is greater that lenient PRESENT???")

//			.totalENSpotted_atLeastInSense(MatchType.LENIENT_OVERLAP, 5)
//
//			.rateENSpotted_inSense(MatchType.STRICT, 1.0)
//			.rateENSpotted_inSense(MatchType.LENIENT, 5.0/4.0)
//			.rateENSpotted_inSense(MatchType.LENIENT_OVERLAP, 1.0)

		;



//			.rateENSpotted_Strict(1.0)
//			.rateENSpotted_Lenient(0.4)
//			.rateENSpotted_LenientOverlap(-2)
		;
	}

	@Test @Disabled
	public void test_evaluateOnWikipediaGlossary_ALL() throws Exception {
		String glossaryPath = IUConfig.getIUDataPath("data/glossaries/wpGlossary.json");
		Integer firstN = null;
		EvaluationResults results =
			new TMEvaluator()
				.evaluate(Paths.get(glossaryPath), firstN);
		new AssertEvaluationResults(results)
			.totalGlossaryEntries(556)
			.totaIUPresent(190)
			.totalENPresent_Strict(55)
			.totalENPresent_Lenient(104)
			.totalENSpotted_Strict(48)
			.totalENSpotted_Lenient(69)
			.totalENSpotted_LenientOverlap(-2)
			.rateENSpotted_Strict(0.872)
			.rateENSpotted_Lenient(0.663)
			.rateENSpotted_LenientOverlap(-2)
		;
	}

}
