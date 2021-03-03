package org.iutools.morphrelatives;

import java.io.File;

import ca.nrc.file.ResourceGetter;
import ca.nrc.testing.AssertNumber;
import ca.nrc.testing.AssertRuntime;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.iutools.corpus.CompiledCorpus;
import org.iutools.corpus.CompiledCorpusRegistry;
import org.junit.Assert;
import org.junit.jupiter.api.*;

import static org.junit.Assert.assertFalse;

import org.iutools.config.IUConfig;
import org.junit.jupiter.api.TestInfo;

public class MorphRelativesFinderAccuracyTest {

	@Test
	public void test__findRelatives__NewQuickPerformanceTest(TestInfo testInfo)
		throws Exception {
				PerformanceExpectations expectations =
			new PerformanceExpectations()
				.setCorpusName("HANSARD-1999-2002")

				// Set this to a single word if you only want to run that one word
//				.setFocusOnWord("mikinniqsanut")

				// The avg runtime per morpheme should not change by more than 30%
				.setRuntimePercTolerance(0.30)

				// The Precision and recall should not change by more than 2%
				.setPrecRecTolerance(0.02)

				// Set this to true if you want to see more details about the
				// words being evaluated and their results.
				.setVerbosity(false)

				;

		evaluatePerformance(expectations, 10, testInfo);
	}

	@Test
	public void test__findRelatives__NewPerformanceTest(TestInfo testInfo)
	throws Exception {
		PerformanceExpectations expectations =
		new PerformanceExpectations()
		.setCorpusName("HANSARD-1999-2002")
		.setVerbosity(false)

		// Set this to a single word if you only want to run that one word
//				.setFocusOnWord("mikinniqsanut")

		// The avg runtime per morpheme should not change by more than 30%
		.setRuntimePercTolerance(0.30)

		// The Precision and recall should not change by more than 2%
		.setPrecRecTolerance(0.02)

		// Set this to true if you want to see more details about the
		// words being evaluated and their results.
		.setVerbosity(false)

		;
		evaluatePerformance(expectations, testInfo);
	}

	@Test
	public void test_TODO1() {
		Assertions.fail("Make sure that dump_corpus without arguments will dump the default corpus to the iutools-data location\n");
	}

	@Test
	public void test_TODO2() {
		Assertions.fail("Make sure that recompile_corpus command will recompute the decomps of all words in the corpus, and dump the new version of the corpus to ituools-data\n");
	}

	@Test
	public void test__MorphRelativesFinderAccuracy(TestInfo testInfo) throws Exception {
		
		System.out.println("\n\n*** Running test__MorphRelativesFinderAccuracy.\nThis test can take a few minutes to complete\n\n");;
		
		PerformanceExpectations expectations =
			new PerformanceExpectations()

			.setCorpusName("HANSARD-1999-2002")

			.setComputeStatsOverSurfaceForms(true)

			// OLD expectations
			// 2020-10-28-AD:
			//   This was the accuracy before we moved to the new version
			//   of the ES corpus with the actual sample of decomps.
			//   Accuracy decrease because the RelativesFinder produces new
			//	 morph relatives which Benoit never got a chance to inspect
			// 	 and validate. In other words, the Gold Standard for the
			//   relatives finder is missing many of those new relatives,
			//   eventhough they are good.
			//
			//   For now, just use the lowered expectations and accept the
			//   fact that the Gold Standard underestimates the accuracy
			//
//			.setTargetPrecision(0.6314)
//			.setTargetRecall(0.4707)

			// NEW (underestimated) expectations
			.setTargetPrecision(0.45)
			.setTargetRecall(0.33)

			.setPrecRecTolerance(0.015)
			// Average runtime for morphemes should not change by more than 30%
			.setRuntimePercTolerance(0.30)

			.setVerbosity(false)
			;

		evaluateAccuracyAndSpeed(expectations, testInfo);
	}

	private void evaluatePerformance(PerformanceExpectations exp,
		TestInfo testInfo) throws Exception {
		evaluatePerformance(exp, (Integer)null, testInfo);
	}

	private void evaluatePerformance(PerformanceExpectations exp,
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

		// whether statistics are to be computed over words (default [true]) or morphemes [false]:
		evaluator
			.setOptionComputeStatsOverSurfaceForms(exp.computeStatsOverSurfaceForms);
		evaluator.setStopAfterNWords(stopAfterNWords);
		evaluator.setFocusOnWord(exp.focusOnWord);

		evaluator.runNew(exp, testInfo);

	}


	private void evaluateAccuracyAndSpeed(
		PerformanceExpectations exp, TestInfo testInfo) throws Exception {
		evaluateAccuracyAndSpeed(exp, (Integer)null, testInfo);
	}

