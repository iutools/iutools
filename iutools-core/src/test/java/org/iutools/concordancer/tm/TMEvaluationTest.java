package org.iutools.concordancer.tm;

import org.iutools.config.IUConfig;
import org.iutools.worddict.EvaluationResults;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

public class TMEvaluationTest {

	@Test
	public void test_evaluateOnWikipediaGlossary_first20() throws Exception {
		String glossaryPath = IUConfig.getIUDataPath("data/glossaries/wpGlossary.json");
		int firstN = 20;
		EvaluationResults results =
			new TMEvaluator()
				.evaluate(Paths.get(glossaryPath), firstN);
		new AssertEvaluationResults(results)
			.totalGlossaryEntries(firstN)
			.totaIUPresent_Orig(7)
			.totalENPresent_Strict(2)
			.totalENPresent_Lenient(5)
			.totalENSpotted_Strict(2)
			.totalENSpotted_Lenient(2)
			.totalENSpotted_LenientOverlap(2)
			.rateENSpotted_Strict(1.0)
			.rateENSpotted_Lenient(0.4)
			.rateENSpotted_LenientOverlap(2)
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
			.totaIUPresent_Orig(190)
			.totalENPresent_Strict(55)
			.totalENPresent_Lenient(104)
			.totalENSpotted_Strict(48)
			.totalENSpotted_Lenient(69)
			.totalENSpotted_LenientOverlap(2)
			.rateENSpotted_Strict(0.872)
			.rateENSpotted_Lenient(0.663)
			.rateENSpotted_LenientOverlap(2)
		;
	}

}
