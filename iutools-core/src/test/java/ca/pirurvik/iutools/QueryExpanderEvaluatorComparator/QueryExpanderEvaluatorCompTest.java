package ca.pirurvik.iutools.QueryExpanderEvaluatorComparator;

import java.io.File;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

import ca.inuktitutcomputing.config.IUConfig;
import ca.pirurvik.iutools.MorphRelativesFinderEvaluator;
import ca.pirurvik.iutools.corpus.CompiledCorpus_InMemory;
import ca.pirurvik.iutools.corpus.CompiledCorpusRegistry;

public class QueryExpanderEvaluatorCompTest {

	@Test
	public void test__QueryExpanderEvaluatorComp() throws Exception {
		
		System.out.println("\n\n*** Running test__QueryExpanderEvaluatorComp. This test can take a few minutes to complete\n\n");;
		
		boolean computeStatsOverSurfaceForms = true;
		
		
		double targetPrecision = 0.6314;
		double targetRecall = 0.4707;
		double precRecTolerance = 0.015;

		long targetRuntimeSecs = 3 * 60; // 5 minutes
		long secsTolerance = 2*60; // 2 minutes
		
		boolean precisionFine = false;
		boolean recallFine = false;
		boolean runtimeFine = false;
		
		String diagnostic = "";
		
		double gotPrecision;
		double gotRecall;
		
		String goldStandardCSVFilePath = IUConfig.getIUDataPath("/src/test/resources/ca/pirurvik/iutools/IU100Words-expansions-added-to-alternatives.csv");
		
		MorphRelativesFinderEvaluator evaluator = new MorphRelativesFinderEvaluator();
		// Set this to true if you want to see print statements.
//		evaluator.verbose = false;
		evaluator.verbose = true;

		CompiledCorpus_InMemory compiledCorpus = CompiledCorpusRegistry.getCorpus();
		evaluator.setCompiledCorpus(compiledCorpus);
		evaluator.setGoldStandard(new File(goldStandardCSVFilePath));
		// whether statistics are to be computed over words (default [true]) or morphemes [false]:
		evaluator.setOptionComputeStatsOverSurfaceForms(computeStatsOverSurfaceForms);

		
		long startMSecs = System.currentTimeMillis();
		evaluator.verbose = true;
		evaluator.run();
		long gotElapsedSecs = (System.currentTimeMillis() - startMSecs) / 1000;
		
		long runtimeDelta = gotElapsedSecs - targetRuntimeSecs;
		long runtimeDeltaAbs = Math.abs(runtimeDelta);
		if (runtimeDeltaAbs < secsTolerance) {
			runtimeFine = true;
		}
				
		gotPrecision = (double)evaluator.precision;
		double precDelta = gotPrecision - targetPrecision;
		double precDeltaAbs = Math.abs(precDelta);
		if (precDeltaAbs < precRecTolerance) {
			precisionFine = true;
		}
		
		gotRecall = (double)evaluator.recall;
		double recDelta = gotRecall - targetRecall;
		double recDeltaAbs = Math.abs(recDelta);
		if (recDeltaAbs < precRecTolerance) {
			recallFine = true;
		}
		
		if (!precisionFine || !recallFine || !runtimeFine) { 
			if ( !precisionFine ) {
				if (precDelta < 0) {
					diagnostic += "\nPRECISION: "+"<<< The precision has gone ***DOWN*** by "+precDeltaAbs+". Was "+targetPrecision+"; now "+gotPrecision;
				} else {
					diagnostic += "\nPRECISION: "+">>> The precision has gone ***UP*** by "+precDeltaAbs+". Was "+targetPrecision+"; now "+gotPrecision;
				}
			}
			if ( !recallFine ) {
				if (recDelta < 0) {
					diagnostic += "\nRECALL: "+"<<< The recall has gone ***DOWN*** by "+recDeltaAbs+". Was "+targetRecall+"; now "+gotRecall;
				} else {
					diagnostic += "\nRECALL: "+">>> The recall has gone ***UP*** by "+recDeltaAbs+". Was "+targetRecall+"; now "+gotRecall;
				}
			}
			if (!runtimeFine) {
				if (runtimeDelta < 0) {
					diagnostic += "\nRUNTIME: "+"<<< The runtime has gone ***DOWN*** by "+runtimeDeltaAbs+" secs. Was "+targetRuntimeSecs+"; now "+gotElapsedSecs;
				} else {
					diagnostic += "\nRUNTIME: "+">>> The runtime has gone ***UP*** by "+runtimeDeltaAbs+" secs. Was "+targetRuntimeSecs+"; now "+gotElapsedSecs;
				}	
			}
			assertFalse(diagnostic,true);
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
}
