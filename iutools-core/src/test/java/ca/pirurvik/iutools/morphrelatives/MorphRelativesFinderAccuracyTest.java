package ca.pirurvik.iutools.morphrelatives;

import java.io.File;

import ca.pirurvik.iutools.corpus.CompiledCorpus;
import ca.pirurvik.iutools.corpus.CompiledCorpusRegistry;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

import ca.inuktitutcomputing.config.IUConfig;

public class MorphRelativesFinderAccuracyTest {

	@Test
	public void test__findRelatives__QuickAccuracyTest() throws Exception {
		PerformanceExpectations expectations =
			new PerformanceExpectations()

				.setCorpusName("HANSARD-1999-2002.v2020-11-02")

				.setVerbosity(true)

				// Set this to a single word if you only want to run that one word
//				.setFocusOnWord("mikinniqsanut")


				// Each morpheme should run in about 1.5 secs, give or take 0.5
				.setTargetRuntimeSecs(2.5, 0.5)

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
				.setTargetPrecision(0.51)

				.setTargetRecall(0.42)
				.setPrecRecTolerance(0.02)
			;

		evaluatePerformance(expectations, 10);
	}

	@Test
	public void test__MorphRelativesFinderAccuracy() throws Exception {
		
		System.out.println("\n\n*** Running test__MorphRelativesFinderAccuracy. This test can take a few minutes to complete\n\n");;
		
		PerformanceExpectations expectations =
			new PerformanceExpectations()

			.setCorpusName("HANSARD-1999-2002.v2020-11-02")

			.setComputeStatsOverSurfaceForms(true)

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
			.setTargetPrecision(0.45)
			.setTargetRecall(0.33)

			.setPrecRecTolerance(0.015)
			// Each word should take on average 2 secs, give or take 1 sec
			.setTargetRuntimeSecs(2 , 1)
			;

		evaluatePerformance(expectations);
	}

	private void evaluatePerformance(PerformanceExpectations exp) throws Exception {
		evaluatePerformance(exp, null);
	}

