package org.iutools.worddict;

import org.iutools.config.IUConfig;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

public class MultilingualDictEvaluationTest {

	@Test
	public void test__WikipediGlossaryWords() throws Exception {
		String glossaryPath = IUConfig.getIUDataPath("data/glossaries/wpGlossary.json");
		int firstN = 20;
		EvaluationResults results =
			new MultilingualDictEvaluator()
				.evaluate(Paths.get(glossaryPath), firstN);
		new AssertEvaluationResults(results)
			.totalEntries(firstN)
			.totalExactSpotOrig(1)
			.totalExactSpotRelated(1)
			.totalPartialSpotOrig(3)
			.totalPartialSpotRelated(2)
			.totalEntriesWithIU(7)
			.totalEntriesWithRelatedIU(6)
			;
	}
}