	private void evaluateAccuracyAndSpeed(PerformanceExpectations exp,
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

		// whether statistics are to be computed over words (default [true]) or morphemes [false]:
		evaluator
			.setOptionComputeStatsOverSurfaceForms(exp.computeStatsOverSurfaceForms);
		evaluator.setStopAfterNWords(stopAfterNWords);
		evaluator.setFocusOnWord(exp.focusOnWord);

		boolean precisionFine = true;
		boolean recallFine = true;
		boolean runtimeFine = true;

		double gotPrecision = -1.0;
		double gotRecall = -1.0;

		String diagnostic = "";

		long startMSecs = System.currentTimeMillis();
		evaluator.run();

//
//
//		// targetPrecision < 0 means we don't care about precision
//		double precDelta= -1.0; double precDeltaAbs = 1.0;
//		if (exp.targetPrecision > 0) {
//			gotPrecision = (double) evaluator.precision;
//			precDelta = gotPrecision - exp.targetPrecision;
//			precDeltaAbs = Math.abs(precDelta);
//			if (precDeltaAbs > exp.precRecTolerance) {
//				precisionFine = false;
//			}
//		}
//
//		double recDelta=-1.0; double recDeltaAbs=1.0;
//
//		// targetRecall < 0 means we don't care about recall
//		if (exp.targetRecall > 0) {
//			gotRecall = (double) evaluator.recall;
//			recDelta = gotRecall - exp.targetRecall;
//			recDeltaAbs = Math.abs(recDelta);
//			if (recDeltaAbs > exp.precRecTolerance) {
//				recallFine = false;
//			}
//		}
//
//		if (!precisionFine || !recallFine || !runtimeFine) {
//			if ( !precisionFine ) {
//				if (precDelta < 0) {
//					diagnostic += "\nPRECISION: "+"<<< The precision has gone ***DOWN*** by "+precDeltaAbs+". Was "+exp.targetPrecision+"; now "+gotPrecision;
//				} else {
//					diagnostic += "\nPRECISION: "+">>> The precision has gone ***UP*** by "+precDeltaAbs+". Was "+exp.targetPrecision+"; now "+gotPrecision;
//				}
//			}
//			if ( !recallFine ) {
//				if (recDelta < 0) {
//					diagnostic += "\nRECALL: "+"<<< The recall has gone ***DOWN*** by "+recDeltaAbs+". Was "+exp.targetRecall+"; now "+gotRecall;
//				} else {
//					diagnostic += "\nRECALL: "+">>> The recall has gone ***UP*** by "+recDeltaAbs+". Was "+exp.targetRecall+"; now "+gotRecall;
//				}
//			}
//			if (!runtimeFine) {
//				diagnostic += runtimeAssertionError.getMessage();
//			}
//			assertFalse(diagnostic,true);
//		}

		String errMess = "";
		Pair<Double,Double> expPrecRecall = exp.targetPrecRecall();
		Double expPrec = expPrecRecall.getLeft();
		Double expRecall = expPrecRecall.getRight();

		try {
			Double tolerance = exp.precRecTolerance * evaluator.precision;
			AssertNumber.performanceHasNotChanged("Precision",
				(double) evaluator.precision, expPrec, tolerance);
		} catch (AssertionError e) {
			errMess += e.getMessage()+"\n";
		}

		try {
			Double tolerance = exp.precRecTolerance * evaluator.recall;
			AssertNumber.performanceHasNotChanged("Recall",
				(double) evaluator.recall, expRecall, tolerance);
		} catch (AssertionError e) {
			errMess += e.getMessage()+"\n";
		}

		Double secsPerCase = evaluator.secsPerCase();
		try {
			AssertRuntime.runtimeHasNotChanged(
				secsPerCase, exp.runtimePercTolerance,
			"avg secs per case", testInfo);
		} catch (AssertionError e) {
			errMess += e.getMessage()+"\n";
		}

		if (!errMess.isEmpty()) {
			errMess += wordOutcomeDiffs(exp, evaluator);
			Assertions.fail(errMess);
		}

		if (exp.focusOnWord != null) {
			Assert.fail("Finder was evaluated only on one word.\nSet focusOnWord to null to run it on all available words");
		}
	}

	private String wordOutcomeDiffs(PerformanceExpectations exp, MorphRelativesFinderEvaluator evaluator) {
		String diffs = "";

		for (Object[] gotOutcomeArr: evaluator.wordOutcomes) {
			// Pretty print details of the outcome we are getting for that word
			//
			String word = (String)gotOutcomeArr[0];
			Pair<String,Triple<Integer,Integer,Integer>> outcome =
				PerformanceExpectations.outcomeArr2Tuple(gotOutcomeArr);
			String gotOutcomeDetail = prettyPrintWordOutcome(outcome);


			// Pretty print details of the outcome we expect to get for that word
			//
			outcome = exp.outcome4word(word);
			String expOutcomeDetail = prettyPrintWordOutcome(outcome);

			// Check if the details have changed
			//
			if (!gotOutcomeDetail.equals(expOutcomeDetail)) {
				diffs +=
					"\n"+
					"Outcome differed for word: "+word+"\n"+
					"Expected:\n"+
					expOutcomeDetail+"\n"+
					"Got:\n"+
					gotOutcomeDetail;
			}
		}
		return diffs;
	}

	private String prettyPrintWordOutcome(
		Pair<String, Triple<Integer, Integer, Integer>> outcome) {
		String pretty =
			"   Word                    : "+outcome.getLeft()+"\n"+
			"   Correct produced        : "+outcome.getRight().getLeft()+"\n"+
			"   Relatives produced      : "+outcome.getRight().getMiddle()+"\n"+
			"   Gold Standard Relatives : "+outcome.getRight().getRight();
			;

		return pretty;
	}


	public String getLargeCompilationTrieFilePath() throws Exception {
		String compiledCorpusFilePath = IUConfig.getIUDataPath("/data/tries/trie_compilation-HANSARD-1999-2002---single-form-in-terminals.json");
		File compiledCorpusFile = new File(compiledCorpusFilePath);
		if ( !compiledCorpusFile.exists()) {
			throw new Exception("Did not find the large corpus compilation file. Please download it and place it in "+
					compiledCorpusFilePath+". You can download the file from "+
					"https://www.dropbox.com/s/ka3cn778wgs1mk4/trie_compilation-HANSARD-1999-2002---single-form-in-terminals.json?dl=0");
		}
		return compiledCorpusFilePath;
	}

}