	private void evaluatePerformance(PerformanceExpectations exp,
		Integer stopAfterNWords) throws Exception {
		String goldStandardCSVFilePath = IUConfig.getIUDataPath("/src/test/resources/ca/pirurvik/iutools/IU100Words-expansions-added-to-alternatives.csv");

		// This is the NEW version of the corpus, which has some
		// non-empty decomp samples
		//
		CompiledCorpus corpus =
			CompiledCorpusRegistry
				.getCorpusWithName(exp.corpusName);

		MorphRelativesFinder finder = new MorphRelativesFinder(corpus);

		MorphRelativesFinderEvaluator evaluator =
			new MorphRelativesFinderEvaluator(
				finder, new File(goldStandardCSVFilePath));

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
		evaluator.verbose = true;
		evaluator.run();
		long gotElapsedSecs = (System.currentTimeMillis() - startMSecs) / 1000;

		double runtimeDelta = -9999.0; 	double runtimeDeltaAbs = 9999.0;
		// targetRuntimeSecs < 0 means we don't care about runtime
		if (exp.targetRuntimeSecs > 0) {
			runtimeDelta = evaluator.secsPerCase() - exp.targetRuntimeSecs;
			runtimeDeltaAbs = Math.abs(runtimeDelta);
			if (runtimeDeltaAbs > exp.secsTolerance) {
				runtimeFine = false;
			}
		}

		// targetPrecision < 0 means we don't care about precision
		double precDelta= -1.0; double precDeltaAbs = 1.0;
		if (exp.targetPrecision > 0) {
			gotPrecision = (double) evaluator.precision;
			precDelta = gotPrecision - exp.targetPrecision;
			precDeltaAbs = Math.abs(precDelta);
			if (precDeltaAbs > exp.precRecTolerance) {
				precisionFine = false;
			}
		}

		double recDelta=-1.0; double recDeltaAbs=1.0;

		// targetRecall < 0 means we don't care about recall
		if (exp.targetRecall > 0) {
			gotRecall = (double) evaluator.recall;
			recDelta = gotRecall - exp.targetRecall;
			recDeltaAbs = Math.abs(recDelta);
			if (recDeltaAbs > exp.precRecTolerance) {
				recallFine = false;
			}
		}

		if (!precisionFine || !recallFine || !runtimeFine) {
			if ( !precisionFine ) {
				if (precDelta < 0) {
					diagnostic += "\nPRECISION: "+"<<< The precision has gone ***DOWN*** by "+precDeltaAbs+". Was "+exp.targetPrecision+"; now "+gotPrecision;
				} else {
					diagnostic += "\nPRECISION: "+">>> The precision has gone ***UP*** by "+precDeltaAbs+". Was "+exp.targetPrecision+"; now "+gotPrecision;
				}
			}
			if ( !recallFine ) {
				if (recDelta < 0) {
					diagnostic += "\nRECALL: "+"<<< The recall has gone ***DOWN*** by "+recDeltaAbs+". Was "+exp.targetRecall+"; now "+gotRecall;
				} else {
					diagnostic += "\nRECALL: "+">>> The recall has gone ***UP*** by "+recDeltaAbs+". Was "+exp.targetRecall+"; now "+gotRecall;
				}
			}
			if (!runtimeFine) {
				if (runtimeDelta < 0) {
					diagnostic += "\nRUNTIME: "+"<<< The runtime has gone ***DOWN*** by "+runtimeDeltaAbs+" secs. Was "+exp.targetRuntimeSecs+"; now "+evaluator.secsPerCase();
				} else {
					diagnostic += "\nRUNTIME: "+">>> The runtime has gone ***UP*** by "+runtimeDeltaAbs+" secs. Was "+exp.targetRuntimeSecs+"; now "+evaluator.secsPerCase();
				}
			}
			assertFalse(diagnostic,true);
		}

		if (exp.focusOnWord != null) {
			Assert.fail("Finder was evaluated only on one word.\nSet focusOnWord to null to run it on all available words");
		}
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

	public static class PerformanceExpectations {

		public String corpusName = CompiledCorpusRegistry.defaultCorpusName;
		public boolean computeStatsOverSurfaceForms;
		public double targetPrecision = -1;
		public double targetRecall = -1;
		public double precRecTolerance;
		public double targetRuntimeSecs = -1;
		public double secsTolerance;
		private String focusOnWord;
		private boolean verbose;

		public PerformanceExpectations setComputeStatsOverSurfaceForms(
				boolean _computeStatsOverSurfaceForms) {
			this.computeStatsOverSurfaceForms = _computeStatsOverSurfaceForms;
			return this;
		}

		public PerformanceExpectations setTargetPrecision(
			double _targetPrecision) {
			this.targetPrecision = _targetPrecision;
			return this;
		}

		public PerformanceExpectations setTargetRecall(double _targetRecall) {
			this.targetRecall = _targetRecall;
			return this;
		}

		public PerformanceExpectations setPrecRecTolerance(double _precRecallTolerance) {
			this.precRecTolerance = _precRecallTolerance;
			return this;
		}

		public PerformanceExpectations setTargetRuntimeSecs(
			double _targetRuntimeSecs, double _secsTolerance) {
			this.targetRuntimeSecs = _targetRuntimeSecs;
			this.secsTolerance = _secsTolerance;
			return this;
		}

		public PerformanceExpectations setFocusOnWord(String _word) {
			this.focusOnWord = _word;
			return this;
		}

		public PerformanceExpectations setVerbosity(boolean _verbose) {
			this.verbose = _verbose;
			return this;
		}

		public PerformanceExpectations setCorpusName(String _corpusName) {
			this.corpusName = _corpusName;
			return this;
		}
	}
}
