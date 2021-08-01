package org.iutools.morphrelatives;

import java.io.File;

import ca.nrc.file.ResourceGetter;
import org.iutools.corpus.CompiledCorpus;
import org.iutools.corpus.CompiledCorpusRegistry;
import org.junit.jupiter.api.*;

import static org.junit.Assert.assertFalse;

import org.junit.jupiter.api.TestInfo;

public class MorphRelativesFinderAccuracyTest {

	@Test
	public void test__findRelatives__QuickPerformanceTest(TestInfo testInfo)
		throws Exception {

		RelatedWordsExperiment experiment =
			new RelatedWordsExperiment()
				.setCorpusName("HANSARD-1999-2002")

				// Set this to a single word if you only want to run that one word
//				.setFocusOnWord("mikinniqsanut")

				// Set this to true if you want to see more details about the
				// words being evaluated and their results.
				.setVerbosity(false)
				;

		evaluatePerformance(experiment, 10, testInfo);
	}

	@Test
	public void test__findRelatives__PerformanceTest(TestInfo testInfo)
	throws Exception {

		RelatedWordsExperiment experiment = new RelatedWordsExperiment()
			.setCorpusName("HANSARD-1999-2002")
			.setVerbosity(false)

			// Set this to a single word if you only want to run that one word
//			.setFocusOnWord("attarnaqtumi")

			// Set this to true if you want to see more details about the
			// words being evaluated and their results.
			.setVerbosity(false)
			;
		evaluatePerformance(experiment, testInfo);
	}

	private void evaluatePerformance(RelatedWordsExperiment exp,
		TestInfo testInfo) throws Exception {
		evaluatePerformance(exp, (Integer)null, testInfo);
	}

	private void evaluatePerformance(RelatedWordsExperiment exp,
		Integer stopAfterNWords, TestInfo testInfo) throws Exception {

		File goldStandardCSVFilePath =
			ResourceGetter.copyResourceToTempLocation("org/iutools/IU100Words-expansions-added-to-alternatives.csv");

		// This is the NEW version of the corpus, which has some
		// non-empty decomp samples
		//
		CompiledCorpus corpus =
			new CompiledCorpusRegistry()
				.getCorpus(exp.corpusName);

		MorphRelativesFinder finder = new MorphRelativesFinder(corpus);

		MorphRelativesFinderEvaluator evaluator =
			new MorphRelativesFinderEvaluator(
				finder, goldStandardCSVFilePath);

		// If focusOnWord != null, we run the evaluator verbosely
		// Otherwise, use the verbosity level provided in the expectations
		boolean verbose = (exp.verbose || exp.focusOnWord != null);
		evaluator.setVerbose(verbose);

		evaluator.setStopAfterNWords(stopAfterNWords);

		evaluator.run(exp, testInfo);
	}
}
