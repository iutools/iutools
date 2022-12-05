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
		String glossaryPath = IUConfig.getIUDataPath("data/glossaries/wpGlossary.gloss.json");

		int firstN = 20;
		EvaluationResults results =
			new TMEvaluator((Path)null, wordAlignsPath)
				.evaluate(Paths.get(glossaryPath), firstN);

		int expEntries = 20;
		int expSingleIUWordEntries = 12;
		int expIUPresent = 7;

		int expEnPresent_Strict = 6;
		int expEnPresent_Lenient = 0;
		int expEnPresent_LenientOverlap = 0;

		int expEnSpotted_Strict = 5;
		int expEnSpotted_Lenient = 0;
		int expEnSpotted_LenientOverlap = 1;

		int expEnPresent_atLeastStrict = expEnPresent_Strict;
		int expEnPresent_atLeastLenient =
			expEnPresent_Strict + expEnPresent_Lenient;
		int expEnPresent_atLeastLenientOverlap =
			expEnPresent_Strict + expEnPresent_Lenient +
			expEnPresent_LenientOverlap;

		int expEnSpotted_atLeastStrict = expEnSpotted_Strict;
		int expEnSpotted_atLeastLenient =
			expEnSpotted_Strict + expEnSpotted_Lenient;
		int expEnSpotted_atLeastLenientOverlap =
			expEnSpotted_Strict + expEnSpotted_Lenient +
			expEnSpotted_LenientOverlap;


		AssertEvaluationResults asserter = new AssertEvaluationResults(results);
		new AssertEvaluationResults(results)
			.totalGlossaryEntries(expEntries)
			.totalSingleIUTermEntries(expSingleIUWordEntries)

			.totaIUPresent(expIUPresent)

			.totalENPresent_inSense(MatchType.STRICT, expEnPresent_Strict)
			.totalENPresent_inSense(MatchType.LENIENT, expEnPresent_Lenient)
			.totalENPresent_inSense(MatchType.LENIENT_OVERLAP, expEnPresent_LenientOverlap)

			.totalENSpotted_inSense(MatchType.STRICT, expEnSpotted_Strict)
			.totalENSpotted_inSense(MatchType.LENIENT, expEnSpotted_Lenient)
			.totalENSpotted_inSense(MatchType.LENIENT_OVERLAP, expEnSpotted_LenientOverlap)

			.totalENPresent_atLeastInSense(MatchType.STRICT, expEnPresent_atLeastStrict)
			.totalENPresent_atLeastInSense(MatchType.LENIENT, expEnPresent_atLeastLenient)
			.totalENPresent_atLeastInSense(MatchType.LENIENT_OVERLAP, expEnPresent_atLeastLenientOverlap)

			.totalENSpotted_atLeastInSense(MatchType.STRICT, expEnSpotted_atLeastStrict)
			.totalENSpotted_atLeastInSense(MatchType.LENIENT, expEnSpotted_atLeastLenient)
			.totalENSpotted_atLeastInSense(MatchType.LENIENT_OVERLAP, expEnSpotted_atLeastLenientOverlap)

			.rateENSpotted_inSense(MatchType.STRICT, 0.833)
			.rateENSpotted_inSense(MatchType.LENIENT, 0.833)
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
		String glossaryPath = IUConfig.getIUDataPath("data/glossaries/wpGlossary.gloss.json");
		Integer firstN = null;
		EvaluationResults results =
			new TMEvaluator(sentPairsFile, wordAlignsPath)
//				.focusOnWord("immuk")
				.evaluate(Paths.get(glossaryPath), firstN);

		int expEntries = 551;
		int expSingleIUWordEntries = 460;
		int expIUPresent = 183;

		int expEnPresent_Strict = 106;
		int expEnPresent_Lenient = 1;
		int expEnPresent_LenientOverlap = 3;

		int expEnSpotted_Strict = 89;
		int expEnSpotted_Lenient = 1;
		int expEnSpotted_LenientOverlap = 7;

		int expEnPresent_atLeastStrict = expEnPresent_Strict;
		int expEnPresent_atLeastLenient =
			expEnPresent_Strict + expEnPresent_Lenient;
		int expEnPresent_atLeastLenientOverlap =
			expEnPresent_Strict + expEnPresent_Lenient +
			expEnPresent_LenientOverlap;

		int expEnSpotted_atLeastStrict = expEnSpotted_Strict;
		int expEnSpotted_atLeastLenient =
			expEnSpotted_Strict + expEnSpotted_Lenient;
		int expEnSpotted_atLeastLenientOverlap =
			expEnSpotted_Strict + expEnSpotted_Lenient +
			expEnSpotted_LenientOverlap;

		new AssertEvaluationResults(results)
			.totalGlossaryEntries(expEntries)
			.totalSingleIUTermEntries(expSingleIUWordEntries)

			.totaIUPresent(expIUPresent)

			.totalENPresent_inSense(MatchType.STRICT, expEnPresent_Strict)
			.totalENPresent_inSense(MatchType.LENIENT, expEnPresent_Lenient)
			.totalENPresent_inSense(MatchType.LENIENT_OVERLAP, expEnPresent_LenientOverlap)

			.totalENSpotted_inSense(MatchType.STRICT, expEnSpotted_Strict)
			.totalENSpotted_inSense(MatchType.LENIENT, expEnSpotted_Lenient)
			.totalENSpotted_inSense(MatchType.LENIENT_OVERLAP, expEnSpotted_LenientOverlap)

			.totalENPresent_atLeastInSense(MatchType.STRICT, expEnPresent_atLeastStrict)
			.totalENPresent_atLeastInSense(MatchType.LENIENT, expEnPresent_atLeastLenient)
			.totalENPresent_atLeastInSense(MatchType.LENIENT_OVERLAP, expEnPresent_atLeastLenientOverlap)

			.totalENSpotted_atLeastInSense(MatchType.STRICT, expEnSpotted_atLeastStrict)
			.totalENSpotted_atLeastInSense(MatchType.LENIENT, expEnSpotted_atLeastLenient)
			.totalENSpotted_atLeastInSense(MatchType.LENIENT_OVERLAP, expEnSpotted_atLeastLenientOverlap)

			.rateENSpotted_inSense(MatchType.STRICT, 0.840)
			.rateENSpotted_inSense(MatchType.LENIENT, 0.841)
			.rateENSpotted_inSense(MatchType.LENIENT_OVERLAP, 0.882)
		;

	}

}
