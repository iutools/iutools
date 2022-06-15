package org.iutools.concordancer.tm;

import ca.nrc.config.ConfigException;
import ca.nrc.testing.TestDirs;
import org.iutools.config.IUConfig;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.iutools.concordancer.tm.TMEvaluator.MatchType;
import org.junit.jupiter.api.TestInfo;

public class TMEvaluationTest {

	public static Path wordAlignsPath;

	static {
		try {
			wordAlignsPath = Paths.get(IUConfig.getIUDataPath("data/translation-memories/testdata/fastalign.NunavutHansard-unique.bpe-j-15000.lc.json"));
		} catch (ConfigException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void test_evaluateOnWikipediaGlossary_first20() throws Exception {
		String glossaryPath = IUConfig.getIUDataPath("data/glossaries/wpGlossary.json");

		int firstN = 20;
		EvaluationResults results =
			new TMEvaluator((Path)null, wordAlignsPath)
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

	/**
	 * Besides evaluating the TM on the WP glossary terms, this test also
	 * produces a json file that contains all the sentence pairs whose word
	 * alignement might impact  the evaulation of the TranslationSpotter on the WP glossary.
	 *
	 * We can use these sentence pairs to evaluate different word alignment
	 * algorithms outside of the IUTools framework.
	 *
	 * The path of the JSON file is printed at the end of the test.
	 */
	@Test
	public void test_evaluateOnWikipediaGlossary_ALL(TestInfo testInfo) throws Exception {
		Path sentPairsFile = new TestDirs(testInfo).outputsFile("sentencePairs.json");
		String glossaryPath = IUConfig.getIUDataPath("data/glossaries/wpGlossary.json");
		Integer firstN = null;
		EvaluationResults results =
			new TMEvaluator(sentPairsFile, wordAlignsPath)
//				.focusOnWord("immuk")
				.evaluate(Paths.get(glossaryPath), firstN);
		new AssertEvaluationResults(results)
			.totalGlossaryEntries(556)
			.totalSingleIUTermEntries(466)

			.totaIUPresent(184)

			.totalENPresent_inSense(MatchType.STRICT, 106)
			.totalENPresent_inSense(MatchType.LENIENT, 1)
			.totalENPresent_inSense(MatchType.LENIENT_OVERLAP, 3)

			.totalENSpotted_inSense(MatchType.STRICT, 88)
			.totalENSpotted_inSense(MatchType.LENIENT, 1)
			.totalENSpotted_inSense(MatchType.LENIENT_OVERLAP, 8)

			.totalENPresent_atLeastInSense(MatchType.STRICT, 106)
			.totalENPresent_atLeastInSense(MatchType.LENIENT, 107)
			.totalENPresent_atLeastInSense(MatchType.LENIENT_OVERLAP, 110)

			.totalENSpotted_atLeastInSense(MatchType.STRICT, 88)
			.totalENSpotted_atLeastInSense(MatchType.LENIENT, 89)
			.totalENSpotted_atLeastInSense(MatchType.LENIENT_OVERLAP, 97)

			.rateENSpotted_inSense(MatchType.STRICT, 0.830)
			.rateENSpotted_inSense(MatchType.LENIENT, 0.832)
			.rateENSpotted_inSense(MatchType.LENIENT_OVERLAP, 0.882)
		;

	}

}
