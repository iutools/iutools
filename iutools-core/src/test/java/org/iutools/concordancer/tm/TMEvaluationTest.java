package org.iutools.concordancer.tm;

import org.iutools.config.IUConfig;
import org.iutools.worddict.EvaluationResults;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

public class TMEvaluationTest {

	@Test
	public void test_evaluateOnWikipediaGlossary() throws Exception {
		String glossaryPath = IUConfig.getIUDataPath("data/glossaries/wpGlossary.json");
		int firstN = 20;
		EvaluationResults results =
			new TMEvaluator()
				.evaluate(Paths.get(glossaryPath), firstN);
		new AssertEvaluationResults(results)
			.totalGlossaryEntries(firstN)
			.totalENPresent_Orig_Strict(5)
			.totalENPresent_Orig_Lenient(7)
			.totalOnlyIUPresent_Orig(297)
//
//			.
			;
	}
}
