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
			.totalENPresent_Orig_Strict(2)
			.totalENPresent_Orig_Lenient(5)
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
			.totalENPresent_Orig_Strict(56)
			.totalENPresent_Orig_Lenient(105)
		;
	}

}
