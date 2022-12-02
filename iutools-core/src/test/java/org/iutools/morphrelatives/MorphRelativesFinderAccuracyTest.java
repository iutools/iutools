package org.iutools.morphrelatives;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import ca.nrc.file.ResourceGetter;
import org.iutools.corpus.CompiledCorpus;
import org.iutools.corpus.CompiledCorpusRegistry;
import org.iutools.corpus.elasticsearch.CompiledCorpus_ES;
import org.iutools.corpus.sql.CompiledCorpus_SQL;
import org.iutools.sql.SQLLeakMonitor;
import org.iutools.sql.SQLTestHelpers;
import org.iutools.utilities.StopWatch;
import org.junit.jupiter.api.*;


import org.junit.jupiter.api.TestInfo;

public class MorphRelativesFinderAccuracyTest {

	SQLLeakMonitor sqlLeakMonitor = null;

	@BeforeEach
	public void setUp() throws Exception {
		sqlLeakMonitor = new SQLLeakMonitor();
	}

	@AfterEach
	public void tearDown() {
		sqlLeakMonitor.assertNoLeaks();
	}

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
//			.setFocusOnWord("aaqqigiaqsinirmit")

			// Set this to true if you want to see more details about the
			// words being evaluated and their results.
			.setVerbosity(false)
			;
		evaluatePerformance(experiment, testInfo);
	}

	// Don't worry about SQL vs ES. We are now committed to using SQL for the
	// CompiledCorpus
	@Test @Disabled
	public void test__SpeedComparison__SQLvsES(TestInfo testInfo) throws Exception {
		CompiledCorpus_ES esCorpus = new CompiledCorpus_ES(CompiledCorpusRegistry.defaultCorpusName);
		CompiledCorpus_SQL sqlCorpus = new CompiledCorpus_SQL(CompiledCorpusRegistry.defaultCorpusName);
		Map<String,Double> times = new HashMap<String,Double>();
		times.put("es", time__find(esCorpus, testInfo));
		times.put("sql", time__find(sqlCorpus, testInfo));
		Double tolerance = 0.3;
		SQLTestHelpers.assertSqlNotSignificantlySlowerThanES("find relatives", times,
		tolerance);
	}


	/////////////////////////////////////////////////
	// TEST HELPERS
	/////////////////////////////////////////////////

	private Double time__find(CompiledCorpus corpus, TestInfo testInfo) throws Exception {
		RelatedWordsExperiment experiment = new RelatedWordsExperiment()
			.setVerbosity(false)
			;
		StopWatch sw = new StopWatch().start();
		evaluatePerformance(experiment, 20, testInfo, corpus, true);
		return 1.0 * sw.lapTime(TimeUnit.MILLISECONDS);
	}


	private void evaluatePerformance(RelatedWordsExperiment exp,
		TestInfo testInfo) throws Exception {
		evaluatePerformance(exp, (Integer)null, testInfo, (CompiledCorpus)null,
			(Boolean)null);
	}

	private void evaluatePerformance(RelatedWordsExperiment exp,
		Integer stopAfterNWords, TestInfo testInfo) throws Exception {
		evaluatePerformance(exp, stopAfterNWords, testInfo, (CompiledCorpus)null,
			(Boolean)null);
	}

	private void evaluatePerformance(RelatedWordsExperiment exp,
		Integer stopAfterNWords, TestInfo testInfo, CompiledCorpus corpus,
		Boolean onlyMeasureSpeed) throws Exception {

		if (onlyMeasureSpeed == null) {
			onlyMeasureSpeed = false;
		}
		if (corpus == null) {
			corpus =
				new CompiledCorpusRegistry()
					.getCorpus(exp.corpusName);
		}

		System.out.println("** Relative Finder uses a corpus of class: "+corpus.getClass().getSimpleName()+"\n");
		File goldStandardCSVFilePath =
			ResourceGetter.copyResourceToTempLocation("org/iutools/IU100Words-expansions-added-to-alternatives.csv");

		MorphRelativesFinder finder = new MorphRelativesFinder(corpus);

		MorphRelativesFinderEvaluator evaluator =
			new MorphRelativesFinderEvaluator(
				finder, goldStandardCSVFilePath, onlyMeasureSpeed);

		// If focusOnWord != null, we run the evaluator verbosely
		// Otherwise, use the verbosity level provided in the expectations
		boolean verbose = (exp.verbose || exp.focusOnWord != null);
		evaluator.setVerbose(verbose);

		evaluator.setStopAfterNWords(stopAfterNWords);

		evaluator.run(exp, testInfo);
	}
}
